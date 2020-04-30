package net.novaware.chip8.core.config;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ImmutableConfig implements CoreConfig {

    static ImmutableConfig.Builder builder() {
        /* TODO: fix autovalue apt config in pom
        return new AutoValue_ImmutableConfig.Builder();
        */
        throw new UnsupportedOperationException("broken");
    }

    @AutoValue.Builder
    abstract static class Builder {
        abstract Builder setLegacyShift(boolean value);
        abstract Builder setLegacyLoadStore(boolean value);
        abstract Builder setLegacyAddressSum(boolean value);
        abstract Builder setEnforceMemoryRoRwState(boolean value);
        abstract Builder setDelayTimerFrequency(int value);
        abstract Builder setSoundTimerFrequency(int value);
        abstract Builder setRenderTimerFrequency(int value);
        abstract Builder setCpuFrequency(int value);
        abstract Builder setTrimVarForFont(boolean value);

        abstract ImmutableConfig build();
    }
}
