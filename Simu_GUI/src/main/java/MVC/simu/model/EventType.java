package MVC.simu.model;

import MVC.simu.framework.IEventType;

/**
 * Represents the different types of events that can occur in the visa/immigration
 * application processing simulation.
 *
 * <p>This enum implements {@link IEventType} and categorizes events in the workflow,
 * including system entry, service completion, system exit, and conditional events.
 * Each event has a human-readable description and can be mapped to a specific service
 * point in the processing workflow.</p>
 *
 * <p>Event categories:</p>
 * <ul>
 *     <li>System entry events: {@link #ARRIVAL}, {@link #REAPPLICATION}</li>
 *     <li>Service completion events: {@link #END_APPLICATION_ENTRY}, {@link #END_DOC_SUBMISSION}, {@link #END_BIOMETRICS}, {@link #END_DOC_CHECK}, {@link #END_DECISION}</li>
 *     <li>System exit events: {@link #EXIT_APPROVED}, {@link #EXIT_REJECTED}</li>
 *     <li>Conditional events: {@link #MISSING_DOCS_RESOLVED}, {@link #END_BIOMETRICS}</li>
 * </ul>
 *
 * See {@link IEventType}
 */
public enum EventType implements IEventType {

    /**
     * Event triggered when a new application arrives in the system.
     * This marks the entry point for a new customer into the workflow.
     */
    ARRIVAL("Application arrives in system!"),

    /**
     * Event triggered when an application entry and appointment booking
     * process is completed at Service Point 1.
     */
    END_APPLICATION_ENTRY("Application entry and appointment booking completed."),

    /**
     * Event triggered when document submission and interview process
     * is completed at Service Point 2.
     */
    END_DOC_SUBMISSION("Document submission and interview completed."),

    /**
     * Event triggered when biometrics collection is completed.
     * This is a conditional event that may require additional processing.
     */
    END_BIOMETRICS("Biometrics collection completed."),

    /**
     * Event triggered when document verification and background check
     * process is completed at Service Point 3.
     */

    END_DOC_CHECK("Document verification and background check completed."),

    /**
     * Event triggered when decision room processing is completed at Service Point 4.
     * This typically results in either approval or rejection.
     */
    END_DECISION("Decision room processing completed."),

    /**
     * Event triggered when missing documents are provided and resolved.
     * This is a conditional event that allows re-processing of incomplete applications.
     */
    MISSING_DOCS_RESOLVED("Missing documents provided and resolved."),

    /**
     * Event triggered when a previously rejected application is resubmitted
     * to the system for reconsideration.
     */
    REAPPLICATION("Application resubmitted after rejection."),

    /**
     * Event triggered when an application is approved and exits the system successfully.
     */
    EXIT_APPROVED("Application approved and exited system."),

    /**
     * Event triggered when an application is rejected and exits the system.
     */
    EXIT_REJECTED("Application rejected and exited system.");

    /** Human-readable description of this event type. */
    private final String description;

    /**
     * Constructs an EventType with the specified description.
     *
     * @param description a human-readable description of the event
     */
    EventType(String description) {
        this.description = description;
    }

    /**
     * Returns the human-readable description of this event type.
     *
     * @return the event description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Determines if this event represents the completion of a service
     * at a service point.
     *
     * <p>Service completion events start with "END_" but exclude {@link #END_DECISION},
     * which requires special handling.</p>
     *
     * @return true if this event represents service completion, false otherwise
     */
    public boolean isServiceCompletion() {
        return this.name().startsWith("END_") &&
                !this.equals(END_DECISION);
    }

    /**
     * Determines if this event represents an entry point into the system.
     * <p>
     * System entry events are new applications arriving or reapplications
     * being resubmitted to the system.</p>
     *
     * @return true if this event marks system entry, false otherwise
     */
    public boolean isSystemEntry() {
        return this.equals(ARRIVAL) || this.equals(REAPPLICATION);
    }

    /**
     * Determines if this event represents an exit from the system.
     * <p>
     * System exit events occur when an application either receives final
     * approval or rejection and leaves the workflow.</p>
     *
     * @return true if this event marks system exit, false otherwise
     */
    public boolean isSystemExit() {
        return this.equals(EXIT_APPROVED) || this.equals(EXIT_REJECTED);
    }

    /**
     * Determines if this event is conditional and may require
     * additional processing or routing decisions.
     * <p>
     * Conditional events include biometrics completion and missing documents
     * resolution, which may result in different workflow paths.</p>
     *
     * @return true if this event is conditional, false otherwise
     */
    public boolean isConditionalEvent() {
        return this.equals(END_BIOMETRICS) || this.equals(MISSING_DOCS_RESOLVED);
    }

    /**
     * Maps this event type to the corresponding service point index in the workflow.
     *
     * <p>Service points (0-5) correspond to:</p>
     * <ul>
     *     <li>0: SP1 - Application Entry & Appointment Booking</li>
     *     <li>1: SP2 - Document Submission Room</li>
     *     <li>2: SP2a - Biometrics Room</li>
     *     <li>3: SP2b - Missing Docs Waiting Room</li>
     *     <li>4: SP3 - Document Check Room</li>
     *     <li>5: SP4 - Decision Room</li>
     * </ul>
     *
     * @return the service point index (0-5) if associated, or -1 if not associated with a specific service point
     */
    public int getServicePointIndex() {
        return switch (this) {
            case END_APPLICATION_ENTRY -> 0; // SP1
            case END_DOC_SUBMISSION -> 1;    // SP2
            case END_BIOMETRICS -> 2;        // SP2a
            case MISSING_DOCS_RESOLVED -> 3; // SP2b
            case END_DOC_CHECK -> 4;         // SP3
            case END_DECISION -> 5;          // SP4
            default -> -1;                   // Not associated
        };
    }

    /**
     * Human-friendly Service Point names corresponding to service point indices.
     */
    private static final String[] SERVICE_POINT_NAMES = {
            "Application Entry & Appointment Booking", // SP1
            "Document Submission Room",                // SP2
            "Biometrics Room",                         // SP2a
            "Missing Docs Waiting Room",               // SP2b
            "Document Check Room",                     // SP3
            "Decision Room"                            // SP4
    };

    /**
     * Returns a human-friendly Service Point name for this event.
     * <p>
     * The name is retrieved from the SERVICE_POINT_NAMES array using the
     * service point index. This provides a descriptive name for display
     * purposes in the UI or reports.</p>
     *
     * @return a descriptive service point name if this event is associated
     *         with a service point, or null if the event is not associated
     *         with a specific service point
     *
     * @see #getServicePointIndex()
     */
    public String getServicePointName() {
        int index = getServicePointIndex();
        if (index >= 0 && index < SERVICE_POINT_NAMES.length) {
            return SERVICE_POINT_NAMES[index];
        } else {
            return null; // or "Unknown Service Point"
        }
    }
}
