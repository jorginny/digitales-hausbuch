package digitales_hausbuch_backend.householdobject;

import java.time.LocalDate;

public record HouseholdObjectResponse(

        Long id,

        String name,

        String description,

        String manufacturer,

        String model,

        LocalDate purchaseDate,

        Long roomId

) {
}
