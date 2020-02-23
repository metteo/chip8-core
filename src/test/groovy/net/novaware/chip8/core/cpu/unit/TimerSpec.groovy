package net.novaware.chip8.core.cpu.unit

import net.novaware.chip8.core.cpu.register.ByteRegister
import spock.lang.Specification

class TimerSpec extends Specification {

    def "should not decrement the timer when 0"() {
        given:
        ByteRegister delay = new ByteRegister("DT")
        Timer timer = new Timer(delay, null)
        timer.init()

        when:
        timer.maybeDecrementValue()

        then:
        delay.get() == 0x0 as byte
    }

    def "should decrement the timer to 0 from 1, after multiple invocations"() {
        given:
        ByteRegister delay = new ByteRegister("DT")
        Timer timer = new Timer(delay, null)
        timer.init()

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
        ByteRegister delay = new ByteRegister("DT")
        Timer timer = new Timer(delay, null)
        timer.init()

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

    def "should trigger output register when more than 1"() {
        given:
        ByteRegister sound = new ByteRegister("ST")
        ByteRegister soundOn = new ByteRegister("SO")

        Timer timer = new Timer(sound, soundOn)
        timer.init()

        when:
        sound.set(5)

        then:
        soundOn.getAsInt() == 1

        where:
        timerValue || outputValue
        5          || 1
        1          || 0
    }

    def "should zero out output register when reached 0"() {
        given:
        ByteRegister sound = new ByteRegister("ST")
        sound.set(1)

        ByteRegister soundOn = new ByteRegister("SO")
        soundOn.set(1)

        Timer timer = new Timer(sound, soundOn)
        timer.init()

        when:
        timer.maybeDecrementValue()

        then:
        sound.getAsInt() == 0
        soundOn.getAsInt() == 0
    }
}
