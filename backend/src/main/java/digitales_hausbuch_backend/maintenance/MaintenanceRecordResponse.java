package digitales_hausbuch_backend.maintenance;

import java.time.LocalDate;

public record MaintenanceRecordResponse(
        Long id,
        LocalDate completedAt,
        String note,
        Long maintenanceTaskId
) {
}
