package net.novaware.chip8.core.cpu.unit

import net.novaware.chip8.core.memory.Memory
import net.novaware.chip8.core.memory.MemoryModule
import spock.lang.Specification

import static net.novaware.chip8.core.cpu.register.RegistersHelper.newRegisters
import static net.novaware.chip8.core.util.UnsignedUtil.uint
import static net.novaware.chip8.core.util.UnsignedUtil.ushort

class StackEngineSpec extends Specification {

    def registers = newRegisters()

    Memory memory = Mock()

    def instance = new StackEngine(
            registers.stackSegment,
            registers.stackPointer,
            registers.memoryAddress,
            registers.programCounter,
            registers.variables,
            memory
    )

    def setup() {
        registers.stackSegment.set(MemoryModule.STACK_START)
        instance.initialize()
    }

    def "should properly init stack pointer"() {
        expect:
        registers.stackPointer.getAsInt() == stackBottom()
    }

    def "should properly reset stack pointer"() {
        given:
        registers.stackPointer.increment(-4)

        when:
        instance.reset()

        then:
        registers.stackPointer.getAsInt() == stackBottom()
    }

    def "should add method call address on stack and update pc"() {
        registers.memoryAddress.set(0x200)
        registers.programCounter.set(0x202)

        short sp = ushort(stackBottom() - 2)

        when:
        instance.call(ushort(0x0300))

        then:
        registers.programCounter.getAsInt() == 0x0300
        registers.stackPointer.get() == sp
        1 * memory.setWord(sp, ushort(0x0200))
    }

    def "should throw stack overflow exception"() {
        registers.memoryAddress.set(0x200)
        registers.stackPointer.set(MemoryModule.STACK_START)

        when:
        instance.call(ushort(0x0300))

        then:
        def ex = thrown(IllegalStateException)
        ex.message == "Stack overflow at 0x0200"
    }

    def "should get method call address from stack and update pc"() {
        short sp = ushort(stackBottom() - 2)

        registers.memoryAddress.set(0x300)
        registers.programCounter.set(0x302)
        registers.stackPointer.set(sp)

        when:
        instance.returnFromSubroutine()

        then:
        1 * memory.getWord(sp) >> ushort(0x200)

        registers.programCounter.getAsInt() == 0x0202
        registers.stackPointer.get() == ushort(stackBottom())
    }

    def "should throw stack underflow exception"() {
        registers.memoryAddress.set(0x200)
        registers.stackPointer.set(stackBottom())

        when:
        instance.returnFromSubroutine()

        then:
        def ex = thrown(IllegalStateException)
        ex.message == "Stack underflow at 0x0200"
    }

    def "should update pc with jump target"() {
        registers.memoryAddress.set(0x200)
        registers.variables[0x1].set(0x3)
        registers.programCounter.set(0x202)
        short sp = registers.stackPointer.get()

        when:
        instance.jump(ushort(0x0300), ushort(0x1))

        then:
        registers.programCounter.getAsInt() == 0x0303
        registers.stackPointer.get() == sp
        0 * memory._
    }

    def stackBottom() {
        uint(MemoryModule.STACK_END) + 1
    }
}
