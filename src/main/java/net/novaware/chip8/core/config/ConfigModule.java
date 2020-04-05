package net.novaware.chip8.core.config;

import dagger.Module;
import dagger.Provides;
import net.novaware.chip8.core.Board;
import net.novaware.chip8.core.cpu.Cpu;
import net.novaware.chip8.core.cpu.unit.ControlUnit;
import net.novaware.chip8.core.gpu.Gpu;
import net.novaware.chip8.core.util.di.BoardScope;

@Module
public class ConfigModule {

    @Provides
    @BoardScope
    static Cpu.Config provideCpuConfig(final CoreConfig config) {
        return config;
    }

    @Provides
    @BoardScope
    static ControlUnit.Config provideControlUnitConfig(final CoreConfig config) {
        return config;
    }

    @Provides
    @BoardScope
    static Board.Config provideBoardConfig(final CoreConfig config) {
        return config;
    }

    @Provides
    @BoardScope
    static Gpu.Config provideGpuConfig(final CoreConfig config) {
        return config;
    }
}
