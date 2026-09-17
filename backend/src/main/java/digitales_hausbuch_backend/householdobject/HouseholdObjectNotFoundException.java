package digitales_hausbuch_backend.householdobject;

public class HouseholdObjectNotFoundException
        extends RuntimeException {

    public HouseholdObjectNotFoundException(
            String message) {
        super(message);
    }
}
