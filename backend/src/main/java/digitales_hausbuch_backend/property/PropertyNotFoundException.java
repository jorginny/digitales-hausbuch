package digitales_hausbuch_backend.property;

public class PropertyNotFoundException extends RuntimeException {

    public PropertyNotFoundException(String message) {
        super(message);
    }
}
