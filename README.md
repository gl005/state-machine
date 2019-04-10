# Finite State Machine 
Simplifies managing a finite state machine (FSM) by providing an easy to use API to build, validate and transition.

## Defining a new state machine using the StateMachineBuilder
Defining a state machine with the StateMachineBuilder
```
StateMachine<OrderState, OrderTransition> orderStateMachine = StateMachine.<OrderState, OrderTransition>builder()
              .transition(OrderState.PLACED, OrderTransition.PROCESS, OrderState.PROCESSING)
              .transition(OrderState.PLACED, OrderTransition.CANCEL, OrderState.CANCELLED)
              .transition(OrderState.PROCESSING, OrderTransition.READY, OrderState.AWAITING_PAYMENT)
              .transition(OrderState.AWAITING_PAYMENT, OrderTransition.PAY, OrderState.PAID)
              .transition(OrderState.AWAITING_PAYMENT, OrderTransition.CANCEL, OrderState.CANCELLED)
              .transition(OrderState.CANCELLED, OrderTransition.CANCEL, OrderState.CANCELLED)
              .beforeConsumer(eventPublisher::publishEvent)
              .afterConsumer(eventPublisher::publishEvent)
              .build();
```
The ``transition(State fromState, Transition transition, State toState)`` method will add a new valid state transition to the FSM.

Calling the before and afterConsumer is optional and allows you to configure Consumers that will be executed before and after the FSM executes a state transition respectively. 

Upon calling build() the transitions are validated to check if there are not multiple transitions between the same 2 states. 

## Guarding a transition
If you want to perform additional validation or side-effects before or after transitioning to a state you can use the before and after consumers. 
