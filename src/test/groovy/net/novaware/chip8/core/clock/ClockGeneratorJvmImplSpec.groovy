package net.novaware.chip8.core.clock


import spock.lang.Specification

class ClockGeneratorJvmImplSpec extends Specification {

    boolean targetExecuted

    def "should construct properly"() {
        when:
        ClockGenerator instance = new ClockGeneratorJvmImpl()
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
