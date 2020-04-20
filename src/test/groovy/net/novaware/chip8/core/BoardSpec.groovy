package net.novaware.chip8.core

import net.novaware.chip8.core.clock.ClockGenerator
import net.novaware.chip8.core.config.CoreConfig
import net.novaware.chip8.core.config.MutableConfig
import net.novaware.chip8.core.cpu.Cpu
import net.novaware.chip8.core.memory.Memory
import net.novaware.chip8.core.memory.SplittableMemory
import net.novaware.chip8.core.port.impl.StorageMemory
import spock.lang.Specification

import static net.novaware.chip8.core.cpu.register.RegistersHelper.newRegisters

class BoardSpec extends Specification {

    CoreConfig config = new MutableConfig()

    Cpu cpu = Mock()

    Memory bootloader = Mock()
    Memory program = Mock(SplittableMemory)
    Memory storage = Mock(StorageMemory)
    Memory mmu = Mock()

    ClockGenerator clock = Mock()

    Board instance

    void setup() {
        cpu.getRegisters() >> newRegisters()

        instance = new Board(config, program, bootloader, Mock(Memory), storage, mmu, clock, cpu)
    }

    def "should reset cpu and memory"() {
        when:
        instance.hardReset0()

        then:
        1 * cpu.reset()
        1 * mmu.clear()
    }
}
