package net.novaware.chip8.core.cpu

import net.novaware.chip8.core.cpu.unit.AddressGeneration
import net.novaware.chip8.core.cpu.unit.ArithmeticLogic
import net.novaware.chip8.core.cpu.unit.ControlUnit
import net.novaware.chip8.core.cpu.unit.StackEngine
import net.novaware.chip8.core.cpu.unit.Timer
import net.novaware.chip8.core.gpu.Gpu
import net.novaware.chip8.core.memory.Memory
import net.novaware.chip8.core.memory.MemoryModule
import spock.lang.Specification

import static net.novaware.chip8.core.cpu.register.RegistersHelper.newRegisters
import static net.novaware.chip8.core.util.UnsignedUtil.uint

class CpuSpec extends Specification {

    Memory memory = Mock()

    def registers = newRegisters()

    Cpu instance = new Cpu(
            Mock(Cpu.Config),
            memory,
            registers,
            Mock(ArithmeticLogic),
            Mock(AddressGeneration),
            Mock(StackEngine),
            Mock(Gpu),
            Mock(ControlUnit),
            Mock(Timer),
            Mock(Timer)
    )

    def "should properly initialize cpu"() {
        when:
        instance.initialize()

        then:
        MemoryModule.PROGRAM_START == instance.getRegisters().getProgramCounter().get()
        MemoryModule.STACK_START == instance.getRegisters().getStackPointer().get()
        MemoryModule.STACK_START == instance.getRegisters().getStackSegment().get()
    }

    def "should reset the registers"() {
        given:
        registers.getMemoryAddress().set(0x232)
        registers.getProgramCounter().set(0x234)
        registers.getStackPointer().set(uint(MemoryModule.STACK_START) + 4)
        registers.getIndex().set(0x345)

        for (def v in registers.getVariables()) {
            v.set(1)
        }

        when:
        instance.reset()

        then:
        registers.getMemoryAddress().getAsInt() == 0
        registers.getProgramCounter().getAsInt() == 0x200
        registers.getStackPointer().getAsInt() == MemoryModule.STACK_START
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
