package net.novaware.chip8.core.port.impl;

import net.novaware.chip8.core.cpu.register.ByteRegister;
import net.novaware.chip8.core.cpu.register.RegisterFile;
import net.novaware.chip8.core.gpu.ViewPort;
import net.novaware.chip8.core.memory.Memory;
import net.novaware.chip8.core.port.DisplayPort;
import net.novaware.chip8.core.util.uml.Owned;
import net.novaware.chip8.core.util.uml.Used;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;
import static net.novaware.chip8.core.cpu.register.RegisterFile.*;
import static net.novaware.chip8.core.util.UnsignedUtil.uint;
import static net.novaware.chip8.core.util.UnsignedUtil.ushort;

public class DisplayPortImpl implements DisplayPort {

    @Used
    private final ByteRegister graphicChange;

    @Used
    private final Memory displayIo;

    @Owned
    private final ViewPort viewPort = new ViewPort();
    private ViewPort.Bit bit = new ViewPort.Bit();
    private ViewPort.Index idx = new ViewPort.Index();

    @Owned
    private final boolean[][] frontBuffer = new boolean[viewPort.getMaxHeight()][viewPort.getMaxWidth()]; // [y][x]
    private boolean frontBufferTouched = true;

    private final boolean[][] backBuffer = new boolean[viewPort.getMaxHeight()][viewPort.getMaxWidth()]; // [y][x]
    private final boolean[][] prevBuffer = new boolean[viewPort.getMaxHeight()][viewPort.getMaxWidth()]; // [y][x]

    private boolean attachedToRegister = false;

    /**
     * Previous meaningful gc, idle or noop doesn't affect it
     */
    private int prevRealGc = RegisterFile.GC_DRAW;

    /**
     * Keeps time of the last GC
     */
    private long prevGcTime = -1;

    @Owned
    private final Packet packet = new Packet() {
        @Override
        public int getColumnCount() {
            return viewPort.getMaxWidth();
        }

        @Override
        public int getRowCount() {
            return viewPort.getMaxHeight();
        }

        @Override
        public boolean getPixel(int column, int row) {
            return frontBuffer[row][column];
        }
    };

    private @Nullable Consumer<Packet> receiver;
    private Mode mode = Mode.DIRECT;

    public DisplayPortImpl(ByteRegister graphicChange, Memory displayIo) {
        this.graphicChange = graphicChange;
        this.displayIo = displayIo;
    }

    /**
     * Should be only called in case a single instance is used and can use GC register exclusively.
     * In case of multiple instances owner of the instances should forward the callbacks and
     * clear the register afterwards
     */
    public void attachToRegister(){
        this.graphicChange.subscribe(gc -> onGraphicChange());
        attachedToRegister = true;
    }

    public void onGraphicChange() {
        prevGcTime = System.nanoTime();

        final short gc = graphicChange.get();

        if (gc == GC_IDLE || gc == GC_NOOP) {
            return; // prevent recursive loop or unneeded updates
        }

        updateBuffers();

        maybeCallReceiver();

        prevRealGc = gc;

        if (attachedToRegister) {
            graphicChange.set(GC_IDLE);
        }
    }

    private void maybeCallReceiver() {
        if (receiver != null && frontBufferTouched) {
            receiver.accept(packet);
        }

        frontBufferTouched = false;
    }

    //TODO: consider switching array references instead of copying bits of data when switching buffers
    private void updateBuffers() {
        final boolean fallingEdge = isFallingEdge();

        for (int y  = 0; y < viewPort.getMaxHeight(); ++y) {
            for (int x  = 0; x < viewPort.getMaxWidth(); ++x) {
                bit.x = x;
                bit.y = y;

                viewPort.toIndex(bit, idx, false);

                byte frame = displayIo.getByte(ushort(idx.arrayByte));
                int mask = 0x1 << 7 - idx.byteBit;
                int pixel = (uint(frame) & mask) >>> 7 - idx.byteBit;
                boolean pixelOn = pixel != 0;

                prevBuffer[y][x] = backBuffer[y][x];
                backBuffer[y][x] = pixelOn;

                switch(mode) {
                    case MERGE_FRAME:
                        frontBuffer[y][x] = backBuffer[y][x] | prevBuffer[y][x];
                        frontBufferTouched = true;
                        break;

                    case FALLING_EDGE:
                        if (fallingEdge) {
                            frontBuffer[y][x] = prevBuffer[y][x];
                            frontBufferTouched = true;
                        }
                        break;
                    case DIRECT:
                    default:
                        frontBuffer[y][x] = backBuffer[y][x];
                        frontBufferTouched = true;
                        break;
                }
            }
        }
    }

    private boolean isFallingEdge() {
        final short gc = graphicChange.get();
        return (prevRealGc != GC_ERASE && gc == GC_ERASE) || gc == GC_MIX;
    }

    @Override
    public void connect(Consumer<Packet> receiver) {
        requireNonNull(receiver, "receiver must not be null");

        this.receiver = receiver;
    }

    @Override
    public Mode getMode() {
        return mode;
    }

    @Override
    public void setMode(Mode mode) {
        requireNonNull(mode, "mode must not be null");

        this.mode = mode;
    }

    public void tick() {
        if (mode == Mode.FALLING_EDGE) {
            long now = System.nanoTime();
            if (now - prevGcTime > TimeUnit.SECONDS.toNanos(1)) {
                updateFrontBuffer();
                prevGcTime = now;
            }
        }
    }

    //TODO: consider switching array references instead of copying bits of data when switching buffers
    private void updateFrontBuffer() {
        for (int y  = 0; y < viewPort.getMaxHeight(); ++y) {
            for (int x  = 0; x < viewPort.getMaxWidth(); ++x) {
                frontBuffer[y][x] = backBuffer[y][x];
                frontBufferTouched = true;
            }
        }

        maybeCallReceiver();
    }

    @Override
    public void disconnect() {
        receiver = null;
    }
}
