package net.novaware.chip8.core.memory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static net.novaware.chip8.core.util.HexUtil.toHexString;
import static net.novaware.chip8.core.util.UnsignedUtil.uint;
import static net.novaware.chip8.core.util.UnsignedUtil.ushort;

public class MappedMemory extends AbstractMemory implements Memory {

    private static final Logger LOG = LogManager.getLogger();

    public static class Entry {
        public final short start;
        public final short end;
        public final Memory ref;

        public Entry(short start, short end, Memory ref) {
            this.start = start;
            this.end = end;
            this.ref = ref;
        }
    }

    private final List<Entry> entries;

    public MappedMemory(final String name, final List<Entry> entries) {
        super(name);

        this.entries = new ArrayList<>(entries);

        short currentStart = 0x0000;

        for (Entry entry : entries) {
            final int end = uint(entry.end);
            final int start = uint(entry.start);

            assert start < end;

            int addressBasedSize = end - start + 1;
            int refBasedSize = entry.ref.getSize();

            assert addressBasedSize == refBasedSize : "addressing for " + entry.ref.getName();
            assert currentStart == entry.start : "contiguity of " + entry.ref.getName();

            currentStart = ushort(end + 1);
        }
    }

    @Override
    public int getSize() {
        return entries.stream().mapToInt(e -> e.ref.getSize()).sum(); //TODO: should not be called in cpu loop!
    }

    /*package*/ Entry getSegment(short address) {

        int addr = uint(address);
        int size = entries.size();
        for (int i = 0; i < size; i++) {
            Entry entry = entries.get(i);

            final int end = uint(entry.end);
            final int start = uint(entry.start);

            if (addr >= start && addr <= end) {
                return entry;
            }
        }

        throw new IllegalArgumentException("Unable to find memory segment for address " + toHexString(address));
    }

    /*package*/ short translateToSegmentAddress(Entry segment, short address) {
        final int addr =  uint(address);
        final int start = uint(segment.start);

        return ushort(addr - start);
    }

    @Override
    public void getBytes(short address, byte[] destination, int length) {
        final Entry segment = getSegment(address);
        short localAddress = translateToSegmentAddress(segment, address);

        assert segment.ref.getSize() >= length : "getting data across segments is not supported"; //TODO: maybe implement?

        LOG.trace(() -> segment.ref.getName() + " @ " + toHexString(address));

        segment.ref.getBytes(localAddress, destination, length);
    }

    @Override
    public byte getByte(short address) {
        final Entry segment = getSegment(address);
        short localAddress = translateToSegmentAddress(segment, address);

        LOG.trace(() -> segment.ref.getName() + " @ " + toHexString(address));

        return segment.ref.getByte(localAddress);
    }

    @Override
    public void setBytes(short address, byte[] source, int length) {
        final Entry segment = getSegment(address);
        short localAddress = translateToSegmentAddress(segment, address);

        assert segment.ref.getSize() >= length : "getting data across segments is not supported"; //TODO: maybe implement?

        LOG.trace(() -> segment.ref.getName() + " @ " + toHexString(address));

        segment.ref.setBytes(localAddress, source, length);
    }

    @Override
    public void setByte(short address, byte source) {
        final Entry segment = getSegment(address);
        short localAddress = translateToSegmentAddress(segment, address);

        LOG.trace(() -> segment.ref.getName() + " @ " + toHexString(address));

        segment.ref.setByte(localAddress, source);
    }


    @Override
    public short getWord(short address) {
        final Entry segment = getSegment(address);
        short localAddress = translateToSegmentAddress(segment, address);

        LOG.trace(() -> segment.ref.getName() + " @ " + toHexString(address));

        return segment.ref.getWord(localAddress);
    }

    @Override
    public void setWord(short address, short instruction) {
        final Entry segment = getSegment(address);
        short localAddress = translateToSegmentAddress(segment, address);

        LOG.trace(() -> segment.ref.getName() + " @ " + toHexString(address));

        segment.ref.setWord(localAddress, instruction);
    }
}
