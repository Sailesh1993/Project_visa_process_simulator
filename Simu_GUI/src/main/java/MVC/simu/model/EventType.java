package MVC.simu.model;

import MVC.simu.framework.IEventType;

public enum EventType implements IEventType {

    ARRIVAL("Application arrives in system!"),
    END_APPLICATION_ENTRY("Application entry and appointment booking completed."),
    END_DOC_SUBMISSION("Document submission and interview completed."),
    END_BIOMETRICS("Biometrics collection completed."),
    END_DOC_CHECK("Document verification and background check completed."),
    END_DECISION("Decision room processing completed."),
    MISSING_DOCS_RESOLVED("Missing documents provided and resolved."),
    REAPPLICATION("Application resubmitted after rejection."),
    EXIT_APPROVED("Application approved and exited system."),
    EXIT_REJECTED("Application rejected and exited system.");

    private final String description;

    EventType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isServiceCompletion() {
        return this.name().startsWith("END_") &&
                !this.equals(END_DECISION);
    }

    public boolean isSystemEntry() {
        return this.equals(ARRIVAL) || this.equals(REAPPLICATION);
    }

    public boolean isSystemExit() {
        return this.equals(EXIT_APPROVED) || this.equals(EXIT_REJECTED);
    }

    public boolean isConditionalEvent() {
        return this.equals(END_BIOMETRICS) || this.equals(MISSING_DOCS_RESOLVED);
    }

    // Map enum to Service Point index
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

    // Human-friendly Service Point names
    private static final String[] SERVICE_POINT_NAMES = {
            "Application Entry & Appointment Booking", // SP1
            "Document Submission Room",                // SP2
            "Biometrics Room",                         // SP2a
            "Missing Docs Waiting Room",               // SP2b
            "Document Check Room",                     // SP3
            "Decision Room"                            // SP4
    };

    /**
     * Returns a human-friendly Service Point name for this event,
     * or null if the event is not associated with a service point.
     */
    public String getServicePointName() {
        int index = getServicePointIndex();
        if (index >= 0 && index < SERVICE_POINT_NAMES.length) {
            return SERVICE_POINT_NAMES[index];
        } else {
            return null; // or "Unknown Service Point"
        }
    }

    /**
     * Legacy display name (enum name converted to lowercase with spaces)
     */
    public String getDisplayName() {
        return name().toLowerCase().replace('_', ' ');
    }
}
