package net.novaware.chip8.core.cpu.unit;

import dagger.Module;
import dagger.Provides;
import net.novaware.chip8.core.cpu.register.ByteRegister;

import javax.inject.Named;

import static net.novaware.chip8.core.cpu.register.RegisterModule.*;

@Module
public class UnitModule {

    @Provides
    @Named("delay")
    static Timer provideDelayTimer(@Named(DELAY) final ByteRegister delay) {
        return new Timer(delay);
    }

    @Provides
    @Named("sound")
    static Timer provideSoundTimer(
            @Named(SOUND) final ByteRegister sound,
            @Named(SOUND_ON) final ByteRegister soundOn
    ) {
        return new Timer(sound, soundOn);
    }
}
