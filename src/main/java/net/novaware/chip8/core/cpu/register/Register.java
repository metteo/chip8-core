package net.novaware.chip8.core.cpu.register;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Consumer;

/**
 * Base register class. Contains name property which is only used for debugging
 */
public abstract class Register<T extends Register<?>> {

    private static final Logger LOG = LogManager.getLogger();

    private final String name;

    private @Nullable Consumer<T> callback;

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
        String setOrUpdate = this.callback != null ? "Updating" : "Setting";
        LOG.info(() -> setOrUpdate + " " + name + " callback");

        this.callback = callback;
    }

    @SuppressWarnings("unchecked")
    void fireCallback() {
        if (callback != null) {
            callback.accept((T) this);
        }
    }
}
