package net.novaware.chip8.core.util

import spock.lang.Specification
import spock.lang.Unroll

class ViewPortSpec extends Specification {

    ViewPort instance = new ViewPort()

    def "should construct properly"() {
        expect:
        instance.getMaxWidth() == ViewPort.DEFAULT_MAX_WIDTH
        instance.getMaxHeight() == ViewPort.DEFAULT_MAX_HEIGHT
    }

    def "should throw NPE when Bit is null"() {
        when:
        instance.toIndex(null, null, true)

        then:
        thrown(NullPointerException)
    }

    def "should throw NPE when Index is null"() {
        when:
        instance.toIndex(new ViewPort.Bit(), null, true)

        then:
        thrown(NullPointerException)
    }

    def "should throw IEA when Bit has negatives"() {
        given:
        ViewPort.Bit b = new ViewPort.Bit()
        b.x = x
        b.y = y

        when:
        instance.toIndex(b, new ViewPort.Index(), true)

        then:
        thrown(IllegalArgumentException)

        where:
         x |  y
        -1 |  0
         0 | -1
    }

    @Unroll
    def "should convert '#desc' to byte / bit pair"() {
        given:
        ViewPort.Bit b = new ViewPort.Bit()
        b.x = x
        b.y = y

        ViewPort.Index i = new ViewPort.Index()

        when:
        boolean result = instance.toIndex(b, i, wrap)

        then:
        result == ok
        i.arrayByte == ab
        i.byteBit == bb

        where:

        x  | y  | wrap  || ok    | ab  | bb | desc
         0 |  0 | true  || true  |   0 |  0 | "top left wrap"
         0 |  0 | false || true  |   0 |  0 | "top left nowrap"

        63 |  0 | true  || true  |   7 |  7 | "top right wrap"
        63 |  0 | false || true  |   7 |  7 | "top right nowrap"

         0 | 31 | true  || true  | 248 |  0 | "bottom left wrap"
         0 | 31 | false || true  | 248 |  0 | "bottom left nowrap"

        63 | 31 | true  || true  | 255 |  7 | "bottom right wrap"
        63 | 31 | false || true  | 255 |  7 | "bottom right nowrap"

        64 |  0 | true  || true  |   0 |  0 | "x wrap"
        64 |  0 | false || false |  -1 | -1 | "x nowrap"

         0 | 32 | true  || true  |   0 |  0 | "y wrap"
         0 | 32 | false || false |  -1 | -1 | "y nowrap"

        65 | 33 | true  || true  |   8 |  1 | "xy wrap"
        65 | 33 | false || false |  -1 | -1 | "xy nowrap"
    }

}
