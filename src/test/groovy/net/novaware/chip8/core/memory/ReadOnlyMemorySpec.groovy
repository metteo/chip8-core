package net.novaware.chip8.core.memory

import spock.lang.Specification

import java.util.function.Supplier

class ReadOnlyMemorySpec extends Specification {

    def "should block byte writes when readonly"() {
        given:
        def instance = new ReadOnlyMemory(new PhysicalMemory("test", 1))
        instance.setReadOnly({_ -> true} as Supplier)

        when:
        instance.setByte(0 as short, 0xAB as byte)

        then:
        instance.isReadOnly()
        thrown(IllegalStateException)
    }

    def "should allow byte writes when not readonly"() {
        given:
        def instance = new ReadOnlyMemory(new PhysicalMemory("test", 1))

        when:
        instance.setByte(0 as short, 0xAB as byte)

        then:
        !instance.isReadOnly()
        instance.getByte(0 as short) == 0xAB as byte
    }

    def "should block word writes when readonly"() {
        given:
        def instance = new ReadOnlyMemory(new PhysicalMemory("test", 1))
        instance.setReadOnly(true)

        when:
        instance.setWord(0 as short, 0xABCD as short)

        then:
        instance.isReadOnly()
        thrown(IllegalStateException)
    }

    def "should allow word writes when not readonly"() {
        given:
        def instance = new ReadOnlyMemory(new PhysicalMemory("test", 2))

        when:
        instance.setWord(0 as short, 0xABCD as short)

        then:
        !instance.isReadOnly()
        instance.getWord(0 as short) == 0xABCD as short
    }

    def "should block bytes writes when readonly"() {
        given:
        def instance = new ReadOnlyMemory(new PhysicalMemory("test", 1))
        instance.setReadOnly(true)

        when:
        instance.setBytes(0 as short, [0xAB as byte] as byte[], 1)

        then:
        instance.isReadOnly()
        thrown(IllegalStateException)
    }

    def "should allow bytes writes when not readonly"() {
        given:
        def instance = new ReadOnlyMemory(new PhysicalMemory("test", 1))

        when:
        instance.setBytes(0 as short, [0xAB as byte] as byte[], 1)

        then:
        !instance.isReadOnly()
        instance.getByte(0 as short) == 0xAB as byte
    }
}
