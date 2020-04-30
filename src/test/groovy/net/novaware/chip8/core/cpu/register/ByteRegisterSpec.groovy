package net.novaware.chip8.core.cpu.register

import spock.lang.Specification

class ByteRegisterSpec extends Specification {

    def register = new ByteRegister("V1")

    def "should trigger callback on value change"() {
        given:
        Register<ByteRegister> reportedRegister = null

        register.subscribe({ r -> reportedRegister = r})

        when:
        register.set(1)

        then:
        reportedRegister.getName() == "V1"
        reportedRegister.is(register)
        register.getAsInt() == 1
    }

    def "should ignore null callback"() {
        given:
        register.subscribe(null)

        when:
        register.set(2 as short)

        then:
        noExceptionThrown()
        register.get() == 2 as short
    }
}
