package digitales_hausbuch_backend.property;

import digitales_hausbuch_backend.user.User;
import digitales_hausbuch_backend.user.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    public PropertyService(
            PropertyRepository propertyRepository,
            UserRepository userRepository) {

        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
    }

    public PropertyResponse createProperty(
            PropertyRequest request,
            String userEmail) {

        User owner = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Benutzer wurde nicht gefunden."
                        )
                );

        Property property = new Property(
                request.name(),
                request.address(),
                owner
        );

        Property savedProperty =
                propertyRepository.save(property);

        return new PropertyResponse(
                savedProperty.getId(),
                savedProperty.getName(),
                savedProperty.getAddress(),
                savedProperty.getCreatedAt()
        );
    }
}
