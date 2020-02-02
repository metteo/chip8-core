package net.novaware.chip8.core.cpu.unit;

import net.novaware.chip8.core.cpu.register.Registers;
import net.novaware.chip8.core.memory.Memory;

/**
 * GPU
 */
public class GraphicsProcessing {

    public static final boolean DUMP_SPRITE = false;
    public static final boolean CLIPPING = false;

    //TODO: gpu should have own memory and registers
    //TODO: cpu has access to those and can write to video memory stuff that gpu should do / draw
    //TODO: when cpu is ready it sets a register which tells gpu to execute the code in its memory
    //TODO: that doesn't block the cpu since gpu has its own clock cycle

    //TODO: create memory buffer for display, buffer for currently drawn sprite, maybe double buffering?

    //TODO: registers for area invalidation

    private final Registers registers;

    private final Memory memory;

    //TODO: make it part of gpu memory and one gpu instruction that uses it
    private final byte[] sprite = new byte[15];

    public GraphicsProcessing(Registers registers, Memory memory) {
        this.registers = registers;
        this.memory = memory;
    }

    public void clearScreen() {
        registers.redraw = true;

        short addr = registers.getGraphicSegment().get();

        for (short i = addr; i < addr + (32 * 64 / 8); i++) {
            memory.setByte(i, (byte) 0x0);
        }
    }

    //FIXME: needs to be rewriten & fixed and tested!

    public void drawSprite(int x, int y, short height) {
        final short addr = registers.getIndex().get();

        registers.redraw = true;

        byte xVal = registers.getVariable(x).get();
        byte yVal = registers.getVariable(y).get();

        //TODO: test wrapping of full sprites
        xVal %= 64;
        yVal %= 32;

        final int gfxStart = registers.getGraphicSegment().getAsInt();

        if (DUMP_SPRITE) System.out.println(String.format("I: 0x%04X (%d)", addr, Short.toUnsignedInt(addr)));

        memory.getBytes(addr, sprite, height);

        //FIXME: temporary check if sprites are ok:
        if (DUMP_SPRITE) dumpSprite(height, sprite);

        byte collision = 0;

        for (int i = 0; i < height; i++) { //y axis
            int yPixel = Byte.toUnsignedInt(yVal) + i;

            if (CLIPPING && yPixel >= 32) { //TODO: test y axis clipping
                break;
            } else {
                yPixel %= 32;
            }

            for(int j = 0; j < 8; j++) {//x axis

                int xPixel = Byte.toUnsignedInt(xVal) + j;

                if (CLIPPING && xPixel >= 64) { //TODO: test x axis clipping
                    break;
                } else {
                    xPixel %= 64;
                }

                int globalBit = yPixel * 64 + xPixel;
                int gfxOffset = globalBit / 8;
                int localBit = globalBit % 8;

                short gfxIndex = (short) (gfxStart + gfxOffset);

                byte frame = memory.getByte(gfxIndex);

                int currMask = 0x1 << localBit;
                int currPixel = (Byte.toUnsignedInt(frame) & currMask) >>> localBit;

                int newMask = 0x1 << (7 - j);
                int newPixel = (Byte.toUnsignedInt(sprite[i]) & newMask) >>> (7 - j);

                int finalPixel = currPixel ^ newPixel;

                if (finalPixel == 1) {
                    frame |= currMask;
                } else {
                    frame &= ~currMask;

                }

                memory.setByte(gfxIndex, frame);

                if (currPixel == 1 && finalPixel == 0) {
                    collision = (byte) 0x01;

                }
            }
        }

        registers.getVariable(0xF).set(collision);
    }

    private void dumpSprite(short height, byte[] sprite) {
        for (int i = 0; i < height; i++) { //y
            for (int j = 7; j >= 0; j--) { //x
                int mask = 0x1 << j;
                int pixel = (Byte.toUnsignedInt(sprite[i]) & mask) >>> j;

                System.out.print(pixel != 0 ? "█" : "░");
            }
            System.out.println();
        }
    }
}
