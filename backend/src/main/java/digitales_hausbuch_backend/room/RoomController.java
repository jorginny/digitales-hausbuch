package digitales_hausbuch_backend.room;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/properties/{propertyId}/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(
            @PathVariable Long propertyId,
            @Valid @RequestBody RoomRequest request,
            Authentication authentication) {

        RoomResponse response = roomService.createRoom(
                propertyId,
                request,
                authentication.getName()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<RoomResponse>> getRooms(
            @PathVariable Long propertyId,
            Authentication authentication) {

        List<RoomResponse> rooms = roomService.getRooms(
                propertyId,
                authentication.getName()
        );

        return ResponseEntity.ok(rooms);
    }

    @PutMapping("/{roomId}")
    public ResponseEntity<RoomResponse> updateRoom(
            @PathVariable Long propertyId,
            @PathVariable Long roomId,
            @Valid @RequestBody RoomRequest request,
            Authentication authentication) {

        RoomResponse response = roomService.updateRoom(
                propertyId,
                roomId,
                request,
                authentication.getName()
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(
            @PathVariable Long propertyId,
            @PathVariable Long roomId,
            Authentication authentication) {

        roomService.deleteRoom(
                propertyId,
                roomId,
                authentication.getName()
        );

        return ResponseEntity.noContent().build();
    }
}
