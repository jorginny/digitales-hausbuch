import { useState } from "react";
import { Link, useParams } from "react-router";

import PropertyForm from "../components/PropertyForm";
import RoomSection from "../components/RoomSection";
import HouseholdObjectSection from "../components/HouseholdObjectSection";
import MaintenanceTaskSection from "../components/MaintenanceTaskSection";

function PropertyDetailPage() {
  const { id } = useParams();

  const [selectedRoomId, setSelectedRoomId] =
    useState<number | null>(null);

  const [
    selectedObjectId,
    setSelectedObjectId,
  ] = useState<number | null>(null);

  if (!id) {
    return (
      <p>
        Keine Immobilien-ID vorhanden.
      </p>
    );
  }

  const propertyId = Number(id);

  const handleSelectRoom = (
    roomId: number | null
  ) => {
    setSelectedRoomId(roomId);

    // Wenn sich der Raum ändert,
    // gehört das vorher ausgewählte Objekt
    // nicht mehr unbedingt zum neuen Raum.
    setSelectedObjectId(null);
  };

  return (
    <div>
      <h1>Immobilie verwalten</h1>

      <Link
        to={`/properties/${propertyId}/maintenance`}
      >
        Zur Wartungsübersicht
      </Link>

      <PropertyForm
        propertyId={propertyId}
      />

      <hr />

      <RoomSection
        propertyId={propertyId}
        selectedRoomId={
          selectedRoomId
        }
        onSelectRoom={
          handleSelectRoom
        }
      />

      <hr />

      <HouseholdObjectSection
        propertyId={propertyId}
        selectedRoomId={
          selectedRoomId
        }
        selectedObjectId={
          selectedObjectId
        }
        onSelectObject={
          setSelectedObjectId
        }
      />

      <hr />

      <MaintenanceTaskSection
        propertyId={propertyId}
        selectedRoomId={
          selectedRoomId
        }
        selectedObjectId={
          selectedObjectId
        }
      />
    </div>
  );
}

export default PropertyDetailPage;