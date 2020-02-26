package net.novaware.chip8.core

import net.novaware.chip8.core.cpu.Cpu
import net.novaware.chip8.core.memory.MemoryMap
import spock.lang.Specification

class BoardSpec extends Specification {

    BoardConfig config = new BoardConfig()

    Cpu cpu = Mock()

    MemoryMap memoryMap = Mock()

    Board instance = new Board(config, memoryMap, cpu)

    def "should reset cpu and memory"() {
        when:
        instance.reset()

        then:
        1 * cpu.reset()
        1 * memoryMap.clear()
    }
}
