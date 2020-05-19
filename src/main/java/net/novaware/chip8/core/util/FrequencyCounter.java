package net.novaware.chip8.core.util;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.LongSupplier;

import static net.novaware.chip8.core.util.AssertUtil.assertState;

public class FrequencyCounter {

    private @Nullable PubSub<FrequencyCounter> pubSub;

    private final LongSupplier nanoTime;
    private int publishEvery;
    private double weightRatio;

    private long numberOfSample = 0;
    private int calculatedFrequency = 0;
    private long lastSampleTakenAt = 0;

    /**
     * Creates new instance.
     * @param publishEvery n-th sample
     * @param weightRatio how new sample affects calculated value
     */
    public FrequencyCounter(int publishEvery, double weightRatio) {
        this(System::nanoTime, publishEvery, weightRatio);
    }

    protected FrequencyCounter(LongSupplier nanoTime, int publishEvery, double weightRatio) {
        this.nanoTime = nanoTime;
        this.publishEvery = publishEvery;
        this.weightRatio = weightRatio;
    }

    public void initialize(){
        pubSub = new PubSub<>(this);
    }

    public void takeASample() {
        long start = nanoTime.getAsLong();

        if (lastSampleTakenAt != 0) {
            calculateFrequency(start - lastSampleTakenAt);
        }
        lastSampleTakenAt = start;
    }

    private void calculateFrequency(long cycleLength) {
        if (cycleLength <= 0) {
            return; //prevent division by 0 on windows
        }

        int frequency = (int)(TimeUnit.SECONDS.toNanos(1) / (cycleLength));

        calculatedFrequency = (int)(weightRatio * frequency + (1.0 - weightRatio) * calculatedFrequency);
    }

    public void maybePublish() {
        assertState(pubSub != null, "pubSub must not be null");

        // ------------------- v checker... v
        if (shouldPublish() && pubSub != null
        ) {
            pubSub.publish();
        }
        numberOfSample++;
    }

    private boolean shouldPublish() {
        return numberOfSample % publishEvery == 0;
    }

    public int getFrequency() {
        return calculatedFrequency;
    }

    public void subscribe(Consumer<FrequencyCounter> subscriber) {
        assertState(pubSub != null, "pubSub must not be null");

        if (pubSub != null) { //checker...
            pubSub.subscribe(subscriber);
        }
    }
}
