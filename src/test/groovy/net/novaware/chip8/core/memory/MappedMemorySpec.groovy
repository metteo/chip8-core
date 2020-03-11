package net.novaware.chip8.core.memory

import net.novaware.chip8.core.cpu.register.RegisterModule
import spock.lang.Specification

import static net.novaware.chip8.core.util.UnsignedUtil.ubyte
import static net.novaware.chip8.core.util.UnsignedUtil.ushort

class MappedMemorySpec extends Specification {

    MappedMemory memory = MappedMemoryHelper.newMappedMemory(RegisterModule.provideVariables())

    def "should properly size memory segments" () {
        expect:
        memory.entries[0].ref.getSize() == MemoryModule.BOOTLOADER_ROM_SIZE
        memory.entries[1].ref.getSize() == MemoryModule.PROGRAM_SIZE
        memory.entries[2].ref.getSize() == MemoryModule.STACK_SIZE
        memory.entries[3].ref.getSize() == MemoryModule.BOOTLOADER_RAM_SIZE
        memory.entries[4].ref.getSize() == MemoryModule.VARIABLES_SIZE
        memory.entries[5].ref.getSize() == MemoryModule.DISPLAY_IO_SIZE
        memory.getSize() == 4096
    }

    def "should return correct segment with given address" () {
        when:
        def segment = memory.getSegment(address).ref

        then:
        segment.getName() == expectedSegment

        where:

        address                            || expectedSegment
        MemoryModule.BOOTLOADER_ROM_START  || "Bootloader ROM"
        (short) 0x002                      || "Bootloader ROM"
        MemoryModule.BOOTLOADER_ROM_END    || "Bootloader ROM"

        MemoryModule.PROGRAM_START         || "Program"
        (short) 0x203                      || "Program"
        MemoryModule.PROGRAM_END           || "Program"

        //TODO: add more tests that verify the whole mapped memoryMap
    }

    def "should translate absolute address into segment space" () {
        given:
        short address = 0x202

        def segment = memory.getSegment(address)

        expect:
        (short) 0x002 == memory.translateToSegmentAddress(segment, address)
    }

    //TODO: refactor to allow testing, (mocking of memory segments)
    def "should clear display and maybe other memory segments"() {
        given:
        for (int i = MemoryModule.DISPLAY_IO_START; i <= MemoryModule.DISPLAY_IO_END; ++i) {
            memory.setByte(ushort(i), ubyte(0xFF))
        }

        when:
        memory.clear()

        then:
        for (int i = MemoryModule.DISPLAY_IO_START; i <= MemoryModule.DISPLAY_IO_END; ++i) {
            assert memory.getByte(ushort(i)) == 0 as byte
        }
    }

    def "should save and retrieve byte of data"() {
        given:
        short address = 0x000
        byte data = 64

        short address2 = 0x000
        byte data2 = 65

        byte[] source = [data]
        byte[] dest = new byte[1]

        when:
        memory.setBytes(address, source, 1)
        memory.getBytes(address, dest, 1)

        memory.setByte(address2, data2)
        def dest2 = memory.getByte(address2)

        then:
        dest[0] == data
        dest2 == data2
        memory.getName() == "MMU"
    }

    def "should save and retrieve byte of data (not in the beginning)"() {
        given:
        short address = 0x007
        byte data = 42

        short address2 = 0x006
        byte data2 = 43

        byte[] source = [data]
        byte[] dest = new byte[2]

        when:
        memory.setBytes(address, source, 1)
        memory.getBytes(address, dest, 1)

        memory.setByte(address2, data2)
        def dest2 = memory.getByte(address2)

        then:
        dest[0] == data
        dest2 == data2
    }

    def "should properly fetch instruction"() {
        given:
        short address = 0x0200
        byte[] memoryPart = [(byte)0x12, (byte)0x34]

        short instruction = 0x1234 //invalid but good for this test

        memory.setBytes(address, memoryPart, 2)

        when:
        def output = memory.getWord(address)

        then:
        output == instruction
    }

    def "should properly fetch instruction 0x00E0"() {
        given:
        short address = 0x0200
        byte[] memoryPart = [(byte)0x00, (byte)0xE0]

        short instruction = 0x00E0

        memory.setBytes(address, memoryPart, 2)

        when:
        def output = memory.getWord(address)

        then:
        output == instruction
    }


    def "should properly set instruction"() {
        given:
        short address = 0x0200
        byte[] memoryPart = new byte[2]

        short instruction = 0x1234 //invalid but good for this test

        memory.setWord(address, instruction)

        when:
        memory.getBytes(address, memoryPart, 2)

        then:
        memoryPart == [(byte)0x12, (byte)0x34] as byte[]
    }

    def "should throw IAE when address is outside mapped range"() {
        given:
        short address = 0x1234

        when:
        memory.getByte(address)

        then:
        thrown(IllegalArgumentException)
    }
}
