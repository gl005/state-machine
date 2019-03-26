package be.geoforce.statemachine;

public interface StateContainer<S> {
    S getState();
    void setState(S state);
}
