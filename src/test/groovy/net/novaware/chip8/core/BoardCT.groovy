package net.novaware.chip8.core

import spock.lang.Specification

import static net.novaware.chip8.core.BoardFactory.newBoardFactory

/**
 * Component Test for {@link Board}
 */
class BoardCT extends Specification {

    def "should be created and run few cycles without exceptions" () {
        given:
        BoardConfig config = new BoardConfig(
                cpuFrequency: 600,
                delayTimerFrequency: 61,
                soundTimerFrequency: 59,
                enforceMemoryRoRwState: true,
                legacyShift: false,
                legacyLoadStore: false,
                legacyAddressSum: false
        )

        def factory = newBoardFactory(config)

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
