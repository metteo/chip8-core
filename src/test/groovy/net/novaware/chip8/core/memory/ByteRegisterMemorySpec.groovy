package net.novaware.chip8.core.memory

import net.novaware.chip8.core.cpu.register.ByteRegister
import spock.lang.Specification
import spock.lang.Unroll

import java.util.stream.IntStream

class ByteRegisterMemorySpec extends Specification {

    @Unroll
    def "should construct properly with #n registers"() {
        given:
        ByteRegister[] registers = IntStream.range(0, n)
                .mapToObj({ i ->
                    def reg = new ByteRegister("V" + i)
                    reg.set(i + 1)
                    reg
                })
                .toArray({s -> new ByteRegister[s]})


        when:
        Memory memory = new ByteRegisterMemory("V Registers", registers)

        then:

        for(i in 0..n-1) {
            assert memory.getByte(i as short) == (i + 1) as byte

            if (memory.size > 1 && i < n - 2) {
                assert memory.getWord(i as short) == (((i + 1) << 8) | (i + 2)) as short
            }

        }

        memory.getName() == "V Registers"
        memory.getSize() == n

        where:
        n << [1, 7, 8, 16]
    }

    def "should properly store and load a byte of data"() {
        given:
        ByteRegister register = new ByteRegister("V0")
        register.set(0xAB)

        when:
        Memory memory = new ByteRegisterMemory("V Registers", register)

        then:
        memory.getByte(0 as short) == 0xAB as byte
        memory.getName() == "V Registers"
        memory.getSize() == 1

        and: "allow updates using memory"
        memory.setByte(0 as short, 0xCD as byte)
        register.getAsInt() == 0xCD
    }

    def "should throw when storing using too high address"() {
        given:
        ByteRegister register1 = new ByteRegister("V0")
        register1.set(0xAB as byte)

        when:
        Memory memory = new ByteRegisterMemory("V Registers", register1)
        memory.setByte(1 as short, 0xCD as byte)

        then:
        thrown(IllegalArgumentException)
    }

    def "should throw when storing word"() {
        given:
        ByteRegister register = new ByteRegister("V0")
        Memory memory = new ByteRegisterMemory("V Registers", register)

        when:
        memory.setWord(0 as short, 0xCDEF as short)

        then:
        thrown(UnsupportedOperationException)
    }

    def "should throw when storing bytes"() {
        given:
        ByteRegister register = new ByteRegister("V0")
        Memory memory = new ByteRegisterMemory("V Registers", register)

        when:
        memory.setBytes(0 as short, [0xCD as byte] as byte[], 1)

        then:
        thrown(UnsupportedOperationException)
    }

    def "should throw when getting bytes"() {
        given:
        ByteRegister register = new ByteRegister("V0")
        Memory memory = new ByteRegisterMemory("V Registers", register)

        when:
        memory.getBytes(0 as short, new byte[1], 1)

        then:
        thrown(UnsupportedOperationException)
    }
}
