package net.novaware.chip8.core

import net.novaware.chip8.core.clock.ClockGeneratorJvmImpl
import net.novaware.chip8.core.port.DisplayPort
import net.novaware.chip8.core.port.KeyPort
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

    def inputPacket = new KeyPort.InputPacket() {
        @Override
        KeyPort.Direction getDirection() {
            return KeyPort.Direction.DOWN
        }

        @Override
        byte getKeyCode() {
            return 0x1
        }
    }

    def "should be created and run few cycles without exceptions" () {
        given:
        def conditions = new PollingConditions(timeout: 1, initialDelay: 0.1, factor: 2.0)

        def clock = new ClockGeneratorJvmImpl("Test");

        def factory = newBoardFactory(config, clock, new Random().&nextInt)

        byte[] infiniteLoop = [0x12, 0x00] //jump to 0x200

        when:
        def board = factory.newBoard()

        board.getStoragePort().attachSource({-> infiniteLoop})
        board.getKeyPort().connect({p -> println "key output: " + p.isKeyUsed(0x1 as byte)}).accept(inputPacket)
        board.getAudioPort().connect({ p -> println "sound on: " + p.isSoundOn()})
        board.getDisplayPort(DisplayPort.Type.PRIMARY).connect({ packet -> /* noop */})

        board.initialize()

        board.runOnScheduler(64)

        then:
        noExceptionThrown()

        conditions.eventually {
            assert !board.isRunning()
        }
    }

    def "should stop after reaching MLS@011" () {
        given:
        def conditions = new PollingConditions(timeout: 1, initialDelay: 0.1, factor: 2.0)

        def clock = new ClockGeneratorJvmImpl("Test");
        def factory = newBoardFactory(config, clock, new Random().&nextInt)

        byte[] infiniteLoop = [0x00, 0x11] //exit with 1

        when:
        def board = factory.newBoard()

        board.getStoragePort().attachSource({-> infiniteLoop})
        board.getKeyPort().connect({p -> println "key output: " + p.isKeyUsed(0x1 as byte)}).accept(inputPacket)
        board.getAudioPort().connect({ p -> println "sound on: " + p.isSoundOn()})
        board.getDisplayPort(DisplayPort.Type.PRIMARY).connect({ packet -> /* noop */})

        board.initialize()

        board.runOnScheduler(256)

        then:
        noExceptionThrown()

        conditions.eventually {
            assert !board.isRunning()
            assert board.cpu.registers.output.getAsInt() == 0x11
        }
    }

    def "should properly fetch first instruction"() {
        given:
        def board = newBoardFactory(config, new ClockGeneratorJvmImpl("Test"), new Random().&nextInt).newBoard()
        board.initialize()

        def cpu = board.cpu

        short address = 0x0200
        short instruction = 0x1234 //invalid but good for this test

        board.mmu.setWord(address, instruction)
        cpu.registers.programCounter.set(address) //ignore bootloader

        when:
        cpu.cycle()

        then:
        def actual = cpu.getRegisters().getCurrentInstruction().get()
        actual == instruction
    }

    def "should properly fetch 'last' instruction"() {
        given:
        def board = newBoardFactory(config, new ClockGeneratorJvmImpl("Test"), new Random().&nextInt).newBoard()
        board.initialize()

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
        board.initialize()

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
