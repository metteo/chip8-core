package net.novaware.chip8.core.port.impl

import net.novaware.chip8.core.cpu.register.ByteRegister
import net.novaware.chip8.core.cpu.register.RegisterFile
import net.novaware.chip8.core.memory.Memory
import net.novaware.chip8.core.memory.MemoryModule
import net.novaware.chip8.core.memory.PhysicalMemory
import net.novaware.chip8.core.port.DisplayPort
import spock.lang.Specification
import spock.lang.Unroll

import static net.novaware.chip8.core.util.UnsignedUtil.*

class DisplayPortImplSpec extends Specification {

    ByteRegister graphicChange = new ByteRegister("GC")
    Memory displayIo = new PhysicalMemory("Display IO", MemoryModule.DISPLAY_IO_SIZE)

    def instance = new DisplayPortImpl(graphicChange, displayIo)

    def "should use defaults after creation"() {
        expect:
        instance.getMode() == DisplayPort.Mode.DIRECT
    }

    @Unroll
    def "should send packet on graphic change in #mode"() {
        given:
        instance.attachToRegister()
        instance.setMode(mode)

        boolean gotPacket = false
        instance.connect({ p -> gotPacket = true})

        when:
        graphicChange.set(RegisterFile.GC_DRAW)

        then:
        gotPacket
        instance.getMode() == mode

        where:
        mode << [DisplayPort.Mode.DIRECT, DisplayPort.Mode.MERGE_FRAME, DisplayPort.Mode.FALLING_EDGE]
    }

    def "should not send packet on graphic change to disconnected receiver"() {
        given:
        instance.attachToRegister()

        boolean gotPacket = false
        instance.connect({ p -> gotPacket = true})
        instance.disconnect()

        when:
        graphicChange.set(RegisterFile.GC_DRAW)

        then:
        !gotPacket
    }

    def "should properly update beginning of the buffer"() {
        given:
        instance.attachToRegister()
        DisplayPort.Packet packet = null

        instance.connect({ p -> packet = p})

        displayIo.setByte(USHORT_0, ubyte(0x80))

        when:
        graphicChange.set(RegisterFile.GC_DRAW)

        then:
        packet.getColumnCount() == 64
        packet.getRowCount() == 32
        packet.getPixel(0, 0)
        !packet.getPixel(1, 0)
        !packet.getPixel(0, 1)
        !packet.getPixel(1, 1)
        graphicChange.getAsInt() == RegisterFile.GC_IDLE
    }

    def "should properly update end of the buffer"() {
        given:
        instance.attachToRegister()
        DisplayPort.Packet packet = null

        instance.connect({ p -> packet = p})

        displayIo.setByte(ushort(255), ubyte(0x1))

        when:
        graphicChange.set(RegisterFile.GC_DRAW)

        then:
        packet.getPixel(63, 31)
        !packet.getPixel(63, 30)
        !packet.getPixel(62, 31)
        !packet.getPixel(62, 30)
    }

    def "should not update front buffer without falling edge state"() {
        given:
        instance.attachToRegister()
        instance.setMode(DisplayPort.Mode.FALLING_EDGE)
        DisplayPort.Packet packet = null

        instance.connect({ p -> packet = p})

        displayIo.setByte(USHORT_0, ubyte(0x80))

        when:
        graphicChange.set(RegisterFile.GC_DRAW)

        then:
        packet.getColumnCount() == 64
        packet.getRowCount() == 32
        !packet.getPixel(0, 0)

        graphicChange.getAsInt() == RegisterFile.GC_IDLE

        and:
        instance.tick()

        then:
        !packet.getPixel(0, 0) //not enough time has passed since last draw

        and:
        instance.updateFrontBuffer()

        then:
        packet.getPixel(0, 0)
    }
}