package net.novaware.chip8.core

import net.novaware.chip8.core.clock.ClockGeneratorJvmImpl
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import static net.novaware.chip8.core.BoardFactory.newBoardFactory

/**
 * Component Test for {@link Board}
 */
//TODO: refactor this test
class BoardCT extends Specification {

    BoardConfig config = new BoardConfig(
            cpuFrequency: 600,
            delayTimerFrequency: 61,
            soundTimerFrequency: 59,
            enforceMemoryRoRwState: true,
            legacyShift: false,
            legacyLoadStore: false,
            legacyAddressSum: false
    )

    def "should be created and run few cycles without exceptions" () {
        given:
        def conditions = new PollingConditions(timeout: 1, initialDelay: 0.1, factor: 2.0)

        def clock = new ClockGeneratorJvmImpl("Test");

        def factory = newBoardFactory(config, clock, new Random().&nextInt)

        byte[] infiniteLoop = [0x12, 0x00] //jump to 0x200

        when:
        def board = factory.newBoard()
        board.init()

        board.getStoragePort().load(infiniteLoop)
        board.getKeyPort().keyPressed(0x0 as byte)
        board.getAudioPort().attach({on -> println "sound on: " + on})
        board.getDisplayPort().attach({ gc, buffer -> /* noop */})

        board.runOnScheduler(10)

        then:
        noExceptionThrown()

        conditions.eventually {
            assert !board.isRunning()
        }
    }

    def "should properly fetch first instruction"() {
        given:
        def board = newBoardFactory(config, new ClockGeneratorJvmImpl("Test"), new Random().&nextInt).newBoard()
        board.init()

        def cpu = board.cpu

        short address = 0x0200
        short instruction = 0x1234 //invalid but good for this test

        board.mmu.setWord(address, instruction)

        when:
        cpu.cycle()

        then:
        def actual = cpu.getRegisters().getCurrentInstruction().get()
        actual == instruction
    }

    def "should properly fetch 'last' instruction"() {
        given:
        def board = newBoardFactory(config, new ClockGeneratorJvmImpl("Test"), new Random().&nextInt).newBoard()
        board.init()

        def cpu = board.cpu

        short address = 0x0FF0
        short instruction = 0x1234 as short

        board.mmu.setWord(address, instruction)

        cpu.getRegisters().getProgramCounter().set(address)

        when:
        cpu.cycle()

        then:
        def actual = cpu.getRegisters().getCurrentInstruction().get()
        actual == instruction
    }

    def "should properly decode MVI / LD I instruction"() {
        given:
        def board = newBoardFactory(config, new ClockGeneratorJvmImpl("Test"), new Random().&nextInt).newBoard()
        board.init()

        def cpu = board.cpu

        short address = 0x0FF0
        short instruction = 0xA321 as short

        board.mmu.setWord(address, instruction)

        cpu.getRegisters().getProgramCounter().set(address)

        when:
        cpu.cycle()

        then:
        def decoded = cpu.getRegisters().getDecodedInstruction()
        decoded[0].get() == (short)0xA000
        decoded[1].get() == (short)0x0321
        decoded[2].get() == (short)0x0000
        decoded[3].get() == (short)0x0000
    }
}
