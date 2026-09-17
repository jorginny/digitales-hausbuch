package digitales_hausbuch_backend.householdobject;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record HouseholdObjectRequest(

        @NotBlank
        String name,

        String description,

        String manufacturer,

        String model,

        LocalDate purchaseDate

) {
}
