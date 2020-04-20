package net.novaware.chip8.core.port.impl

import net.novaware.chip8.core.port.StoragePort
import spock.lang.Specification

class StorageMemorySpec extends Specification {

    def "should report correct name and size"() {
        given:
        def memory = new StorageMemory("test", 1024)

        expect:
        memory.name == "test"
        memory.getSize() == 1024
    }

    def "should return zeros when packet is absent"() {
        given:

        def memory = new StorageMemory("test", 1024)

        expect:

        for(def a in 0..1023) {
            assert memory.getByte(a as short) == 0x00 as byte
        }
    }

    def "should forward data from storage or zeros"() {
        given:

        def packet = new StoragePort.Packet() {

            byte[] data = [0xAB, 0xCD]

            @Override
            int getSize() {
                return data.length
            }

            @Override
            byte getByte(short address) {
                assert address < data.length

                return data[address]
            }
        }

        def memory = new StorageMemory("test", 1024)
        memory.packet = packet

        expect:
        memory.getByte(0x0 as short) == 0xAB as byte
        memory.getByte(0x1 as short) == 0xCD as byte

        for(def a in 2..1023) {
            assert memory.getByte(a as short) == 0x00 as byte
        }
    }
}
