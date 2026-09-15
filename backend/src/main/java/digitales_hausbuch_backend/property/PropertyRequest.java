package digitales_hausbuch_backend.property;

import jakarta.validation.constraints.NotBlank;

public record PropertyRequest(

        @NotBlank
        String name,

        String address

) {
}
