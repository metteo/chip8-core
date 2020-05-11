package net.novaware.chip8.core.util

import spock.lang.Specification

class FrequencyCounterSpec extends Specification {

    def "should construct and initialize without exceptions"() {
        when:
        def instance = new FrequencyCounter(5, 0.1)
        instance.initialize()

        then:
        noExceptionThrown()
    }

    def "should calculate frequency correctly"() {
        given:
        int publishNumber = 0
        def calculatedFrequency = []

        def instance = new FrequencyCounter(2, 0.1)
        instance.initialize()
        instance.subscribe({fc ->
            publishNumber++
            calculatedFrequency.add(fc.getFrequency())
        })

        when:
        for (def i in 0..2) {
            instance.takeASample()
            instance.maybePublish()
        }

        then:
        publishNumber == 2
        calculatedFrequency[0] == 0
        //FIXME: how to calculate the correct result for assertion?
    }
}
