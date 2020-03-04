package net.novaware.chip8.core.cpu

import spock.lang.Specification

import static net.novaware.chip8.core.util.UnsignedUtil.ubyte

class CpuStateSpec extends Specification {

    def "should return null on unrecognized value"() {
        given:
        byte value = ubyte(0xA0)

        expect:
        CpuState.valueOf(value) == null
    }

    def "should return cpu state on valid value"() {
        given:
        byte value = ubyte(0x30)

        expect:
        CpuState.valueOf(value) == CpuState.SLEEP
    }
}
