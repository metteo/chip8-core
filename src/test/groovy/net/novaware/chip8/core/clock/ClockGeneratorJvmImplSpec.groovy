package net.novaware.chip8.core.clock


import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class ClockGeneratorJvmImplSpec extends Specification {

    String threadName

    def "should schedule and execute simple task on separate thread"() {
        given:
        def conditions = new PollingConditions(timeout: 1, initialDelay: 0.1, factor: 2.0)

        ClockGenerator instance = new ClockGeneratorJvmImpl("test")

        when:
        ClockGenerator.Handle handle = instance.schedule(this.&sampleTarget)

        then:
        handle != null

        conditions.eventually {
            println threadName
            assert threadName == "Chip8-test-Clock"
        }
    }

    void sampleTarget() {
        threadName = Thread.currentThread().getName()
    }

    def "should repeat a simple task on separate thread"() {
        given:
        def conditions = new PollingConditions(timeout: 1, initialDelay: 0.1, factor: 2.0)

        def threadNames = new ArrayList<String>()
        def execTimes = new ArrayList<Long>();

        ClockGenerator instance = new ClockGeneratorJvmImpl("test")

        when:
        ClockGenerator.Handle handle = instance.schedule({_ ->
            threadNames.add(Thread.currentThread().getName())
            execTimes.add(System.nanoTime())
        }, 500 /* Hz */)

        then:
        handle != null

        conditions.eventually {
            assert execTimes.size() > 30
            // TODO: consider checking the difference between nanostamps

            def uniqueThreadNames = threadNames.unique()
            assert uniqueThreadNames.size() == 1
            assert uniqueThreadNames[0] == "Chip8-test-Clock"
        }
    }
}
