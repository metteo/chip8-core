package net.novaware.chip8.core.cpu.register

import spock.lang.Specification

class ByteRegisterSpec extends Specification {

    def register = new ByteRegister("V1")

    def "should trigger callback on value change"() {
        given:
        Register<WordRegister> reportedRegister = null

        register.setCallback({ r -> reportedRegister = r})

        when:
        register.set(1)

        then:
        reportedRegister.getName() == "V1"
        reportedRegister.is(register)
    }

    def "should ignore null callback"() {
        given:
        register.setCallback(null)

        when:
        register.set(2)

        then:
        noExceptionThrown()
    }
}
