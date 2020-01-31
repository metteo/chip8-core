package net.novaware.chip8.core.cpu.instruction

import spock.lang.Specification

import static InstructionType.Ox1MMM

class InstructionTypeSpec extends Specification {

    def "should return null on unrecognized opcode"() {
        given:
        int opcode = 0xFEEE

        expect:
        InstructionType.valueOf(opcode) == null
        InstructionType.valueOf((short)opcode) == null
    }

    def "should return instruction on valid opcode"() {
        given:
        int opcode = 0x1000

        expect:
        InstructionType.valueOf(opcode) == Ox1MMM
        InstructionType.valueOf((short)opcode) == Ox1MMM
    }
}
