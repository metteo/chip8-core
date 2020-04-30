package net.novaware.chip8.core.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;
import static net.novaware.chip8.core.util.AssertUtil.assertState;

/**
 * Simple publish subscribe handling class
 * @param <T> type of the source
 */
// TODO: add @NotThreadSafe here
public class PubSub<T> {

    private static final Logger LOG = LogManager.getLogger();

    private T source;
    private final List<Consumer<T>> subscribers;
    private final boolean preventRecursivePublishing;

    /**
     * Prevents recursive publishing and handler list modifications during publishing
     */
    private boolean publishing = false;

    public PubSub(T source) {
        this(source, true);
    }

    public PubSub(T source, boolean preventRecursivePublishing) {
        this.source = source;
        this.subscribers = new ArrayList<>();
        this.preventRecursivePublishing = preventRecursivePublishing;
    }

    public void subscribe(final Consumer<T> subscriber) {
        assertState(!publishing, "can't subscribe during publishing");
        requireNonNull(subscriber, "subscriber can't be null");

        subscribers.add(subscriber);
    }

    public void publish() {
        if (preventRecursivePublishing) {
            assertState(!publishing, "can't publish during publishing");
        }

        publishing = true;
        try {
            publish0();
        } finally {
            publishing = false;
        }
    }

    private void publish0() {
        for (int i = 0; i < subscribers.size(); ++i) {
            subscribers.get(i).accept(source);
        }
    }

    public T getSource() {
        return source;
    }

    public List<Consumer<T>> getSubscribers() {
        return Collections.unmodifiableList(subscribers);
    }
}
