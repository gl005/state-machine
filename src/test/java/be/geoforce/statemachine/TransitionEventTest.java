package be.geoforce.statemachine;

import be.geoforce.statemachine.mock.Order;
import be.geoforce.statemachine.mock.OrderState;
import be.geoforce.statemachine.mock.OrderTransition;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TransitionEventTest {

    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(TransitionEvent.class)
                .verify();
    }

    @Test
    public void toStringTest() {
        TransitionEvent<OrderState, OrderTransition, Order> event = new TransitionEvent<>(
                        TransitionEvent.TransitionEventType.BEFORE,
                        OrderState.AWAITING_PAYMENT,
                        OrderTransition.CANCEL,
                        new Order(OrderState.CANCELLED));

        String result = event.toString();
        assertThat(result).isEqualTo("TransitionEvent(eventType=BEFORE, fromState=AWAITING_PAYMENT, transition=CANCEL, container=Order(state=CANCELLED))");
    }
}