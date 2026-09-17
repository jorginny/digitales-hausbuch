package digitales_hausbuch_backend.maintenance;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record MaintenanceTaskRequest(

        @NotBlank
        String title,

        String description,

        LocalDate dueDate

) {
}
