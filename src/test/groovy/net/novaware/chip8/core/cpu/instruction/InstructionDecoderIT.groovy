package net.novaware.chip8.core.cpu.instruction

import net.novaware.chip8.core.cpu.register.WordRegister
import spock.lang.Specification

import static net.novaware.chip8.core.cpu.register.RegistersHelper.newRegisters

class InstructionDecoderIT extends Specification {

    def registers = newRegisters()

    InstructionDecoder decoder = new InstructionDecoder(
            registers.getCurrentInstruction(),
            registers.getDecodedInstruction(),
            new InstructionRegistry()
    )

    def "should properly decode an instruction"() {
        given:
        registers.getCurrentInstruction().set(0xD123)

        when:
        decoder.decode()

        then:
        WordRegister[] decoded = registers.getDecodedInstruction()
        decoded[0].get() == (short)0xD000
        decoded[1].get() == (short)0x0001
        decoded[2].get() == (short)0x0002
        decoded[3].get() == (short)0x0003

    }

    def "should throw an exception for unknown instruction"() {
        given:
        registers.getCurrentInstruction().set(0xE123)

        when:
        decoder.decode()

        then:
        thrown(RuntimeException)
    }
}
