package be.geoforce.statemachine;

import be.geoforce.statemachine.exceptions.IllegalTransitionException;
import be.geoforce.statemachine.mock.Order;
import be.geoforce.statemachine.mock.OrderState;
import be.geoforce.statemachine.mock.OrderTransition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class StateMachineTest {

    private StateMachine<OrderState, OrderTransition> orderStateMachine;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Before
    public void setUp() {
        orderStateMachine = StateMachine.<OrderState, OrderTransition>builder()
            .transition(OrderState.PLACED, OrderTransition.PROCESS, OrderState.PROCESSING)
            .transition(OrderState.PLACED, OrderTransition.CANCEL, OrderState.CANCELLED)
            .transition(OrderState.PROCESSING, OrderTransition.READY, OrderState.AWAITING_PAYMENT)
            .transition(OrderState.AWAITING_PAYMENT, OrderTransition.PAY, OrderState.PAID)
            .transition(OrderState.AWAITING_PAYMENT, OrderTransition.CANCEL, OrderState.CANCELLED)
            .transition(OrderState.CANCELLED, OrderTransition.CANCEL, OrderState.CANCELLED)
            .beforeConsumer(eventPublisher::publishEvent)
            .afterConsumer(eventPublisher::publishEvent)
            .build();
    }

    @Test
    public void findTransitionWhenPresent() {
        OrderTransition transition = orderStateMachine.findTransition(OrderState.PLACED, OrderState.CANCELLED);

        assertThat(transition).isEqualTo(OrderTransition.CANCEL);
    }

    @Test
    public void findTransitionWhenMissing() {
        OrderTransition transition = orderStateMachine.findTransition(OrderState.CANCELLED, OrderState.PROCESSING);

        assertThat(transition).isNull();
    }

    @Test
    public void transitionToValidStateWithCallback() {
        Order order = new Order(OrderState.AWAITING_PAYMENT);
        List<Order> callArguments = new ArrayList<>();

        orderStateMachine.transition(order, OrderState.PAID, (orderV) -> {
            callArguments.add(orderV);
            return orderV;
        });

        assertThat(callArguments).hasSize(1);
        assertThat(callArguments.get(0).getState()).isEqualTo(OrderState.PAID);
    }

    @Test
    public void transitionToValidState() {
        Order order = new Order(OrderState.AWAITING_PAYMENT);
        ArgumentCaptor<TransitionEvent> argumentCaptor = ArgumentCaptor.forClass(TransitionEvent.class);

        Order result = orderStateMachine.transition(order, OrderState.PAID);

        verify(eventPublisher, times(2)).publishEvent(argumentCaptor.capture());
        List<TransitionEvent> allValues = argumentCaptor.getAllValues();
        TransitionEvent firstEvent = allValues.get(0);
        TransitionEvent secondEvent = allValues.get(1);
        assertThat(result.getState()).isEqualTo(OrderState.PAID);
        assertThat(firstEvent.getEventType()).isEqualTo(TransitionEvent.TransitionEventType.BEFORE);
        assertThat(firstEvent.getContainer()).isEqualTo(result);
        assertThat(firstEvent.getFromState()).isEqualTo(OrderState.AWAITING_PAYMENT);
        assertThat(firstEvent.getTransition()).isEqualTo(OrderTransition.PAY);

        assertThat(secondEvent.getEventType()).isEqualTo(TransitionEvent.TransitionEventType.AFTER);
        assertThat(secondEvent.getContainer()).isEqualTo(result);
        assertThat(secondEvent.getFromState()).isEqualTo(OrderState.AWAITING_PAYMENT);
        assertThat(secondEvent.getTransition()).isEqualTo(OrderTransition.PAY);
    }

    @Test
    public void transitionToInValidState() {
        Order order = new Order(OrderState.CANCELLED);

        assertThatExceptionOfType(IllegalTransitionException.class)
            .describedAs("Can not transition from CANCELLED to PAID")
            .isThrownBy(() -> orderStateMachine.transition(order, OrderState.PAID));

        verify(eventPublisher, never()).publishEvent(any(TransitionEvent.class));
        assertThat(order.getState()).isEqualTo(OrderState.CANCELLED);
    }

    @Test
    public void transitionWithNullContainer() {
        Order order = new Order(OrderState.CANCELLED);

        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> orderStateMachine.transition(null, OrderState.PAID));

        verify(eventPublisher, never()).publishEvent(any(TransitionEvent.class));
        assertThat(order.getState()).isEqualTo(OrderState.CANCELLED);
    }

    @Test
    public void transitionWithNullToState() {
        Order order = new Order(OrderState.CANCELLED);

        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> orderStateMachine.transition(order, null));

        verify(eventPublisher, never()).publishEvent(any(TransitionEvent.class));
        assertThat(order.getState()).isEqualTo(OrderState.CANCELLED);
    }
}
