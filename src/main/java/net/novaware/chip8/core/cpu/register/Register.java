package net.novaware.chip8.core.cpu.register;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Base register class. Contains name property which is only used for debugging
 */
public abstract class Register<T extends Register> {

    private final String name;

    private Consumer<T> callback;

    protected Register(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Will be called after register value change.
     * @param callback
     */
    public void setCallback(@Nullable Consumer<T> callback) {
        this.callback = callback;
    }

    @SuppressWarnings("unchecked")
    void fireCallback() {
        if (callback != null) {
            callback.accept((T) this);
        }
    }
}
