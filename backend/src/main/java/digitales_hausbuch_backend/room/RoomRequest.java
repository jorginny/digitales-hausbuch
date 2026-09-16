package digitales_hausbuch_backend.room;

import jakarta.validation.constraints.NotBlank;

public record RoomRequest(

        @NotBlank
        String name

) {
}
