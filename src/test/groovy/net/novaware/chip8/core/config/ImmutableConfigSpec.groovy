package net.novaware.chip8.core.config

import spock.lang.Specification

class ImmutableConfigSpec extends Specification {

    def "should construct proper instance"() {
        given:

        when:
        def instance = ImmutableConfig.builder()
            .setCpuFrequency(500)
            .setDelayTimerFrequency(61)
            .setSoundTimerFrequency(62)
            .setRenderTimerFrequency(63)
            .setLegacyShift(true)
            .setLegacyLoadStore(false)
            .setLegacyAddressSum(true)
            .setEnforceMemoryRoRwState(false)
            .setTrimVarForFont(false)
            .build()

        then:
        with(instance) {
            getCpuFrequency() == 500
            getDelayTimerFrequency() == 61
            getSoundTimerFrequency() == 62
            getRenderTimerFrequency() == 63
            isLegacyShift()
            !isLegacyLoadStore()
            isLegacyAddressSum()
            !isEnforceMemoryRoRwState()
            !isTrimVarForFont()
        }

        !instance.toString().isEmpty()
        instance.hashCode() == instance.hashCode() //stable hashcode
        instance.equals(instance) //reflexive equals
    }
}
