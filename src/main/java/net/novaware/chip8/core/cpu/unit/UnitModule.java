package net.novaware.chip8.core.cpu.unit;

import dagger.Module;
import dagger.Provides;
import net.novaware.chip8.core.cpu.register.ByteRegister;
import net.novaware.chip8.core.cpu.register.RegisterModule;
import net.novaware.chip8.core.util.di.BoardScope;

import javax.inject.Named;

@Module
public class UnitModule {

    public static final String RANDOM = "random";
    public static final String DELAY = "delay";
    public static final String SOUND = "sound";

    @Provides
    @BoardScope
    @Named(DELAY)
    static Timer provideDelayTimer(
            @Named(RegisterModule.VARIABLES) final ByteRegister[] variables,
            @Named(RegisterModule.DELAY) final ByteRegister delay
    ) {
        return new Timer(variables, delay);
    }

    @Provides
    @BoardScope
    @Named(SOUND)
    static Timer provideSoundTimer(
            @Named(RegisterModule.VARIABLES) final ByteRegister[] variables,
            @Named(RegisterModule.SOUND) final ByteRegister sound,
            @Named(RegisterModule.SOUND_ON) final ByteRegister soundOn
    ) {
        return new Timer(variables, sound, soundOn);
    }
}
