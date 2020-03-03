package net.novaware.chip8.core.cpu.unit;

import dagger.Module;
import dagger.Provides;
import net.novaware.chip8.core.cpu.register.ByteRegister;
import net.novaware.chip8.core.cpu.register.RegisterModule;

import javax.inject.Named;
import javax.inject.Singleton;

@Module
public class UnitModule {

    public static final String DELAY = "delay";
    public static final String SOUND = "sound";

    @Provides
    @Singleton
    @Named(DELAY)
    static Timer provideDelayTimer(
            @Named(RegisterModule.VARIABLES) final ByteRegister[] variables,
            @Named(RegisterModule.DELAY) final ByteRegister delay
    ) {
        return new Timer(variables, delay);
    }

    @Provides
    @Singleton
    @Named(SOUND)
    static Timer provideSoundTimer(
            @Named(RegisterModule.VARIABLES) final ByteRegister[] variables,
            @Named(RegisterModule.SOUND) final ByteRegister sound,
            @Named(RegisterModule.SOUND_ON) final ByteRegister soundOn
    ) {
        return new Timer(variables, sound, soundOn);
    }
}
