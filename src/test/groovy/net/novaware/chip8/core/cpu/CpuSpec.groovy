package net.novaware.chip8.core.cpu

import net.novaware.chip8.core.memory.Memory
import net.novaware.chip8.core.memory.MemoryMap
import spock.lang.Specification

import static net.novaware.chip8.core.cpu.register.RegistersHelper.newRegisters
import static net.novaware.chip8.core.util.UnsignedUtil.uint

class CpuSpec extends Specification {

    Cpu.Config config = Mock()

    Memory memory = Mock()

    def registers = newRegisters()

    Cpu instance = new Cpu(config, memory, registers)

    def "should reset the registers"() {
        given:
        registers.getMemoryAddress().set(0x232)
        registers.getProgramCounter().set(0x234)
        registers.getStackPointer().set(uint(MemoryMap.STACK_START) + 4)
        registers.getIndex().set(0x345)

        for (def v in registers.getVariables()) {
            v.set(1)
        }

        when:
        instance.reset()

        then:
        registers.getMemoryAddress().getAsInt() == 0
        registers.getProgramCounter().getAsInt() == 0x200
        registers.getStackPointer().getAsInt() == MemoryMap.STACK_START
        registers.getIndex().getAsInt() == 0
        registers.getDelay().getAsInt() == 0
        registers.getSound().getAsInt() == 0
        registers.getSoundOn().getAsInt() == 0

        for (def v in registers.getVariables()) {
            assert v.getAsInt() == 0
        }

        0 * memory._
    }
}
