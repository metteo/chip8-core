package net.novaware.chip8.core;

import dagger.BindsInstance;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = BoardModule.class)
public abstract class BoardFactory {

    public static BoardFactory newBoardFactory(BoardConfig config) {
        return DaggerBoardFactory.builder()
                .config(config)
                .build();
    }

    public abstract Board newBoard();

    @Component.Builder
    public static abstract class Builder {

        @BindsInstance
        public abstract Builder config(BoardConfig config);

        public abstract BoardFactory build();
    }
}
