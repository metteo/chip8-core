package net.novaware.chip8.core.memory

import spock.lang.Specification

import static net.novaware.chip8.core.util.UnsignedUtil.uint

class SplittableMemorySpec extends Specification {

    SplittableMemory memory

    def "should save and retrieve byte of data"() {
        given:
        memory = new SplittableMemory(new PhysicalMemory("test", 2))

        short address = 0x000
        byte data = 64

        short address2 = 0x001
        byte data2 = 65

        byte[] source = [data]
        byte[] dest = new byte[1]

        when:
        memory.setBytes(address, source, 1)
        memory.setByte(address2, data2)

        memory.setSplit(2)

        memory.getBytes(address, dest, 1)
        def dest2 = memory.getByte(address2)

        then:
        dest[0] == data
        dest2 == data2

        and:
        memory.getName() == "test"
        memory.getSize() == 2
    }

    def "should save and retrieve byte of data (end of large memory)"() {
        given:
        memory = new SplittableMemory(new PhysicalMemory("test", 8))

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

    def "should properly fetch word"() {
        given:
        memory = new SplittableMemory(new PhysicalMemory("test", 1024))

        short address = 0x0200
        byte[] memoryPart = [(byte)0x12, (byte)0x34]

        short instruction = 0x1234 //invalid but good for this test

        memory.setBytes(address, memoryPart, 2)
        memory.setSplit(uint(address) + 1)

        when:
        def output = memory.getWord(address)

        then:
        output == instruction
    }

    def "should properly fetch word 0x00E0"() {
        given:
        memory = new SplittableMemory(new PhysicalMemory("test", 1024))

        short address = 0x0200
        byte[] memoryPart = [(byte)0x00, (byte)0xE0]

        short instruction = 0x00E0

        memory.setBytes(address, memoryPart, 2)

        when:
        def output = memory.getWord(address)

        then:
        output == instruction
    }


    def "should properly set word"() {
        given:
        memory = new SplittableMemory(new PhysicalMemory("test", 1024))

        short address = 0x0200
        byte[] memoryPart = new byte[2]

        short instruction = 0x1234 //invalid but good for this test

        memory.setWord(address, instruction)

        when:
        memory.getBytes(address, memoryPart, 2)

        then:
        memoryPart == [(byte)0x12, (byte)0x34] as byte[]
    }

    def "should fail when storing byte in ROM"() {
        given:
        memory = new SplittableMemory(new PhysicalMemory("test", 1024))
        memory.setSplit(2) // bytes 0 & 1 are now RO

        when:
        memory.setByte(0 as short, 0xAB as byte)

        then:
        thrown(IllegalArgumentException)
    }

    def "should fail when storing word in ROM"() {
        given:
        memory = new SplittableMemory(new PhysicalMemory("test", 1024))
        memory.setSplit(2) // bytes 0 & 1 are now RO

        when:
        memory.setWord(addr as short, 0xABCD as short)

        then:
        thrown(IllegalArgumentException)

        where:
        addr << [0, 1]
    }

    def "should fail when storing bytes in ROM"() {
        given:
        memory = new SplittableMemory(new PhysicalMemory("test", 1024))
        memory.setSplit(2) // bytes 0 & 1 are now RO

        when:
        memory.setBytes(addr as short, [0xAB as byte, 0xCD as byte] as byte[], 2)

        then:
        thrown(IllegalArgumentException)

        where:
        addr << [0, 1]
    }
}
