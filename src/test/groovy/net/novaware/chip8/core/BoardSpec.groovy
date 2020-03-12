package net.novaware.chip8.core

import net.novaware.chip8.core.clock.ClockGenerator
import net.novaware.chip8.core.cpu.Cpu
import net.novaware.chip8.core.memory.Memory
import net.novaware.chip8.core.memory.SplittableMemory
import spock.lang.Specification

import static net.novaware.chip8.core.cpu.register.RegistersHelper.newRegisters

class BoardSpec extends Specification {

    BoardConfig config = new BoardConfig()

    Cpu cpu = Mock()

    Memory bootloader = Mock()
    Memory program = Mock(SplittableMemory)
    Memory mmu = Mock()

    ClockGenerator clock = Mock()

    Board instance

    void setup() {
        cpu.getRegisters() >> newRegisters()

        instance = new Board(config, program, bootloader, Mock(Memory), mmu, clock, cpu)
    }

    def "should reset cpu and memory"() {
        when:
        instance.hardReset0()

        then:
        1 * cpu.reset()
        1 * mmu.clear()
    }
}
