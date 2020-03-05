package net.novaware.chip8.core.cpu.unit

import net.novaware.chip8.core.cpu.register.RegisterModule
import spock.lang.Specification
import spock.lang.Unroll

import static net.novaware.chip8.core.cpu.CpuState.*

class PowerMgmtSpec extends Specification {

    def cpuState = RegisterModule.provideCpuState()
    
    def instance = new PowerMgmt(cpuState)

    @Unroll
    def "should go to sleep when requested (from #prevState)"() {
        given:
        cpuState.set(prevState.value())
        assert instance.getState() == prevState

        when:
        instance.sleep()

        then:
        instance.getState() == SLEEP

        where:
        prevState << [OPERATING, HALT, STOP_CLOCK, SLEEP]
    }

    def "should wake up if it was sleeping"() {
        given:
        cpuState.set(SLEEP.value())
        assert instance.getState() == SLEEP

        when:
        instance.wakeUp()

        then:
        instance.getState() == OPERATING
    }

    @Unroll
    def "should not wake up from #prevState"() {
        given:
        cpuState.set(prevState.value())
        assert instance.getState() == prevState

        when:
        instance.wakeUp()

        then:
        instance.getState() == prevState

        where:
        prevState << [OPERATING, HALT, STOP_CLOCK]
    }

    def "should halt when requested"() {
        given:
        cpuState.set(OPERATING.value())
        assert instance.getState() == OPERATING

        when:
        instance.halt()

        then:
        instance.getState() == HALT
    }

    @Unroll
    def "should not halt from #prevState"() {
        given:
        cpuState.set(prevState.value())
        assert instance.getState() == prevState

        when:
        instance.halt()

        then:
        instance.getState() == prevState

        where:
        prevState << [HALT, STOP_CLOCK, SLEEP]
    }

    def "should cont when requested"() {
        given:
        cpuState.set(HALT.value())
        assert instance.getState() == HALT

        when:
        instance.cont()

        then:
        instance.getState() == OPERATING
    }

    @Unroll
    def "should not cont from #prevState"() {
        given:
        cpuState.set(prevState.value())
        assert instance.getState() == prevState

        when:
        instance.cont()

        then:
        instance.getState() == prevState

        where:
        prevState << [OPERATING, STOP_CLOCK, SLEEP]
    }

    def "should stop clock when requested"() {
        given:
        cpuState.set(OPERATING.value())
        assert instance.getState() == OPERATING

        when:
        instance.stopClock()

        then:
        instance.getState() == STOP_CLOCK
    }

    @Unroll
    def "should not stop clock from #prevState"() {
        given:
        cpuState.set(prevState.value())
        assert instance.getState() == prevState

        when:
        instance.stopClock()

        then:
        instance.getState() == prevState

        where:
        prevState << [HALT, STOP_CLOCK, SLEEP]
    }

    def "should start clock when requested"() {
        given:
        cpuState.set(STOP_CLOCK.value())
        assert instance.getState() == STOP_CLOCK

        when:
        instance.startClock()

        then:
        instance.getState() == OPERATING
    }

    @Unroll
    def "should not start clock from #prevState"() {
        given:
        cpuState.set(prevState.value())
        assert instance.getState() == prevState

        when:
        instance.startClock()

        then:
        instance.getState() == prevState

        where:
        prevState << [OPERATING, HALT, SLEEP]
    }
}
