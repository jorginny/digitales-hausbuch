package digitales_hausbuch_backend.maintenance;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/properties/{propertyId}/maintenance-tasks")
public class MaintenanceOverviewController {

    private final MaintenanceTaskService maintenanceTaskService;

    public MaintenanceOverviewController(
            MaintenanceTaskService maintenanceTaskService
    ) {
        this.maintenanceTaskService = maintenanceTaskService;
    }

    @GetMapping
    public ResponseEntity<List<MaintenanceOverviewResponse>>
    getOpenMaintenanceTasks(
            @PathVariable Long propertyId,
            Authentication authentication
    ) {
        List<MaintenanceOverviewResponse> tasks =
                maintenanceTaskService.getOpenMaintenanceTasks(
                        propertyId,
                        authentication.getName()
                );

        return ResponseEntity.ok(tasks);
    }
}
