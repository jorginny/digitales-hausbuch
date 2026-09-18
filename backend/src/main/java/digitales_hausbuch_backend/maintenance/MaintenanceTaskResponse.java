package digitales_hausbuch_backend.maintenance;

import java.time.LocalDate;

public record MaintenanceTaskResponse(

        Long id,

        String title,

        String description,

        LocalDate dueDate,

        boolean completed,

        Integer recurrenceInterval,

        RecurrenceUnit recurrenceUnit,

        Long householdObjectId

) {
}
