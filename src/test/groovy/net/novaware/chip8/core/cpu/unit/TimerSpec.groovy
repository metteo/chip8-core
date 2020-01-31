package net.novaware.chip8.core.cpu.unit

import net.novaware.chip8.core.cpu.register.ByteRegister
import spock.lang.Specification

class TimerSpec extends Specification {

    ByteRegister delay = new ByteRegister("DT")

    Timer timer = new Timer(delay, null)

    def "should not decrement the timer when 0"() {
        when:
        timer.maybeDecrementValue()

        then:
        delay.get() == 0x0 as byte
    }

    def "should decrement the timer to 0 from 1, after multiple invocations"() {
        given:
        delay.set(0x1 as byte)

        when:
        timer.maybeDecrementValue()
        timer.maybeDecrementValue()
        timer.maybeDecrementValue()

        then:
        delay.get() == 0x0 as byte
    }

    def "should decrement the timer by 1 every invocation"() {
        given:
        delay.set(0x3 as byte)

        expect:
        delay.get() == 0x3 as byte
        timer.maybeDecrementValue()
        delay.get() == 0x2 as byte
        timer.maybeDecrementValue()
        delay.get() == 0x1 as byte
        timer.maybeDecrementValue()
        delay.get() == 0x0 as byte
        timer.maybeDecrementValue()
        delay.get() == 0x0 as byte
    }
}
