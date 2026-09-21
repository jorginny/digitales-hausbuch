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

    @PutMapping("/{taskId}")
    public ResponseEntity<MaintenanceTaskResponse>
    updateMaintenanceTask(
            @PathVariable Long propertyId,
            @PathVariable Long roomId,
            @PathVariable Long objectId,
            @PathVariable Long taskId,
            @Valid @RequestBody MaintenanceTaskRequest request,
            Authentication authentication) {

        MaintenanceTaskResponse response =
                maintenanceTaskService
                        .updateMaintenanceTask(
                                propertyId,
                                roomId,
                                objectId,
                                taskId,
                                request,
                                authentication.getName()
                        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{taskId}/complete")
    public ResponseEntity<MaintenanceTaskResponse> completeMaintenanceTask(
            @PathVariable Long propertyId,
            @PathVariable Long roomId,
            @PathVariable Long objectId,
            @PathVariable Long taskId,
            @RequestBody(required = false)
            CompleteMaintenanceTaskRequest request,
            Authentication authentication
    ) {

        String note =
                request != null
                        ? request.note()
                        : null;

        MaintenanceTaskResponse response =
                maintenanceTaskService.completeMaintenanceTask(
                        propertyId,
                        roomId,
                        objectId,
                        taskId,
                        note,
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{taskId}/history")
    public ResponseEntity<List<MaintenanceRecordResponse>> getMaintenanceHistory(
            @PathVariable Long propertyId,
            @PathVariable Long roomId,
            @PathVariable Long objectId,
            @PathVariable Long taskId,
            Authentication authentication
    ) {

        List<MaintenanceRecordResponse> history =
                maintenanceTaskService.getMaintenanceHistory(
                        propertyId,
                        roomId,
                        objectId,
                        taskId,
                        authentication.getName()
                );

        return ResponseEntity.ok(history);
    }


}
