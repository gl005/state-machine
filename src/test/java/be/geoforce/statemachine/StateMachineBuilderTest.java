package be.geoforce.statemachine;

import be.geoforce.statemachine.exceptions.IllegalConfigException;
import be.geoforce.statemachine.mock.OrderState;
import be.geoforce.statemachine.mock.OrderTransition;
import org.assertj.core.description.Description;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class StateMachineBuilderTest {

    @Test
    public void addValidTransition() {
        StateMachineBuilder<OrderState, OrderTransition> builder = StateMachine.builder();
        builder.transition(OrderState.AWAITING_PAYMENT, OrderTransition.PAY, OrderState.PAID);
        StateMachine<OrderState, OrderTransition> stateMachine = builder.build();

        OrderTransition transition = stateMachine.findTransition(OrderState.AWAITING_PAYMENT, OrderState.PAID);

        assertThat(transition).isEqualTo(OrderTransition.PAY);
    }


    @Test
    public void addDoubleTransition() {
        StateMachineBuilder<OrderState, OrderTransition> builder = StateMachine.builder();
        builder.transition(OrderState.AWAITING_PAYMENT, OrderTransition.PAY, OrderState.PAID);

        assertThatExceptionOfType(IllegalConfigException.class)
                .describedAs("Transition from AWAITING_PAYMENT using PAY already exists (to PAID)")
                .isThrownBy(() -> {
                    builder.transition(OrderState.AWAITING_PAYMENT, OrderTransition.PAY, OrderState.PROCESSING);
                });
    }
}