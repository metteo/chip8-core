package net.novaware.chip8.core.memory

import spock.lang.Specification

class UnusedMemorySpec extends Specification {

    def "should support basic methods" () {
        given:
        def instance = new UnusedMemory(3)

        when:
        callClear(instance)

        then:
        instance.getName() == "Unused"
        instance.getSize() == 3
        noExceptionThrown()
    }

    def "should throw when calling read / write methods"() {
        given:
        def instance = new UnusedMemory(3)

        when:
        method.call(instance)

        then:
        thrown(UnsupportedOperationException)

        where:
        method << [
            { Memory m -> m.getByte(0 as short) },
            { Memory m -> m.setByte(0 as short, 0 as byte) },
            { Memory m -> m.getWord(0 as short) },
            { Memory m -> m.setWord(0 as short, 0 as short) },
            { Memory m -> m.getBytes(0 as short, [] as byte[], 0) },
            { Memory m -> m.setBytes(0 as short, [] as byte[], 0) },
        ]
    }

    static boolean callClear(Memory memory) {
        memory.clear()
        true
    }
}
