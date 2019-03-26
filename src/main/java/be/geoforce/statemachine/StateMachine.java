package be.geoforce.statemachine;

import be.geoforce.statemachine.exceptions.IllegalTransitionException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static be.geoforce.statemachine.TransitionEvent.TransitionEventType.AFTER;
import static be.geoforce.statemachine.TransitionEvent.TransitionEventType.BEFORE;

public final class StateMachine<S, T> {
    private final Map<S, Map<T, S>> transitions;
    private final Consumer<TransitionEvent<S, T, ?>> beforeConsumer;
    private final Consumer<TransitionEvent<S, T, ?>> afterConsumer;

    public StateMachine(
        Map<S, Map<T, S>> transitions,
        Consumer<TransitionEvent<S, T, ?>> beforeConsumer,
        Consumer<TransitionEvent<S, T, ?>> afterConsumer
    ) {
        // Copy
        this.transitions = new HashMap<>();
        for (Map.Entry<S, Map<T, S>> entry : transitions.entrySet()) {
            this.transitions.put(entry.getKey(), new HashMap<>(entry.getValue()));
        }

        this.beforeConsumer = beforeConsumer;
        this.afterConsumer = afterConsumer;
    }

    public <R extends StateContainer<S>> R transition(R container, S toState) {
        return transition(container, toState, null);
    }

    public <R extends StateContainer<S>> R transition(R container, S toState, Function<R, R> onTransitioned) {
        if (container == null || toState == null) {
            throw new IllegalArgumentException();
        }

        S beforeState = container.getState();
        T transition = findTransition(beforeState, toState);
        if (transition == null) {
            throw new IllegalTransitionException(String.format("Can not transition from %s to %s", beforeState, toState));
        }
        TransitionEvent<S, T, R> beforeEvent = new TransitionEvent<>(BEFORE, beforeState, transition, container);
        beforeConsumer.accept(beforeEvent);

        container.setState(toState);
        R resultContainer = onTransitioned == null ? container : onTransitioned.apply(container);

        TransitionEvent<S, T, R> afterEvent = new TransitionEvent<>(AFTER, beforeState, transition, container);
        afterConsumer.accept(afterEvent);
        return resultContainer;
    }

    public T findTransition(S fromState, S toState) {
        return transitions
            .getOrDefault(fromState, Collections.emptyMap())
            .entrySet().stream()
            .filter(e -> e.getValue().equals(toState))
            .map(Map.Entry::getKey)
            .findFirst().orElse(null);
    }

    public static <S, T> StateMachineBuilder<S, T> builder() {
        return new StateMachineBuilder<>();
    }

}
