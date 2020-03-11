package net.novaware.chip8.core.cpu.register

import spock.lang.Specification

class TribbleRegisterSpec extends Specification {

    def register = new TribbleRegister("PC")

    def "should trigger callback on value change"() {
        given:
        Register<TribbleRegister> reportedRegister = null

        register.setCallback({ r -> reportedRegister = r})

        when:
        register.increment(1)

        then:
        reportedRegister.getName() == "PC"
        reportedRegister.is(register)
        register.get() == 1 as short
    }

    def "should ignore null callback"() {
        given:
        register.setCallback(null)

        when:
        register.increment(1)

        then:
        noExceptionThrown()
    }

    def "should truncate value over 0xFFF"() {
        given:
        register.set(0xABCD)

        expect:
        register.getAsInt() == 0xBCD
    }

    def "should support decrement properly when close to 0"() {
        given:
        register.set(0x1)

        when:
        register.increment(-3)

        then:
        register.getAsInt() == 0xFFE
    }
}
