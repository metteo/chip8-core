package net.novaware.chip8.core.memory

import spock.lang.Specification

class PhysicalMemorySpec extends Specification {

    PhysicalMemory memory

    def "should save and retrieve byte of data"() {
        given:
        memory = new PhysicalMemory("test", 1)

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
        memory.getName() == "test"
    }

    def "should save and retrieve byte of data (end of large memory)"() {
        given:
        memory = new PhysicalMemory("test", 8)

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
        memory = new PhysicalMemory("test", 1024)

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
        memory = new PhysicalMemory("test", 1024)

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
        memory = new PhysicalMemory("test", 1024)

        short address = 0x0200
        byte[] memoryPart = new byte[2]

        short instruction = 0x1234 //invalid but good for this test

        memory.setWord(address, instruction)

        when:
        memory.getBytes(address, memoryPart, 2)

        then:
        memoryPart == [(byte)0x12, (byte)0x34] as byte[]
    }

    def "should throw IAException when reading outside of boundaries" () {
        given:
        memory = new PhysicalMemory("test", 2)

        when:
        memory.getByte(0x2 as short)

        then:
        thrown(IllegalArgumentException)
    }
}
