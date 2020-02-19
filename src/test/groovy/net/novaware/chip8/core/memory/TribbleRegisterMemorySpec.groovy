package net.novaware.chip8.core.memory

import net.novaware.chip8.core.cpu.register.TribbleRegister
import spock.lang.Specification
import spock.lang.Unroll

import java.util.stream.IntStream

class TribbleRegisterMemorySpec extends Specification {

    @Unroll
    def "should construct properly with #n registers"() {
        given:
        TribbleRegister[] registers = IntStream.range(0, n)
                .mapToObj({ i ->
                    def reg = new TribbleRegister("S" + i)
                    reg.set(i + 1)
                    reg
                })
                .toArray({s -> new TribbleRegister[s]})


        when:
        Memory memory = new TribbleRegisterMemory("S Registers", registers)

        then:

        for(i in 0..n-2) {
            //assert memory.getByte(i as short) == (i + 1) as byte

            if (i % 2 == 0) {
                assert memory.getWord(i as short) == (i / 2 + 1) as short
            }

        }

        memory.getName() == "S Registers"
        memory.getSize() == n * 2

        where:
        n << [1, 7, 8, 24]
    }

    def "should properly store and load a word (aligned)"() {
        given:
        TribbleRegister register = new TribbleRegister("S0")
        register.set(0xABCD)

        when:
        Memory memory = new TribbleRegisterMemory("S Registers", register)

        then:
        memory.getWord(0 as short) == 0x0BCD as short
        memory.getName() == "S Registers"
        memory.getSize() == 2

        and: "allow updates using memory"
        memory.setWord(0 as short, 0xBCDE as short)
        register.getAsInt() == 0x0CDE
    }

    def "should block loading the word using unaligned address"() {
        given:
        TribbleRegister register1 = new TribbleRegister("S0")
        register1.set(0xABCD)
        TribbleRegister register2 = new TribbleRegister("S1")
        register2.set(0x1234)

        when:
        Memory memory = new TribbleRegisterMemory("S Registers", register1, register2)
        memory.getWord(1 as short)

        then:
        thrown(IllegalArgumentException)
    }

    def "should block storing the word using unaligned address"() {
        given:
        TribbleRegister register1 = new TribbleRegister("S0")
        register1.set(0xABCD)
        TribbleRegister register2 = new TribbleRegister("S1")
        register2.set(0x1234)

        when:
        Memory memory = new TribbleRegisterMemory("S Registers", register1, register2)
        memory.setWord(1 as short, 0xABCD as short)

        then:
        thrown(IllegalArgumentException)
    }

    def "should block store from too high address"() {
        given:
        TribbleRegister register1 = new TribbleRegister("S0")
        register1.set(0xABCD)

        when:
        Memory memory = new TribbleRegisterMemory("S Registers", register1)
        memory.setWord(2 as short, 0xABCD as short)

        then:
        thrown(IllegalArgumentException)
    }

    def "should throw when storing byte"() {
        given:
        TribbleRegister register = new TribbleRegister("S0")
        Memory memory = new TribbleRegisterMemory("Stack Registers", register)

        when:
        memory.setByte(0 as short, 0xCD as byte)

        then:
        thrown(UnsupportedOperationException)
    }

    def "should throw when getting byte"() {
        given:
        TribbleRegister register = new TribbleRegister("S0")
        Memory memory = new TribbleRegisterMemory("Stack Registers", register)

        when:
        memory.getByte(0 as short)

        then:
        thrown(UnsupportedOperationException)
    }

    def "should throw when storing bytes"() {
        given:
        TribbleRegister register = new TribbleRegister("S0")
        Memory memory = new TribbleRegisterMemory("Stack Registers", register)

        when:
        memory.setBytes(0 as short, [0xCD as byte] as byte[], 1)

        then:
        thrown(UnsupportedOperationException)
    }

    def "should throw when getting bytes"() {
        given:
        TribbleRegister register = new TribbleRegister("S0")
        Memory memory = new TribbleRegisterMemory("Stack Registers", register)

        when:
        memory.getBytes(0 as short, new byte[1], 1)

        then:
        thrown(UnsupportedOperationException)
    }
}
