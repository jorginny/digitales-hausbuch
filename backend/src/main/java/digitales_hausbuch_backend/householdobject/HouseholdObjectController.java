package digitales_hausbuch_backend.householdobject;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/properties/{propertyId}/rooms/{roomId}/objects"
)
public class HouseholdObjectController {

    private final HouseholdObjectService householdObjectService;

    public HouseholdObjectController(
            HouseholdObjectService householdObjectService) {

        this.householdObjectService = householdObjectService;
    }

    @PostMapping
    public ResponseEntity<HouseholdObjectResponse> createHouseholdObject(
            @PathVariable Long propertyId,
            @PathVariable Long roomId,
            @Valid @RequestBody HouseholdObjectRequest request,
            Authentication authentication) {

        HouseholdObjectResponse response =
                householdObjectService.createHouseholdObject(
                        propertyId,
                        roomId,
                        request,
                        authentication.getName()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<HouseholdObjectResponse>> getHouseholdObjects(
            @PathVariable Long propertyId,
            @PathVariable Long roomId,
            Authentication authentication) {

        List<HouseholdObjectResponse> objects =
                householdObjectService.getHouseholdObjects(
                        propertyId,
                        roomId,
                        authentication.getName()
                );

        return ResponseEntity.ok(objects);
    }

    @PutMapping("/{objectId}")
    public ResponseEntity<HouseholdObjectResponse> updateHouseholdObject(
            @PathVariable Long propertyId,
            @PathVariable Long roomId,
            @PathVariable Long objectId,
            @Valid @RequestBody HouseholdObjectRequest request,
            Authentication authentication) {

        HouseholdObjectResponse response =
                householdObjectService.updateHouseholdObject(
                        propertyId,
                        roomId,
                        objectId,
                        request,
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }
}
