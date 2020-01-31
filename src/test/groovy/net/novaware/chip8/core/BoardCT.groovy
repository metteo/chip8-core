package net.novaware.chip8.core

import spock.lang.Specification

import static net.novaware.chip8.core.BoardFactory.newBoardFactory

/**
 * Component Test for {@link Board}
 */
class BoardCT extends Specification {

    def "should be created and run few cycles without exceptions" () {
        given:
        def factory = newBoardFactory()

        byte[] infiniteLoop = [0x12, 0x00] //jump to 0x200

        when:
        def board = factory.newBoard()
        board.init()
        board.getStoragePort().load(infiniteLoop)
        board.run(10)

        then:
        noExceptionThrown()
    }
}
