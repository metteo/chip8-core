package net.novaware.chip8.core;

import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = BoardModule.class)
public abstract class BoardFactory {

    public static BoardFactory newBoardFactory() {
        return DaggerBoardFactory.create();
    }

    public abstract Board newBoard();
}
