package digitales_hausbuch_backend.property;

import java.time.LocalDateTime;

public record PropertyResponse(
        Long id,
        String name,
        String address,
        LocalDateTime createdAt
) {
}
