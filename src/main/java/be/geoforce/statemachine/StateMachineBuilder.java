package be.geoforce.statemachine;

import be.geoforce.statemachine.exceptions.IllegalConfigException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public final class StateMachineBuilder<S, T> {
    private final Map<S, Map<T, S>> transitions = new HashMap<>();
    private Consumer<TransitionEvent<S, T, ?>> beforeConsumer;
    private Consumer<TransitionEvent<S, T, ?>> afterConsumer;

    StateMachineBuilder() {
    }

    public StateMachineBuilder<S, T> transition(S fromState, T transition, S toState) {
        requireNonNull(fromState, "fromState");
        requireNonNull(transition, "transition");
        requireNonNull(toState, "toState");

        S existingToState = transitions
            .getOrDefault(fromState, Collections.emptyMap())
            .getOrDefault(transition, null);

        if (existingToState != null) {
            throw new IllegalConfigException(String.format("Transition from %s using %s already exists (to %s)", fromState, transition, existingToState));
        }

        transitions
            .computeIfAbsent(fromState, k -> new HashMap<>())
            .put(transition, toState);

        return this;
    }

    public StateMachineBuilder<S, T> beforeConsumer(Consumer<TransitionEvent<S, T, ?>> beforeConsumer) {
        this.beforeConsumer = beforeConsumer;
        return this;
    }

    public StateMachineBuilder<S, T> afterConsumer(Consumer<TransitionEvent<S, T, ?>> afterConsumer) {
        this.afterConsumer = afterConsumer;
        return this;
    }

    public StateMachine<S, T> build() {
        return new StateMachine<S, T>(transitions, beforeConsumer, afterConsumer);
    }
}
