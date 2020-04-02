package net.novaware.chip8.core.config;

import dagger.Module;
import dagger.Provides;
import net.novaware.chip8.core.Board;
import net.novaware.chip8.core.cpu.Cpu;
import net.novaware.chip8.core.cpu.unit.ControlUnit;

@Module
public class ConfigModule {
    @Provides
    static Cpu.Config provideCpuConfig(final CoreConfig config) {
        return config;
    }

    @Provides
    static ControlUnit.Config provideControlUnitConfig(final CoreConfig config) {
        return config;
    }

    @Provides
    static Board.Config provideBoardConfig(final CoreConfig config) {
        return config;
    }
}
