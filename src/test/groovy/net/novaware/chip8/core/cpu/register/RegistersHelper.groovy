package net.novaware.chip8.core.cpu.register

import static net.novaware.chip8.core.cpu.register.RegisterModule.*

class RegistersHelper {

    static Registers newRegisters() {
        new Registers(
                provideVariables(),
                provideStatusType(),
                provideIndex(),
                provideMemoryAddress(),
                provideProgramCounter(),
                provideStackSegment(),
                provideStackPointer(),
                provideGraphicSegment(),
                provideGraphicChange(),
                provideKeyState(),
                provideKeyWait(),
                provideKeyValue(),
                provideDelay(),
                provideSound(),
                provideSoundOn(),
                provideCurrentInstruction(),
                provideDecodedInstruction()
        )
    }
}