package simu.model;

import simu.framework.IEventType;

/**
 * Event types are defined by the requirements of the simulation model
 *
 * TODO: This must be adapted to the actual simulator
 */
public enum EventType implements IEventType {
	ARRIVAL, END_REGISTRATION, END_BIOMETRICS, END_DOC_CHECK, END_DECISION, DEP1, DEP2, DEP3, DEP4;
}
