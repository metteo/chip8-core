package net.novaware.chip8.core.cpu

import net.novaware.chip8.core.cpu.unit.*
import net.novaware.chip8.core.gpu.Gpu
import net.novaware.chip8.core.memory.Memory
import spock.lang.Specification

import static net.novaware.chip8.core.cpu.register.RegistersHelper.newRegisters

class CpuSpec extends Specification {

    Memory memory = Mock()

    LoadStore lsu = Mock()
    ArithmeticLogic alu = Mock()
    AddressGen agu = Mock()
    StackEngine stackEngine = Mock()
    PowerMgmt powerMgmt = Mock()
    Gpu gpu = Mock()
    ControlUnit cu = Mock()
    Timer delay = Mock()
    Timer sound = Mock()

    def registers = newRegisters()

    Cpu instance = new Cpu(
            Mock(Cpu.Config),
            memory,
            registers,
            lsu,
            alu,
            agu,
            stackEngine,
            powerMgmt,
            gpu,
            cu,
            delay,
            sound
    )

    def "should properly initialize cpu"() {
        when:
        instance.initialize()

        then:
        0 * memory._
        1 * lsu.initialize()
        1 * alu.initialize()
        1 * agu.initialize()
        1 * stackEngine.initialize()
        1 * delay.initialize()
        1 * sound.initialize()
        1 * cu.initialize()
    }

    def "should reset the registers"() {
        given:

        when:
        instance.reset()

        then:
        0 * memory._
        1 * lsu.reset()
        1 * alu.reset()
        1 * agu.reset()
        1 * stackEngine.reset()
        1 * gpu.reset()
        1 * delay.reset()
        1 * sound.reset()
        1 * cu.reset()
    }
}
