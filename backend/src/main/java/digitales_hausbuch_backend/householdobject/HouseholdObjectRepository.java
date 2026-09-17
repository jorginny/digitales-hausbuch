package digitales_hausbuch_backend.householdobject;

import digitales_hausbuch_backend.room.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HouseholdObjectRepository
        extends JpaRepository<HouseholdObject, Long> {

    List<HouseholdObject> findByRoom(Room room);

    Optional<HouseholdObject> findByIdAndRoom(
            Long id,
            Room room
    );
}
