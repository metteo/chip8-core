package net.novaware.chip8.core.util

import spock.lang.Specification

import java.util.concurrent.TimeUnit

import static java.lang.System.nanoTime
import static net.novaware.chip8.core.util.SleepUtil.sleepNanos

class SleepUtilSpec extends Specification {

    /**
     * Very inaccurate test. Checks only that the method executes.
     */
    def "should sleep"() {
        given:
        def sleepTime = TimeUnit.MILLISECONDS.toNanos(4)

        when:
        def start = nanoTime()
        sleepNanos(sleepTime)
        def end = nanoTime()

        then:
        end - start > 0
    }
}
