package net.novaware.chip8.core.cpu.instruction.definition

import spock.lang.Specification

class DrawSpriteDefSpec extends Specification {

    def instance = new DrawSpriteDef()

    def "should return 0 when asked for 4th parameter"() {
        expect:
        0 as short == instance.getParam(4, 0xD123 as short)
    }
}
