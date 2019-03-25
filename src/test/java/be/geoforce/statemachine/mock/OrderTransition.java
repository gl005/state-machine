package be.geoforce.statemachine.mock;

import be.geoforce.statemachine.Transition;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.EnumSet;
import java.util.Set;

@Getter
@AllArgsConstructor
public enum OrderTransition implements Transition<OrderState> {
    PROCESS(OrderState.PLACED, OrderState.PROCESSING),
    READY(OrderState.PROCESSING, OrderState.AWAITING_PAYMENT),
    PAY(OrderState.AWAITING_PAYMENT, OrderState.PAID),
    CANCEL(EnumSet.of(
            OrderState.AWAITING_PAYMENT,
            OrderState.CANCELLED,
            OrderState.PLACED),
            OrderState.CANCELLED);

    private final Set<OrderState> from;
    private final OrderState to;
    public final static Set<OrderTransition> ALL = EnumSet.allOf(OrderTransition.class);

    OrderTransition(OrderState from, OrderState to) {
        this(EnumSet.of(from), to);
    }
}
