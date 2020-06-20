package net.novaware.chip8.core.config

import spock.lang.Specification

class MutableConfigSpec extends Specification {

    def "should construct and instance"() {
        when:
        def instance = new MutableConfig()
        with(instance) {
            setCpuFrequency(501)
            setDelayTimerFrequency(62)
            setSoundTimerFrequency(63)
            setRenderTimerFrequency(64)
            setEnforceMemoryRoRwState(true)
            setLegacyShift(false)
            setLegacyLoadStore(true)
            setLegacyAddressSum(false)
            setTrimVarForFont(true)
            setWrapping(false)
            setVerticalClipping(true)
            setHorizontalClipping(false)
            setClsCollision(true)
        }

        then:
        with(instance) {
            getCpuFrequency() == 501
            getDelayTimerFrequency() == 62
            getSoundTimerFrequency() == 63
            getRenderTimerFrequency() == 64
            isEnforceMemoryRoRwState()
            !isLegacyShift()
            isLegacyLoadStore()
            !isLegacyAddressSum()
            isTrimVarForFont()
            !isWrapping()
            isVerticalClipping()
            !isHorizontalClipping()
            isClsCollision()
        }
    }
}
