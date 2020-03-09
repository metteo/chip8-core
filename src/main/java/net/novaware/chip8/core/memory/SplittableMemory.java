package net.novaware.chip8.core.memory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

import static net.novaware.chip8.core.util.AssertUtil.assertState;
import static net.novaware.chip8.core.util.HexUtil.toHexString;
import static net.novaware.chip8.core.util.UnsignedUtil.uint;
import static net.novaware.chip8.core.util.UnsignedUtil.ushort;

/**
 * Splits Memory into ROM and RAM region
 * When in strict mode, writes to ROM trigger exception
 */
public class SplittableMemory extends MemoryDecorator implements Memory {

    private static final Logger LOG = LogManager.getLogger();

    private int split = 0; //start with whole memory being RW

    private Supplier<Boolean> strict = () -> true;

    public SplittableMemory(Memory memory) {
        super(memory);
    }

    /**
     * Sets the split point.
     * 0     ... split - 1            = ROM
     * split ... memory.getSize() - 1 = RAM
     * @param split
     */
    public void setSplit(int split) {
        LOG.debug(() -> "<0x0000, " + toHexString(ushort(split)) + ") " + getName() + " ROM");
        LOG.debug(() -> "<" + toHexString(ushort(split)) + ", " + toHexString(ushort(getSize())) + ") " + getName() +" RAM");

        this.split = split;
    }

    public boolean isStrict() {
        return strict.get();
    }

    public void setStrict(boolean strict) {
        this.strict = () -> strict;
    }

    public void setStrict(Supplier<Boolean> strict) {
        this.strict = strict;
    }

    private boolean isRam(short address) {
        return uint(address) >= split;
    }

    private boolean isRom(short address) {
        return !isRam(address);
    }

    private String getSubName(short address) {
        return isRom(address) ? "ROM" : "RAM";
    }

    @Override
    public byte getByte(short address) {
        LOG.trace(() -> memory.getName() + " " + getSubName(address) + " @ " + toHexString(address));

        return super.getByte(address);
    }

    @Override
    public void setByte(short address, byte value) {
        boolean rom = isRom(address);

        if (rom) {
            assertState(!strict.get(), "can not write in ROM");
            LOG.warn(() -> memory.getName() + " ROM " + toHexString(address));
        } else {
            LOG.trace(() -> memory.getName() + " RAM " + toHexString(address));
        }

        super.setByte(address, value);
    }

    @Override
    public short getWord(short address) {
        LOG.trace(() -> memory.getName() + " " + getSubName(address) + " @ " + toHexString(address));

        return super.getWord(address);
    }

    @Override
    public void setWord(short address, short value) {
        boolean rom = isRom(address);

        if (rom) {
            assertState(!strict.get(), "can not write in ROM");
            LOG.warn(() -> memory.getName() + " ROM " + toHexString(address));
        } else {
            LOG.trace(() -> memory.getName() + " RAM " + toHexString(address));
        }

        super.setWord(address, value);
    }

    @Override
    public void getBytes(short address, byte[] destination, int length) {
        LOG.trace(() -> memory.getName() + " " + getSubName(address) + " @ " + toHexString(address));

        super.getBytes(address, destination, length);
    }

    @Override
    public void setBytes(short address, byte[] source, int length) {
        boolean rom = isRom(address);

        if (rom) {
            assertState(!strict.get(), "can not write in ROM");
            LOG.warn(() -> memory.getName() + " ROM " + toHexString(address));
        } else {
            LOG.trace(() -> memory.getName() + " RAM " + toHexString(address));
        }

        super.setBytes(address, source, length);
    }
}
