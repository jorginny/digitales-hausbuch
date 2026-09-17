package digitales_hausbuch_backend.maintenance;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/properties/{propertyId}/rooms/{roomId}/objects/{objectId}/maintenance-tasks"
)
public class MaintenanceTaskController {

    private final MaintenanceTaskService maintenanceTaskService;

    public MaintenanceTaskController(
            MaintenanceTaskService maintenanceTaskService) {

        this.maintenanceTaskService = maintenanceTaskService;
    }

    @PostMapping
    public ResponseEntity<MaintenanceTaskResponse> createMaintenanceTask(
            @PathVariable Long propertyId,
            @PathVariable Long roomId,
            @PathVariable Long objectId,
            @Valid @RequestBody MaintenanceTaskRequest request,
            Authentication authentication) {

        MaintenanceTaskResponse response =
                maintenanceTaskService.createMaintenanceTask(
                        propertyId,
                        roomId,
                        objectId,
                        request,
                        authentication.getName()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<MaintenanceTaskResponse>> getMaintenanceTasks(
            @PathVariable Long propertyId,
            @PathVariable Long roomId,
            @PathVariable Long objectId,
            Authentication authentication) {

        List<MaintenanceTaskResponse> tasks =
                maintenanceTaskService.getMaintenanceTasks(
                        propertyId,
                        roomId,
                        objectId,
                        authentication.getName()
                );

        return ResponseEntity.ok(tasks);
    }
}
