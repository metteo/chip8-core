package net.novaware.chip8.core.gpu;

import net.novaware.chip8.core.cpu.register.RegisterFile;
import net.novaware.chip8.core.gpu.ViewPort.Index;
import net.novaware.chip8.core.memory.Memory;
import net.novaware.chip8.core.memory.MemoryModule;
import net.novaware.chip8.core.util.di.BoardScope;
import net.novaware.chip8.core.util.uml.Owned;
import net.novaware.chip8.core.util.uml.Used;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.signedness.qual.Unsigned;

import javax.inject.Inject;
import javax.inject.Named;

import static net.novaware.chip8.core.cpu.register.RegisterFile.*;
import static net.novaware.chip8.core.gpu.ViewPort.Bit;
import static net.novaware.chip8.core.memory.MemoryModule.MMU;
import static net.novaware.chip8.core.util.AssertUtil.assertState;
import static net.novaware.chip8.core.util.HexUtil.toHexString;
import static net.novaware.chip8.core.util.UnsignedUtil.*;

/**
 * Graphics Processing Unit
 */
@BoardScope
public class Gpu {

    private static final Logger LOG = LogManager.getLogger();

    public static final int MAX_SPRITE_HEIGHT = 0x10;

    public interface Config {
        /**
         * Boot-128 expects this to be true. Without the value in V4 would overflow a nibble
         * so the font address loading routine which doesn't check it, would load the address
         * after font area...
         */
        boolean isTrimVarForFont();
    }

    @Owned
    private final Config config;

    @Used
    private final RegisterFile registers;

    @Used
    private final Memory memory;

    @Owned
    private final ViewPort viewPort = new ViewPort();

    private byte[] spriteBuffer;

    private short[] memoryBuffer; //optimizes misaligned memory access

    private byte[] paintBuffer;

    private byte[] resultBuffer;

    //FIXME: separate wrapping / clipping into x & y axis
    //TODO: BLITZ doesn't like y wrapping, VERS requires y wrapping
    final boolean wrapping = true;
    final boolean clipping = false;
    final boolean dump = true;

    @Inject
    public Gpu(Config config, RegisterFile registers, @Named(MMU) Memory memory) {
        this.config = config;
        this.registers = registers;
        this.memory = memory;

        spriteBuffer = new byte[MAX_SPRITE_HEIGHT];
        memoryBuffer = new short[MAX_SPRITE_HEIGHT];
        paintBuffer = new byte[MAX_SPRITE_HEIGHT];
        resultBuffer = new byte[MAX_SPRITE_HEIGHT];
    }

    public void reset() {
        registers.getGraphicChange().set(GC_IDLE);
    }

    public void clearScreen() {
        final int gs = registers.getGraphicSegment().getAsInt();

        for(int i = 0; i < MemoryModule.DISPLAY_IO_SIZE; ++i) {
            memory.setByte(ushort(gs + i), UBYTE_0);
        }

        registers.getStatus().set(0x1);
        registers.getStatusType().set(VF_COLLISION);

        registers.getGraphicChange().set(GC_ERASE);
    }

    //TODO: unit test
    public void scrollUp(final short n) {
        final int gs = registers.getGraphicSegment().getAsInt();

        int lines = uint(n);
        int bytes = lines * 8;
        int gfxSize = MemoryModule.DISPLAY_IO_SIZE;

        for(int b = 0; b < gfxSize; ++b) {
            if (b < gfxSize - bytes) {
                memory.setByte(ushort(gs + b), memory.getByte(ushort(gs + b + bytes)));
            } else {
                memory.setByte(ushort(gs + b), UBYTE_0);
            }
        }
    }

    public void loadFontAddressIntoRegister(final short x) {
        int xValue = registers.getVariable(x).getAsInt();

        boolean validXValue = xValue >= 0 && xValue <= 0xF;

        if (config.isTrimVarForFont()) {
            if (!validXValue) {
                LOG.warn("Trimming V" + x + " value: " + toHexString(ubyte(xValue)));
                xValue &= 0xF;
            }
        } else {
            assertState(validXValue, "V" + x + " register should be between 0 and 0xF");
        }

        final int fontSegment = registers.getFontSegment().getAsInt();
        int offset = getFontOffset(xValue);

        final int fontAddress = (fontSegment & 0xFF00) | (0xFF & offset);
        registers.getIndex().set(fontAddress);
    }

    private int getFontOffset(int xValue) {
        int offset;
        switch(xValue) {
            case 0x0: offset = 0x30; break;
            case 0x1: offset = 0x39; break;
            case 0x2: offset = 0x22; break;
            case 0x3: offset = 0x2A; break;
            case 0x4: offset = 0x3E; break;
            case 0x5: offset = 0x20; break;
            case 0x6: offset = 0x24; break;
            case 0x7: offset = 0x34; break;
            case 0x8: offset = 0x26; break;
            case 0x9: offset = 0x28; break;
            case 0xA: offset = 0x2E; break;
            case 0xB: offset = 0x18; break;
            case 0xC: offset = 0x14; break;
            case 0xD: offset = 0x1C; break;
            case 0xE: offset = 0x10; break;
            case 0xF: offset = 0x12; break;
            default: throw new AssertionError("should not happen");
        }
        return offset;
    }

    public void drawSprite(short x, short y, short height) {
        final int xBit = registers.getVariable(x).getAsInt();
        final int yBit = registers.getVariable(y).getAsInt();

        if (viewPort.isOutOfBounds(xBit, yBit) && !wrapping) {
            //TODO: handle no wrapping scenario: do not read from memory
            return;
        }

        final short spriteAddress = registers.getIndex().get();
        final int spriteHeight = uint(height);

        fillSpriteBuffer(spriteAddress, spriteBuffer, spriteHeight);
        fillPaintBuffer(xBit, yBit, paintBuffer, spriteHeight);
        byte gc = xorBuffers(xBit, yBit, spriteHeight);
        storePaintBuffer(xBit, yBit, resultBuffer, spriteHeight);

        registers.getStatus().set((gc == GC_ERASE || gc == GC_MIX) ? 0x1 : 0x0);
        registers.getStatusType().set(VF_COLLISION);

        registers.getGraphicChange().set(gc); // must be last because triggers redraw
    }

    /* package */ void fillSpriteBuffer(final short address, final byte[] buffer, final int height) {
        memory.getBytes(address, buffer, height);

        if (dump) dumpBuffer("sprite", ushort(height), buffer);
    }

    /* package */ void fillPaintBuffer(int xBit, int yBit, final byte[] buffer, final int height) {
        //TODO: get rid of these instantiations from emulator loop, maybe use pooling
        final Bit bit = new Bit(xBit, yBit);
        final Index idx1 = new Index(); // for byte aligned memory access
        final Index idx2 = new Index(); // additional, for misaligned case

        final int graphicSegment = registers.getGraphicSegment().getAsInt();

        for (int row = 0; row < height; ++row) {
            int currentYBit = yBit + row;

            bit.x = xBit;
            bit.y = currentYBit;

            viewPort.toIndex(bit, idx1, !clipping);

            byte rowData;
            if (idx1.byteBit == 0) { // byte aligned
                rowData = memory.getByte(ushort(graphicSegment + idx1.arrayByte));
            } else { // misaligned
                bit.x = bit.x + 8;
                viewPort.toIndex(bit, idx2,  !clipping);

                short word = getRowAsWord(graphicSegment, idx1, idx2);
                memoryBuffer[row] = word;

                rowData = ubyte(uint(word) >>> (8 - idx1.byteBit));
            }

            buffer[row] = rowData;
        }

        if (dump) dumpBuffer("paint " + xBit + ", " + yBit + " ", ushort(height), buffer);
    }

    private short getRowAsWord(int graphicSegment, Index idx1, Index idx2) {
        short rowIndex1 = ushort(graphicSegment + idx1.arrayByte);
        short rowIndex2 = ushort(graphicSegment + idx2.arrayByte);

        byte rowData1 = memory.getByte(rowIndex1);
        byte rowData2 = memory.getByte(rowIndex2);

        return ushort((uint(rowData1) << 8) | uint(rowData2));
    }

    //TODO: handle clipping in xor method
    /* package */ byte xorBuffers(int xBit, int yBit, final int height) {
        boolean erasing = false;
        boolean drawing = false;

        for (int y = 0; y < height; ++y) {
            final @Unsigned int spriteRow = uint(spriteBuffer[y]);
            final @Unsigned int paintRow = uint(paintBuffer[y]);

            resultBuffer[y] = ubyte(spriteRow ^ paintRow);

            final @Unsigned int resultRow = uint(resultBuffer[y]);

            // inverse logical consequence ~(p => q) <=> ~(~p | q) <=> p & ~q
            // 1->0 = 1, the rest is 0
            if ((paintRow & ~resultRow) > 0) {
                erasing = true;
            }

            // special ~p & q
            // 0->1 = 1, the rest is 0
            if ((~paintRow & resultRow) > 0) {
                drawing = true;
            }
        }

        if (dump) dumpBuffer("result", ushort(height), resultBuffer);

        return getGraphicChange(erasing, drawing);
    }

    private byte getGraphicChange(boolean erasing, boolean drawing) {
        byte gc;
        if (drawing && erasing) {
            gc = GC_MIX;
        } else if (drawing) {
            gc = GC_DRAW;
        } else if (erasing) {
            gc = GC_ERASE;
        } else {
            gc = GC_NOOP;
        }
        return gc;
    }

    /* package */ void storePaintBuffer(int xBit, int yBit, final byte[] buffer, final int height) {
        //TODO: get rid of these instantiations from emulator loop, maybe use pooling
        final Bit bit = new Bit(xBit, yBit);
        final Index idx1 = new Index(); // for byte aligned memory access
        final Index idx2 = new Index(); // additional, for misaligned case

        final int graphicSegment = registers.getGraphicSegment().getAsInt();

        for (int row = 0; row < height; ++row) {
            int currentYBit = yBit + row;

            bit.x = xBit;
            bit.y = currentYBit;

            viewPort.toIndex(bit, idx1, !clipping);

            if (idx1.byteBit == 0) { // byte aligned
                memory.setByte(ushort(graphicSegment + idx1.arrayByte), buffer[row]);
            } else { // misaligned
                bit.x = bit.x + 8;
                viewPort.toIndex(bit, idx2, !clipping);

                short rowIndex1 = ushort(graphicSegment + idx1.arrayByte);
                short rowIndex2 = ushort(graphicSegment + idx2.arrayByte);

                //TODO: maybe store this part of memory on hand when filling so it's available here?
                int rowData1 = uint(memory.getByte(rowIndex1));
                int rowData2 = uint(memory.getByte(rowIndex2));

                int mask1 = (1 << (8 - idx1.byteBit)) - 1 ;
                int mask2 = uint(ubyte(~mask1)); // cut off unneeded 1s on the front

                rowData1 = (rowData1 & mask2) | (uint(buffer[row]) >>> idx1.byteBit);
                rowData2 = (rowData2 & mask1) | (uint(buffer[row]) << (8 - idx1.byteBit));

                memory.setByte(rowIndex1, ubyte(rowData1));
                memory.setByte(rowIndex2, ubyte(rowData2));
            }
        }
    }

    private void dumpBuffer(String title, short height, byte[] buffer) {
        LOG.debug("buffer: " + title);
        for (int i = 0; i < height; i++) { //y
            StringBuilder sb = new StringBuilder();

            for (int j = 7; j >= 0; j--) { //x
                int mask = 0x1 << j;
                int pixel = (uint(buffer[i]) & mask) >>> j;

                sb.append(pixel != 0 ? "█" : "░");
            }
            LOG.debug(sb);
        }
    }
}
