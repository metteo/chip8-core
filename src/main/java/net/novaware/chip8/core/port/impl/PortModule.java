package net.novaware.chip8.core.port.impl;

import dagger.Module;
import dagger.Provides;
import net.novaware.chip8.core.cpu.register.ByteRegister;
import net.novaware.chip8.core.cpu.register.RegisterModule;
import net.novaware.chip8.core.cpu.register.WordRegister;
import net.novaware.chip8.core.memory.Memory;
import net.novaware.chip8.core.util.di.BoardScope;

import javax.inject.Named;

import static net.novaware.chip8.core.memory.MemoryModule.DISPLAY_IO;
import static net.novaware.chip8.core.memory.MemoryModule.STORAGE_ROM;

/**
 * Returning implementation because they are necessary for Board operation.
 * Interfaces are only for outside world.
 */
@Module
public class PortModule {

    public static final String PRIMARY = "primary";
    public static final String SECONDARY = "secondary";

    @Provides
    @BoardScope
    @Named(PRIMARY)
    static DisplayPortImpl providePrimaryDisplayPort(
            @Named(RegisterModule.GRAPHIC_CHANGE) final ByteRegister graphicChange,
            @Named(DISPLAY_IO) final Memory displayIo
    ) {
        return new DisplayPortImpl(graphicChange, displayIo);
    }

    @Provides
    @BoardScope
    @Named(SECONDARY)
    static DisplayPortImpl provideSecondaryDisplayPort(
            @Named(RegisterModule.GRAPHIC_CHANGE) final ByteRegister graphicChange,
            @Named(DISPLAY_IO) final Memory displayIo
    ) {
        return new DisplayPortImpl(graphicChange, displayIo);
    }

    @Provides
    @BoardScope
    static AudioPortImpl provideAudioPort(
            @Named(RegisterModule.SOUND_ON) final ByteRegister soundOn
    ) {
        return new AudioPortImpl(soundOn);
    }

    @Provides
    @BoardScope
    static KeyPortImpl provideKeyPort(
            @Named(RegisterModule.INPUT) final WordRegister input,
            @Named(RegisterModule.INPUT_CHECK) final WordRegister inputCheck
    ) {
        return new KeyPortImpl(input, inputCheck);
    }

    @Provides
    @BoardScope
    static StoragePortImpl provideStoragePort(
            @Named(STORAGE_ROM) final Memory storageRom
    ) {
        return new StoragePortImpl((StorageMemory)storageRom);
    }
}
