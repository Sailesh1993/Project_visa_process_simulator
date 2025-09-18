package simu.model;
import simu.framework.IEventType;

public enum EventType implements IEventType {

    ARRIVAL("Application arrives in system"),

    END_APPLICATION_ENTRY("Application entry and appointment booking completed"),

    END_DOC_SUBMISSION("Document submission and interview completed"),

    END_BIOMETRICS("Biometrics collection completed"),

    END_DOC_CHECK("Document verification and background check completed"),

    END_DECISION("Decision room processing completed"),

    MISSING_DOCS_RESOLVED("Missing documents provided and resolved"),

    REAPPLICATION("Application resubmitted after rejection"),

    EXIT_APPROVED("Application approved and exited system"),

    EXIT_REJECTED("Application rejected and exited system");

    // Description field for better debugging and logging
    private final String description;


    //Constructor for EventType enum
    EventType(String description) {
        this.description = description;
    }

    //Get the description of this event type
    public String getDescription() {
        return description;
    }

    //Check if this event represents a service completion
    public boolean isServiceCompletion() {
        return this.name().startsWith("END_") &&
                !this.equals(END_DECISION);
    }

    //Check if this event represents system entry
    public boolean isSystemEntry() {
        return this.equals(ARRIVAL) || this.equals(REAPPLICATION);
    }

    //Check if this event represents system exit
    public boolean isSystemExit() {
        return this.equals(EXIT_APPROVED) || this.equals(EXIT_REJECTED);
    }

    //Check if this event is conditional based on application requirements
    public boolean isConditionalEvent() {
        return this.equals(END_BIOMETRICS) || this.equals(MISSING_DOCS_RESOLVED);
    }

    //Get the next possible events that can follow this event
    public EventType[] getPossibleNextEvents() {
        switch (this) {
            case ARRIVAL:
                return new EventType[]{END_APPLICATION_ENTRY};

            case END_APPLICATION_ENTRY:
                return new EventType[]{END_DOC_SUBMISSION};

            case END_DOC_SUBMISSION:
                return new EventType[]{END_BIOMETRICS, END_DOC_CHECK, MISSING_DOCS_RESOLVED};

            case END_BIOMETRICS:
                return new EventType[]{END_DOC_CHECK};

            case END_DOC_CHECK:
                return new EventType[]{END_DECISION};

            case END_DECISION:
                return new EventType[]{EXIT_APPROVED, REAPPLICATION};

            case MISSING_DOCS_RESOLVED:
                return new EventType[]{END_DOC_CHECK};

            case REAPPLICATION:
                return new EventType[]{END_DOC_SUBMISSION};

            case EXIT_APPROVED:
            case EXIT_REJECTED:
                return new EventType[]{};

            default:
                return new EventType[]{};
        }
    }

    //Get the service point number associated with this event
    public int getServicePointIndex() {
        switch (this) {
            case END_APPLICATION_ENTRY:
                return 0; // SP1: Application Entry & Appointment Booking
            case END_DOC_SUBMISSION:
                return 1; // SP2: Doc Submission room - Interviews
            case END_BIOMETRICS:
                return 2; // SP2a: Biometrics room (sub-process of SP2)
            case MISSING_DOCS_RESOLVED:
                return 3; // SP2b: Missing Doc waiting room (sub-process of SP2)
            case END_DOC_CHECK:
                return 4; // SP3: Doc check room - Backgrounds, verifications
            case END_DECISION:
                return 5; // SP4: Decision room - Approve/Reject

            default:
                return -1; // Not associated with a specific service point
        }
    }

    //Returns a formatted string representation
    @Override
    public String toString() {
        return String.format("EventType.%s (%s)", this.name(), this.description);
    }

    //Get a simple display name for UI purposes
    public String getDisplayName() {
        return name().toLowerCase()
                .replace('_', ' ')
                .replaceFirst("end ", "")
                .replaceFirst("departure ", "");
    }
}