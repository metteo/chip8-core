package net.novaware.chip8.core.cpu.register

import static net.novaware.chip8.core.cpu.register.RegisterModule.*

class RegistersHelper {

    static Registers newRegisters() {
        new Registers(
                provideCpuState(),
                provideVariables(),
                provideStatusType(),
                provideIndex(),
                provideMemoryAddress(),
                provideProgramCounter(),
                provideStackSegment(),
                provideStackPointer(),
                provideFontSegment(),
                provideGraphicSegment(),
                provideGraphicChange(),
                provideInput(),
                provideDelay(),
                provideSound(),
                provideSoundOn(),
                provideCurrentInstruction(),
                provideDecodedInstruction()
        )
    }
}