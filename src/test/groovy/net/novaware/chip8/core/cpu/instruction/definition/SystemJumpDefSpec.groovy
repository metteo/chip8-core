package net.novaware.chip8.core.cpu.instruction.definition

import spock.lang.Specification

class SystemJumpDefSpec extends Specification {

    def instance = new SystemJumpDef()

    def "should decode jump to system location" () {
        given:
        short instruction = 0x0123

        expect:
        instance.isRecognized(instruction)
        (short) 0x0000 == instance.getOpCode()
                1      == instance.getParamCount()
        (short) 0x0123 == instance.getParam(0, instruction)
    }

    def "should ignore clear screen" () {
        given:
        short instruction = 0x00E0

        when:
        instance.getParam(0, instruction)

        then:
        !instance.isRecognized(instruction)
        thrown(AssertionError)
    }

    def "should ignore return from subroutine" () {
        given:
        short instruction = 0x00EE

        when:
        instance.getParam(0, instruction)

        then:
        !instance.isRecognized(instruction)
        thrown(AssertionError)
    }

    def "should ignore jump to location" () {
        given:
        short instruction = 0x1234

        when:
        instance.getParam(0, instruction)

        then:
        !instance.isRecognized(instruction)
        thrown(AssertionError)
    }
}
