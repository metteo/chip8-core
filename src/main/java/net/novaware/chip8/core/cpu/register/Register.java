package net.novaware.chip8.core.cpu.register;

import net.novaware.chip8.core.util.PubSub;
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

    protected PubSub<T> pubSub;

    protected Register(String name) {
        this(name, true);
    }

    @SuppressWarnings("unchecked")
    protected Register(String name, boolean preventRecursivePublish) {
        this.name = name;

        pubSub = new PubSub<>((T) this, preventRecursivePublish);
    }

    public String getName() {
        return name;
    }

    /**
     * Will be called after register value change.
     * @param subscriber
     */
    public void subscribe(@Nullable Consumer<T> subscriber) {
        if (subscriber == null) {
            return; // ignore null
        }

        LOG.info(() -> "Subscribing to " + name + " register events");

        this.pubSub.subscribe(subscriber);
    }
}
