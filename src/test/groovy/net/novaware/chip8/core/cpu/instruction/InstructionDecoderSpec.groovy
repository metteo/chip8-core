package net.novaware.chip8.core.cpu.instruction

import net.novaware.chip8.core.cpu.instruction.definition.AddressOnlyDef
import spock.lang.Specification

import static net.novaware.chip8.core.cpu.register.RegistersHelper.newRegisters

class InstructionDecoderSpec extends Specification {

    def registers = newRegisters()

    def registry = Mock(InstructionRegistry)

    InstructionDecoder instance = new InstructionDecoder(
            registers.getCurrentInstruction(),
            registers.getDecodedInstruction(),
            registry
    )

    def "should decode unseen instruction"() {
        given:
        registers.getCurrentInstruction().set(0x1234)

        def instructionDef = new AddressOnlyDef(InstructionType.Ox1MMM)

        when:
        instance.decode()

        then:
        1 * registry.getDefinition(0x1234) >> instructionDef
        with(registers.getDecodedInstruction()) {
            it[0].getAsInt() == 0x1000
            it[1].getAsInt() == 0x0234
            it[2].getAsInt() == 0x0000
            it[3].getAsInt() == 0x0000
        }
    }

    def "should use cache for previously seen instruction"() {
        given:
        registers.getCurrentInstruction().set(0x1234)

        def instructionDef = new AddressOnlyDef(InstructionType.Ox1MMM)

        when:
        instance.decode()
        instance.decode()

        then:
        1 * registry.getDefinition(0x1234) >> instructionDef
        with(registers.getDecodedInstruction()) {
            it[0].getAsInt() == 0x1000
            it[1].getAsInt() == 0x0234
            it[2].getAsInt() == 0x0000
            it[3].getAsInt() == 0x0000
        }
    }
}
