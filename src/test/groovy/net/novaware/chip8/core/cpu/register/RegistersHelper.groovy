package net.novaware.chip8.core.cpu.register

import static net.novaware.chip8.core.cpu.register.RegisterModule.*

class RegistersHelper {

    static RegisterFile newRegisters() {
        new RegisterFile(
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
                provideInputCheck(),
                provideOutput(),
                provideStorage(),
                provideDelay(),
                provideSound(),
                provideSoundOn(),
                provideCurrentInstruction(),
                provideDecodedInstruction()
        )
    }
}