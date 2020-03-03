package net.novaware.chip8.core.cpu.unit

import net.novaware.chip8.core.cpu.register.ByteRegister
import spock.lang.Specification

import static net.novaware.chip8.core.cpu.register.RegistersHelper.newRegisters

class TimerSpec extends Specification {

    def "should not decrement the timer when 0"() {
        given:
        def registers = newRegisters()
        Timer timer = new Timer(registers.getVariables(), registers.getDelay())
        timer.init()

        when:
        timer.maybeDecrementValue()

        then:
        registers.getDelay().get() == 0x0 as byte
    }

    def "should decrement the timer to 0 from 1, after multiple invocations"() {
        given:
        def registers = newRegisters()
        Timer timer = new Timer(registers.getVariables(), registers.getDelay())
        timer.init()

        registers.getDelay().set(0x1 as byte)

        when:
        timer.maybeDecrementValue()
        timer.maybeDecrementValue()
        timer.maybeDecrementValue()

        then:
        registers.getDelay().get() == 0x0 as byte
    }

    def "should decrement the timer by 1 every invocation"() {
        given:
        def registers = newRegisters()
        Timer timer = new Timer(registers.getVariables(), registers.getDelay())
        timer.init()

        def delay = registers.getDelay()

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
        def registers = newRegisters()

        ByteRegister sound = registers.getSound()
        ByteRegister soundOn = registers.getSoundOn()

        Timer timer = new Timer(registers.getVariables(), sound, soundOn)
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
        def registers = newRegisters()

        ByteRegister sound = registers.getSound()
        sound.set(1)

        ByteRegister soundOn = registers.getSoundOn()
        soundOn.set(1)

        Timer timer = new Timer(registers.getVariables(), sound, soundOn)
        timer.init()

        when:
        timer.maybeDecrementValue()

        then:
        sound.getAsInt() == 0
        soundOn.getAsInt() == 0
    }
}
