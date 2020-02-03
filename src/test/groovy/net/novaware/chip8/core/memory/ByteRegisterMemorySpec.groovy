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
        Memory memory = new ByteRegisterMemory("V Register", register)

        then:
        memory.getByte(0 as short) == 0xAB as byte
        memory.getName() == "V Register"
        memory.getSize() == 1

        and: "allow updates using memory"
        memory.setByte(0 as short, 0xCD as byte)
        register.getAsInt() == 0xCD
    }

    //TODO: cases for throwing exception, word, byte array
}
