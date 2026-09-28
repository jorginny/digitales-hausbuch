package digitales_hausbuch_backend.property;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @PostMapping
    public ResponseEntity<PropertyResponse> createProperty(
            @Valid @RequestBody PropertyRequest request,
            Authentication authentication) {

        PropertyResponse response =
                propertyService.createProperty(
                        request,
                        authentication.getName()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyResponse> getProperty(
            @PathVariable Long id,
            Authentication authentication) {

        PropertyResponse response =
                propertyService.getProperty(
                        id,
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PropertyResponse> updateProperty(
            @PathVariable Long id,
            @Valid @RequestBody PropertyRequest request,
            Authentication authentication) {

        PropertyResponse response =
                propertyService.updateProperty(
                        id,
                        request,
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PropertyResponse>> getProperties(
            Authentication authentication) {

        List<PropertyResponse> properties =
                propertyService.getPropertiesForUser(
                        authentication.getName()
                );

        return ResponseEntity.ok(properties);
    }

    @DeleteMapping("/{propertyId}")
    public ResponseEntity<Void> deleteProperty(
            @PathVariable Long propertyId,
            Authentication authentication
    ) {
        propertyService.deleteProperty(
                propertyId,
                authentication.getName()
        );

        return ResponseEntity.noContent().build();
    }
}
