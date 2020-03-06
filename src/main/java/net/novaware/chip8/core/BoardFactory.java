package net.novaware.chip8.core;

import dagger.BindsInstance;
import dagger.Component;
import net.novaware.chip8.core.clock.ClockGenerator;
import net.novaware.chip8.core.cpu.register.RegisterModule;
import net.novaware.chip8.core.cpu.unit.UnitModule;
import net.novaware.chip8.core.memory.MemoryModule;
import net.novaware.chip8.core.util.di.BoardScope;

import javax.inject.Named;
import java.util.function.IntUnaryOperator;

import static net.novaware.chip8.core.cpu.unit.UnitModule.RANDOM;

@BoardScope
@Component(modules = {
        RegisterModule.class,
        MemoryModule.class,
        UnitModule.class,
        BoardModule.class
})
public abstract class BoardFactory {

    public static BoardFactory newBoardFactory(BoardConfig config, ClockGenerator clock, IntUnaryOperator random) {
        return DaggerBoardFactory.builder()
                .config(config)
                .clock(clock)
                .random(random)
                .build();
    }

    public abstract Board newBoard();

    @Component.Builder
    public static abstract class Builder {

        @BindsInstance
        public abstract Builder config(BoardConfig config);

        @BindsInstance
        public abstract Builder clock(ClockGenerator clock);

        @BindsInstance
        public abstract Builder random(@Named(RANDOM) IntUnaryOperator random);

        public abstract BoardFactory build();
    }
}
