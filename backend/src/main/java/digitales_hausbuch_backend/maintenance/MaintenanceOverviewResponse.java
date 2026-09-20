package digitales_hausbuch_backend.maintenance;

import java.time.LocalDate;

public record MaintenanceOverviewResponse(
        Long taskId,
        String title,
        String description,
        LocalDate dueDate,
        LocalDate completedAt,
        Integer recurrenceInterval,
        RecurrenceUnit recurrenceUnit,
        Long objectId,
        String objectName,
        Long roomId,
        String roomName
) {
}
