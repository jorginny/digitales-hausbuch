package digitales_hausbuch_backend.room;

import digitales_hausbuch_backend.property.Property;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByProperty(Property property);

    Optional<Room> findByIdAndProperty(Long id, Property property);
}
