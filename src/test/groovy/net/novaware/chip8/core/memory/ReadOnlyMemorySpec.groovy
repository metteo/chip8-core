package net.novaware.chip8.core.memory

import spock.lang.Specification

class ReadOnlyMemorySpec extends Specification {

    def "should block byte writes when readonly"() {
        given:
        def instance = new ReadOnlyMemory(new PhysicalMemory("test", 1))
        instance.setReadOnly(true)

        when:
        instance.setByte(0 as short, 0xAB as byte)

        then:
        thrown(IllegalArgumentException)
    }

    def "should block word writes when readonly"() {
        given:
        def instance = new ReadOnlyMemory(new PhysicalMemory("test", 1))
        instance.setReadOnly(true)

        when:
        instance.setWord(0 as short, 0xABCD as short)

        then:
        thrown(IllegalArgumentException)
    }

    def "should block bytes writes when readonly"() {
        given:
        def instance = new ReadOnlyMemory(new PhysicalMemory("test", 1))
        instance.setReadOnly(true)

        when:
        instance.setBytes(0 as short, [0xAB as byte] as byte[], 0)

        then:
        thrown(IllegalArgumentException)
    }
}
