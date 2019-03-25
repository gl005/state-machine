package be.geoforce.statemachine;

import be.geoforce.statemachine.exceptions.IllegalConfigException;
import be.geoforce.statemachine.exceptions.IllegalTransitionException;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.context.ApplicationEventPublisher;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

@RequiredArgsConstructor
public class StateMachine<S extends State, T extends Transition<S>> {
    private final Set<T> transitions;
    private final ApplicationEventPublisher applicationEventPublisher;

    @PostConstruct
    private void validateTransitions() {
        Set<SingleTransition<S>> singleTransitions = new HashSet<>();
        for (T transition : transitions) {
            for (S fromState : transition.getFrom()) {
                SingleTransition<S> singleTransition = new SingleTransition<>(fromState, transition.getTo());

                // check if there isn't already a transition between these states
                if (singleTransitions.contains(singleTransition)) {
                    throw new IllegalConfigException(
                            String.format("Ambiguous transition from %s to %s, there is another transitions between these states",
                                    singleTransition.fromState, singleTransition.toState));
                }

                // from state can not not be a final state
                if (fromState.isFinalState()) {
                    throw new IllegalConfigException(String.format("Transition found from state '%s' which is marked as final", fromState));
                }

                // from and to state can not be the same
                if (fromState.equals(singleTransition.getToState())) {
                    throw new IllegalConfigException(String.format("From state can not be equal to to destination state '%s'", singleTransition.getToState()));
                }
                singleTransitions.add(singleTransition);
            }
        }
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
        TransitionEvent beforeEvent = createEvent(TransitionEvent.TransitionEventType.BEFORE, container, beforeState, transition);
        applicationEventPublisher.publishEvent(beforeEvent);

        container.setState(toState);
        R resultContainer = onTransitioned == null ? container: onTransitioned.apply(container);

        TransitionEvent afterEvent = createEvent(TransitionEvent.TransitionEventType.AFTER, container, beforeState, transition);
        applicationEventPublisher.publishEvent(afterEvent);
        return resultContainer;
    }

    protected <R extends StateContainer<S>> TransitionEvent createEvent(TransitionEvent.TransitionEventType transitionEventType, R container, S beforeState, T transition) {
        return new TransitionEvent<>(transitionEventType, beforeState, transition, container);
    }

    public T findTransition(S fromState, S toState) {
        for (T transition : transitions) {
            if (transition.getFrom().contains(fromState) && transition.getTo().equals(toState)) {
                return transition;
            }
        }
        return null;
    }


    @Value
    private static final class SingleTransition<S extends State> {
        private final S fromState;
        private final S toState;
    }
}
