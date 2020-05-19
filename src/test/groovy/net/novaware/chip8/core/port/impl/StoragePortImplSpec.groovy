package net.novaware.chip8.core.port.impl

import net.novaware.chip8.core.cpu.register.TribbleRegister
import net.novaware.chip8.core.port.StoragePort
import spock.lang.Specification

import java.util.function.Supplier

class StoragePortImplSpec extends Specification {

    def register = Mock(TribbleRegister)
    def memory = Mock(StorageMemory)
    def instance = new StoragePortImpl(memory, register)

    def "should connect correctly"() {
        given:
        StoragePort.Packet packet = Mock()
        Supplier<StoragePort.Packet> supplier = { -> packet}

        when:
        instance.connect(supplier)

        then:
        instance.packetSupplier == supplier
        1 * memory.setPacket(packet)
        1 * packet.getSize()
    }

    def "should disconnect correctly"() {
        given:
        StoragePort.Packet packet = Mock()
        Supplier<StoragePort.Packet> supplier = { -> packet}
        instance.connect(supplier)

        when:
        instance.disconnect()

        then:
        instance.packetSupplier == null
        1 * memory.setPacket(null)
        1 * register.set(0)
    }
}
