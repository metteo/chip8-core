package net.novaware.chip8.core.util

import spock.lang.Specification

import java.util.function.Consumer

class PubSubSpec extends Specification {

    def "should create new instance"() {
        given:
        Object source = new Object()

        when:
        def instance = new PubSub(source)

        then:
        instance.source.is(source)
        instance.subscribers.isEmpty()
    }

    def "should allow subscription"() {
        given:
        Object source = new Object()
        def instance = new PubSub(source)

        Consumer<Object> handler = { s ->
            assert source.is(s)
        }

        when:
        instance.subscribe(handler)

        then:
        def handlers = instance.subscribers
        with(handlers) {
            handlers.size() == 1
            handlers[0] == handler
        }
    }

    def "should publish to a single subscriber"() {
        given:
        Object source = new Object()
        def instance = new PubSub(source)

        def published = 0

        Consumer<Object> handler = { s ->
            ++published
            assert source.is(s)
        }
        instance.subscribe(handler)

        when:
        instance.publish()

        then:
        published == 1
    }

    def "should publish to multiple subscribers"() {
        given:
        Object source = new Object()
        def instance = new PubSub(source)

        def published = 0

        for (int i = 1; i < 9; i*=2) {
            final increment = i
            Consumer<Object> handler = { s ->
                published+=increment
                assert source.is(s)
            }
            instance.subscribe(handler)
        }

        when:
        instance.publish()

        then:
        published == 1 + 2 + 4 + 8
    }

    def "should block publishing while publishing"() {
        given:
        Object source = new Object()
        def instance = new PubSub(source)

        Consumer<Object> handler = { s ->
            instance.publish()
            assert source.is(s)
        }
        instance.subscribe(handler)

        when:
        instance.publish()

        then:
        thrown(IllegalStateException)
    }

    def "should block subscribing while publishing"() {
        given:
        Object source = new Object()
        def instance = new PubSub(source)

        Consumer<Object> handler = { s ->
            instance.subscribe(null)
            assert source.is(s)
        }
        instance.subscribe(handler)

        when:
        instance.publish()

        then:
        thrown(IllegalStateException)
    }
}
