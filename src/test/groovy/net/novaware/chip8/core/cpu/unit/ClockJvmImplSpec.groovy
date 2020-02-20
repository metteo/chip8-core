package net.novaware.chip8.core.cpu.unit

import spock.lang.Specification

class ClockJvmImplSpec extends Specification {

    boolean targetExecuted

    def "should construct properly"() {
        when:
        Clock instance = new ClockJvmImpl()
        instance.setFrequency(500)
        instance.setTarget(this.&sampleTarget as Runnable)

        then:
        instance.getFrequency() == 500
        instance.getTarget() != null
        !targetExecuted
    }

    void sampleTarget() {
        targetExecuted = true
    }
}
