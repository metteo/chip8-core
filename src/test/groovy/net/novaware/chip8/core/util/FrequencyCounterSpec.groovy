package net.novaware.chip8.core.util

import spock.lang.Specification

import java.util.function.LongSupplier

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
        def nanoTime = Mock(LongSupplier)
        nanoTime.asLong >>> [
                100_000_000_100_000,
                100_000_000_200_000,
                100_000_000_300_000,
        ]

        int publishNumber = 0
        def calculatedFrequency = []

        def instance = new FrequencyCounter(nanoTime, 2, 0.1)
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
        calculatedFrequency[1] == 1900
    }

    def "should not divide by 0"() {
        given:
        def nanoTime = Mock(LongSupplier)
        nanoTime.asLong >>> 1;

        def instance = new FrequencyCounter(nanoTime, 2, 0.1)
        instance.initialize()

        when:
        for (def i in 0..2) {
            instance.takeASample()
        }

        then:
        noExceptionThrown()
    }
}
