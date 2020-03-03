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

    def "should properly decode MVI / LD I instruction"() {
        given:
        registers.getCurrentInstruction().set(0xA123)

        when:
        decoder.decode()

        then:
        WordRegister[] decoded = registers.getDecodedInstruction()
        decoded[0].get() == (short)0xA000
        decoded[1].get() == (short)0x0123
        decoded[2].get() == (short)0x0000
        decoded[3].get() == (short)0x0000

    }

    def "should properly decode JMP instruction"() {
        given:
        registers.getCurrentInstruction().set(0x1234)

        when:
        decoder.decode()

        then:
        WordRegister[] decoded = registers.getDecodedInstruction()
        decoded[0].get() == (short)0x1000
        decoded[1].get() == (short)0x0234
        decoded[2].get() == (short)0x0000
        decoded[3].get() == (short)0x0000

    }

    def "should properly decode SE X KK instruction"() {
        given:
        registers.getCurrentInstruction().set(0x3456)

        when:
        decoder.decode()

        then:
        WordRegister[] decoded = registers.getDecodedInstruction()
        decoded[0].get() == (short)0x3000
        decoded[1].get() == (short)0x0004
        decoded[2].get() == (short)0x0056
        decoded[3].get() == (short)0x0000

    }
}
