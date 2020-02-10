package net.novaware.chip8.core.cpu.unit

import net.novaware.chip8.core.cpu.register.Registers
import net.novaware.chip8.core.memory.PhysicalMemory
import spock.lang.Specification
import spock.lang.Unroll

import static net.novaware.chip8.core.cpu.register.Registers.GC_DRAW
import static net.novaware.chip8.core.cpu.register.Registers.GC_ERASE
import static net.novaware.chip8.core.cpu.register.Registers.GC_MIX
import static net.novaware.chip8.core.cpu.register.Registers.GC_NOOP
import static net.novaware.chip8.core.cpu.unit.GraphicsProcessing.MAX_SPRITE_HEIGHT
import static net.novaware.chip8.core.util.HexUtil.toHexString

class GraphicsProcessingSpec extends Specification {

    private static final boolean DUMP_MEMORY = Boolean.getBoolean(GraphicsProcessingSpec.getSimpleName() + ".DUMP_MEMORY")

    def registers = new Registers()

    def memory = new PhysicalMemory("GFX", 256)

    def instance = new GraphicsProcessing(registers, memory)

    def "should properly extract sprite from memory"() {
        given:
        short address = 0x3
        int height = 5

        byte[] sprite = [0x12, 0x34, 0x56, 0x78, 0x9A]

        memory.setBytes(address, sprite, sprite.length)

        byte[] buffer = new byte[MAX_SPRITE_HEIGHT]

        when:
        instance.fillSpriteBuffer(address, buffer, height)

        then:
        assertBufferContent(buffer, sprite, height)
    }

    static void assertBufferContent(byte[] buffer, byte[] sprite, int height) {
        for(def i = 0; i < height; ++i) {
            assert buffer[i] == sprite[i]
        }

        for(def i = height; i < buffer.length; ++i) {
            assert buffer[i] == 0 as byte
        }
    }

    @Unroll
    def "should properly extract painted area from memory (byte aligned: #xBit, #yBit, #height)"() {
        given:

        byte[] sprite = [0x12, 0x34, 0x56, 0x78, 0x9A, 0xBC, 0xDE, 0xF3]

        for(i in 0 .. height - 1) {
            int index = (((yBit % 32) + i) * 64 + (xBit % 64)) / 8

            memory.setByte(index as short, sprite[i])
        }

        if (DUMP_MEMORY) dumpGraphicsSegment()

        byte[] buffer = new byte[MAX_SPRITE_HEIGHT]

        when:
        instance.fillPaintBuffer(xBit, yBit, buffer, height)

        then:
        assertBufferContent(buffer, sprite, height)

        if (DUMP_MEMORY) dumpBuffer(buffer, height)

        where:
        xBit | yBit | height
        // top left corner
        0    | 0    | 1
        0    | 0    | 5
        0    | 0    | 8
        64   | 32   | 3 // wrapping

        // bottom left corner
        0    | 31   | 1
        0    | 27   | 5
        0    | 24   | 8

        // middle
        8    | 1    | 3

        // top right corner
        56   | 0    | 1
        56   | 0    | 5
        56   | 0    | 8

        // bottom right corner
        56   | 30   | 2
        56   | 24   | 8

    }

    def "should properly extract painted area from memory (x halved)"() {
        given:

        int xBit = 4
        int yBit = 0
        int height = 3

        byte[] sprite = [0x12, 0x34, 0x56]

        memory.setByte(0 as short, 0x01 as byte)
        memory.setByte(1 as short, 0x20 as byte)

        memory.setByte(8 as short, 0x03 as byte)
        memory.setByte(9 as short, 0x40 as byte)

        memory.setByte(16 as short, 0x05 as byte)
        memory.setByte(17 as short, 0x60 as byte)

        if (DUMP_MEMORY) dumpGraphicsSegment()

        byte[] buffer = new byte[MAX_SPRITE_HEIGHT]

        when:
        instance.fillPaintBuffer(xBit, yBit, buffer, height)

        then:
        assertBufferContent(buffer, sprite, height)

        if (DUMP_MEMORY) dumpBuffer(buffer, height)
    }

    def "should properly extract painted area from memory (x halved, xy wrapped)"() {
        given:

        int xBit = 7 * 8 + 4
        int yBit = 31
        int height = 2

        byte[] sprite = [0x12, 0x34]

        memory.setByte(255 as short, 0x01 as byte)
        memory.setByte(31 * 8 as short, 0x20 as byte)

        memory.setByte(7 as short, 0x03 as byte)
        memory.setByte(0 as short, 0x40 as byte)

        if (DUMP_MEMORY) dumpGraphicsSegment()

        byte[] buffer = new byte[MAX_SPRITE_HEIGHT]

        when:
        instance.fillPaintBuffer(xBit, yBit, buffer, height)

        then:
        assertBufferContent(buffer, sprite, height)

        if (DUMP_MEMORY) dumpBuffer(buffer, height)
    }

    def "should properly extract painted area from memory (x 5/8)"() {
        given:

        int xBit = 6 * 8 + 5
        int yBit = 32 - 3
        int height = 3

        byte[] sprite = [0x12, 0x34, 0x56]

        // 0x1200 >> 5
        memory.setByte(29 * 8 + 6 as short, 0x00 as byte)
        memory.setByte(29 * 8 + 7 as short, 0x90 as byte)

        // 0x3400 >> 5
        memory.setByte(30 * 8 + 6 as short, 0x01 as byte)
        memory.setByte(30 * 8 + 7 as short, 0xA0 as byte)

        // 0x5600 >> 5
        memory.setByte(31 * 8 + 6 as short, 0x02 as byte)
        memory.setByte(31 * 8 + 7 as short, 0xB0 as byte)

        if (DUMP_MEMORY) dumpGraphicsSegment()

        byte[] buffer = new byte[MAX_SPRITE_HEIGHT]

        when:
        instance.fillPaintBuffer(xBit, yBit, buffer, height)

        then:
        assertBufferContent(buffer, sprite, height)

        if (DUMP_MEMORY) dumpBuffer(buffer, height)
    }

    @Unroll
    def "should xor buffers properly in a #title case"() {
        given:
        instance.spriteBuffer = sprite;
        instance.paintBuffer = bg;

        when:
        byte xorResult = instance.xorBuffers(0, 0, 2) //TODO: clipping

        then:
        assertBufferContent(instance.resultBuffer, result as byte[], 2)
        xorResult == gfxChange

        where:
        sprite       | bg           || result       | gfxChange | title
        [0x12, 0x34] | [0x56, 0x78] || [0x44, 0x4C] | GC_MIX    | "mixed"
        [0x00, 0x00] | [0xFF, 0xFF] || [0xFF, 0xFF] | GC_NOOP   | "no collision 1"
        [0x00, 0x00] | [0x00, 0x00] || [0x00, 0x00] | GC_NOOP   | "no collision 2"
        [0xFF, 0xFF] | [0x00, 0x00] || [0xFF, 0xFF] | GC_DRAW   | "no collision 3"
        [0x00, 0x01] | [0xFF, 0xFF] || [0xFF, 0xFE] | GC_ERASE  | "single collision"
        [0xFF, 0xFF] | [0xFF, 0xFF] || [0x00, 0x00] | GC_ERASE  | "all collision"
    }

    //TODO: add cases with preexisting data (check if it's preserved)
    @Unroll
    def "should properly store painted area in memory (byte aligned: #xBit, #yBit, #height)"() {
        given:

        byte[] paintedBuffer = [0x12, 0x34]

        when:
        instance.storePaintBuffer(xBit, yBit, paintedBuffer, height)

        then:
        if (DUMP_MEMORY) dumpGraphicsSegment()

        for (int i in 0 .. memory.getSize() - 1) {
            if (i == 0) {
                assert memory.getByte(i as short) == 0x12 as byte
            } else if (i == 8) {
                assert memory.getByte(i as short) == 0x34 as byte
            } else {
                assert memory.getByte(i as short) == 0x0 as byte
            }
        }

        where:
        xBit | yBit | height
        0    | 0    | 2
    }

    //TODO: add cases with preexisting data (check if it's preserved)
    def "should properly store painted area in memory (x 5/8)"() {
        given:
        int xBit = 6 * 8 + 5
        int yBit = 32 - 3
        int height = 3

        byte[] buffer = [0x12, 0x34, 0x56]

        when:
        instance.storePaintBuffer(xBit, yBit, buffer, height)

        then:
        if (DUMP_MEMORY) dumpGraphicsSegment()

        for (int i in 0 .. memory.getSize() - 1) {

            if (i == 29 * 8 + 6) {
                // 0x1200 >> 5 first byte
                assert memory.getByte(i as short) == 0x00 as byte

            } else if (i == 29 * 8 + 7) {
                // 0x1200 >> 5 second byte
                assert memory.getByte(i as short) == 0x90 as byte

            } else if (i == 30 * 8 + 6) {
                // 0x3400 >> 5 first byte
                assert memory.getByte(i as short) == 0x01 as byte

            } else if (i == 30 * 8 + 7) {
                // 0x3400 >> 5 second byte
                assert memory.getByte(i as short) == 0xA0 as byte

            } else if (i == 31 * 8 + 6) {
                // 0x5600 >> 5 first byte
                assert memory.getByte(i as short) == 0x02 as byte

            } else if (i == 31 * 8 + 7) {
                // 0x5600 >> 5 second byte
                assert memory.getByte(i as short) == 0xB0 as byte

            } else {
                assert memory.getByte(i as short) == 0x0 as byte
            }
        }
    }

    void dumpGraphicsSegment() {
        for (int i = 0; i < 256; ++i) {
            print toHexString(memory.getByte(i as short)) + " "

            if (i % 8 == 7) {
                println ""
            }
        }

        println "--"
    }

    static void dumpBuffer(byte[] buffer, int height) {
        for (int i = 0; i < height; ++i) {
            print toHexString(buffer[i]) + " "
        }
        println "\n"
    }
}
