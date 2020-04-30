package net.novaware.chip8.core.cpu.register

import spock.lang.Specification

class WordRegisterSpec extends Specification {

    def register = new WordRegister("IR")

    def "should trigger callback on value change"() {
        given:
        Register<WordRegister> reportedRegister = null

        register.subscribe({ r -> reportedRegister = r})

        when:
        register.set(1)

        then:
        reportedRegister.getName() == "IR"
        reportedRegister.is(register)
        register.get() == 1 as short
    }

    def "should ignore null callback"() {
        given:
        register.subscribe(null)

        when:
        register.set(1)

        then:
        noExceptionThrown()
        register.getAsInt() == 1
    }
}
