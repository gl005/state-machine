package be.geoforce.statemachine.mock;

import be.geoforce.statemachine.State;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OrderState implements State {
    PLACED(false),
    PROCESSING(false),
    AWAITING_PAYMENT(false),
    PAID(true),
    CANCELLED(true);

    private final boolean finalState;
}
