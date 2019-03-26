package be.geoforce.statemachine;

import lombok.Value;

@Value
public final class TransitionEvent<S, T, R extends StateContainer<S>> {
    public enum TransitionEventType {
        BEFORE,
        AFTER
    }

    private final TransitionEventType eventType;
    private final S fromState;
    private final T transition;
    private final R container;
}
