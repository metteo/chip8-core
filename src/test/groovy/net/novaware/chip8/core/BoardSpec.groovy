package net.novaware.chip8.core

import net.novaware.chip8.core.cpu.Cpu
import net.novaware.chip8.core.memory.Memory
import spock.lang.Specification

class BoardSpec extends Specification {

    BoardConfig config = new BoardConfig()

    Cpu cpu = Mock()

    Memory interpreter = Mock()
    Memory program = Mock()
    Memory mmu = Mock()

    Board instance = new Board(config, interpreter, program, mmu, cpu)

    def "should reset cpu and memory"() {
        when:
        instance.reset()

        then:
        1 * cpu.reset()
        1 * mmu.clear()
    }
}
