package digitales_hausbuch_backend.maintenance;

public class MaintenanceTaskNotFoundException
        extends RuntimeException {

    public MaintenanceTaskNotFoundException(
            String message) {
        super(message);
    }
}
