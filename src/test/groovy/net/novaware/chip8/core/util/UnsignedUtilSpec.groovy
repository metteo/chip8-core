package net.novaware.chip8.core.util

import spock.lang.Specification

import static net.novaware.chip8.core.util.UnsignedUtil.ubyte
import static net.novaware.chip8.core.util.UnsignedUtil.uint
import static net.novaware.chip8.core.util.UnsignedUtil.ushort

class UnsignedUtilSpec extends Specification {

    def "should convert short to uint" () {
        expect:
        output == uint(input)

        where:
        input           || output
        0x0000 as short || 0
        0xAAAA as short || 0xAAAA
        0xFFFF as short || 0xFFFF
    }

    def "should convert byte to uint" () {
        expect:
        output == uint(input)

        where:
        input        || output
        0x00 as byte || 0
        0xAA as byte || 0xAA
        0xFF as byte || 0xFF
    }

    def "should convert int to ushort" () {
        expect:
        output == ushort(input)

        where:
        input   || output
        0x0000  || 0x0000 as short
        0xAAAA  || 0xAAAA as short
        0xFFFF  || 0xFFFF as short
        0xABCDE || 0xBCDE as short
    }

    def "should convert byte to ushort" () {
        expect:
        output == ushort(input)

        where:
        input        || output
        0x00 as byte || 0x00 as short
        0xAA as byte || 0xAA as short
        0xFF as byte || 0xFF as short

    }

    def "should convert int to ubyte" () {
        expect:
        output == ubyte(input)

        where:
        input || output
        0x00  || 0x00 as byte
        0xAA  || 0xAA as byte
        0xFF  || 0xFF as byte
        0xABC || 0xBC as byte
    }

    def "should convert short to ubyte" () {
        expect:
        output == ubyte(input)

        where:
        input           || output
        0x00 as short   || 0x00 as byte
        0xAA as short   || 0xAA as byte
        0xFF as short   || 0xFF as byte
        0xABCD as short || 0xCD as byte
    }
}
