package net.novaware.chip8.core.memory

import net.novaware.chip8.core.cpu.register.Registers
import spock.lang.Specification

class MappedMemorySpec extends Specification {

    Registers registers = new Registers()

    MemoryMap memoryMap = new MemoryMap(registers.getVariables())

    MappedMemory memory = memoryMap.getCpuMemory()

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
        memory.getName() == "CPU"
    }

    def "should save and retrieve byte of data (end of large memory)"() {
        given:
        short address = 0x007 //TODO: update data to really write to end
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
        memoryPart == [(byte)0x12, (byte)0x34]
    }
}