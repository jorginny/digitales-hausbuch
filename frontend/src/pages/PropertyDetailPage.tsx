import { useEffect, useState } from "react";
import { useParams } from "react-router";

import {
  getProperty,
  updateProperty,
  type PropertyResponse,
} from "../services/propertyService";

import {
  getRooms,
  createRoom,
  updateRoom,
  deleteRoom,
  type RoomResponse,
} from "../services/roomService";

import {
  createHouseholdObject,
} from "../services/householdObjectService";

function PropertyDetailPage() {
  const { id } = useParams();

  const [property, setProperty] =
    useState<PropertyResponse | null>(null);

  const [name, setName] = useState("");
  const [address, setAddress] = useState("");

  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  // Räume
  const [rooms, setRooms] = useState<RoomResponse[]>([]);
  const [newRoomName, setNewRoomName] = useState("");

  const [editingRoomId, setEditingRoomId] =
    useState<number | null>(null);

  const [editingRoomName, setEditingRoomName] =
    useState("");

  // Household Objects
  const [selectedRoomId, setSelectedRoomId] =
    useState<number | null>(null);

  const [objectName, setObjectName] = useState("");
  const [objectDescription, setObjectDescription] = useState("");
  const [objectManufacturer, setObjectManufacturer] = useState("");
  const [objectModel, setObjectModel] = useState("");
  const [objectPurchaseDate, setObjectPurchaseDate] = useState("");

  useEffect(() => {
    const loadProperty = async () => {
      if (!id) {
        setError("Keine Immobilien-ID vorhanden.");
        return;
      }

      try {
        const loadedProperty =
          await getProperty(Number(id));

        setProperty(loadedProperty);
        setName(loadedProperty.name);
        setAddress(loadedProperty.address ?? "");

        const loadedRooms =
          await getRooms(Number(id));

        setRooms(loadedRooms);
      } catch (error) {
        if (error instanceof Error) {
          setError(error.message);
        }
      }
    };

    loadProperty();
  }, [id]);

  const handleSubmit = async (
    event: React.SubmitEvent<HTMLFormElement>
  ) => {
    event.preventDefault();

    if (!id) {
      return;
    }

    setMessage("");
    setError("");

    try {
      const updatedProperty =
        await updateProperty(Number(id), {
          name,
          address,
        });

      setProperty(updatedProperty);

      setMessage(
        "Immobilie wurde erfolgreich aktualisiert."
      );
    } catch (error) {
      if (error instanceof Error) {
        setError(error.message);
      }
    }
  };

  const handleCreateRoom = async (
    event: React.SubmitEvent<HTMLFormElement>
  ) => {
    event.preventDefault();

    if (!id) {
      return;
    }

    setError("");
    setMessage("");

    try {
      const createdRoom = await createRoom(
        Number(id),
        {
          name: newRoomName,
        }
      );

      setRooms((currentRooms) => [
        ...currentRooms,
        createdRoom,
      ]);

      setNewRoomName("");

      setMessage(
        "Raum wurde erfolgreich angelegt."
      );
    } catch (error) {
      if (error instanceof Error) {
        setError(error.message);
      }
    }
  };

  const handleEditRoom = (room: RoomResponse) => {
    setEditingRoomId(room.id);
    setEditingRoomName(room.name);
  };

  const handleUpdateRoom = async (
    roomId: number
  ) => {
    if (!id) {
      return;
    }

    setError("");
    setMessage("");

    try {
      const updatedRoom = await updateRoom(
        Number(id),
        roomId,
        {
          name: editingRoomName,
        }
      );

      setRooms((currentRooms) =>
        currentRooms.map((room) =>
          room.id === roomId
            ? updatedRoom
            : room
        )
      );

      setEditingRoomId(null);
      setEditingRoomName("");

      setMessage(
        "Raum wurde erfolgreich aktualisiert."
      );
    } catch (error) {
      if (error instanceof Error) {
        setError(error.message);
      }
    }
  };

  const handleDeleteRoom = async (
    roomId: number
  ) => {
    if (!id) {
      return;
    }

    const confirmed = window.confirm(
      "Möchtest du diesen Raum wirklich löschen?"
    );

    if (!confirmed) {
      return;
    }

    setError("");
    setMessage("");

    try {
      await deleteRoom(Number(id), roomId);

      setRooms((currentRooms) =>
        currentRooms.filter(
          (room) => room.id !== roomId
        )
      );

      setMessage(
        "Raum wurde erfolgreich gelöscht."
      );
    } catch (error) {
      if (error instanceof Error) {
        setError(error.message);
      }
    }
  };

  const handleCreateHouseholdObject = async (
    event: React.SubmitEvent<HTMLFormElement>
  ) => {
    event.preventDefault();

    if (!id || selectedRoomId === null) {
      setError("Bitte zuerst einen Raum auswählen.");
      return;
    }

    setError("");
    setMessage("");

    try {
      await createHouseholdObject(
        Number(id),
        selectedRoomId,
        {
          name: objectName,
          description: objectDescription,
          manufacturer: objectManufacturer,
          model: objectModel,
          purchaseDate:
            objectPurchaseDate || undefined,
        }
      );

      setObjectName("");
      setObjectDescription("");
      setObjectManufacturer("");
      setObjectModel("");
      setObjectPurchaseDate("");

      setMessage(
        "Objekt wurde erfolgreich angelegt."
      );
    } catch (error) {
      if (error instanceof Error) {
        setError(error.message);
      }
    }
  };

  if (!property && !error) {
    return <p>Immobilie wird geladen...</p>;
  }

  return (
    <div>
      <h1>Immobilie bearbeiten</h1>

      {error && <p>{error}</p>}

      {property && (
        <form onSubmit={handleSubmit}>
          <div>
            <label htmlFor="name">
              Bezeichnung
            </label>

            <input
              id="name"
              type="text"
              value={name}
              onChange={(event) =>
                setName(event.target.value)
              }
            />
          </div>

          <div>
            <label htmlFor="address">
              Adresse
            </label>

            <input
              id="address"
              type="text"
              value={address}
              onChange={(event) =>
                setAddress(event.target.value)
              }
            />
          </div>

          <button type="submit">
            Änderungen speichern
          </button>
        </form>
      )}

      <h2>Räume</h2>

      {rooms.length === 0 ? (
        <p>Noch keine Räume angelegt.</p>
      ) : (
        <ul>
          {rooms.map((room) => (
            <li key={room.id}>
              {editingRoomId === room.id ? (
                <>
                  <input
                    type="text"
                    value={editingRoomName}
                    onChange={(event) =>
                      setEditingRoomName(
                        event.target.value
                      )
                    }
                  />

                  <button
                    type="button"
                    onClick={() =>
                      handleUpdateRoom(room.id)
                    }
                  >
                    Speichern
                  </button>

                  <button
                    type="button"
                    onClick={() => {
                      setEditingRoomId(null);
                      setEditingRoomName("");
                    }}
                  >
                    Abbrechen
                  </button>
                </>
              ) : (
                <>
                  <span>{room.name}</span>

                  <button
                    type="button"
                    onClick={() =>
                      handleEditRoom(room)
                    }
                  >
                    Bearbeiten
                  </button>

                  <button
                    type="button"
                    onClick={() =>
                      handleDeleteRoom(room.id)
                    }
                  >
                    Löschen
                  </button>
                </>
              )}
            </li>
          ))}
        </ul>
      )}

      <form onSubmit={handleCreateRoom}>
        <div>
          <label htmlFor="roomName">
            Neuer Raum
          </label>

          <input
            id="roomName"
            type="text"
            value={newRoomName}
            onChange={(event) =>
              setNewRoomName(event.target.value)
            }
            required
          />
        </div>

        <button type="submit">
          Raum anlegen
        </button>
      </form>

      <h2>Objekt anlegen</h2>

      <form onSubmit={handleCreateHouseholdObject}>
        <div>
          <label htmlFor="objectRoom">
            Raum
          </label>

          <select
            id="objectRoom"
            value={selectedRoomId ?? ""}
            onChange={(event) => {
              const value = event.target.value;

              setSelectedRoomId(
                value === ""
                  ? null
                  : Number(value)
              );
            }}
          >
            <option value="">
              Raum auswählen
            </option>

            {rooms.map((room) => (
              <option
                key={room.id}
                value={room.id}
              >
                {room.name}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label htmlFor="objectName">
            Name
          </label>

          <input
            id="objectName"
            type="text"
            value={objectName}
            onChange={(event) =>
              setObjectName(event.target.value)
            }
            required
          />
        </div>

        <div>
          <label htmlFor="objectDescription">
            Beschreibung
          </label>

          <input
            id="objectDescription"
            type="text"
            value={objectDescription}
            onChange={(event) =>
              setObjectDescription(
                event.target.value
              )
            }
          />
        </div>

        <div>
          <label htmlFor="objectManufacturer">
            Hersteller
          </label>

          <input
            id="objectManufacturer"
            type="text"
            value={objectManufacturer}
            onChange={(event) =>
              setObjectManufacturer(
                event.target.value
              )
            }
          />
        </div>

        <div>
          <label htmlFor="objectModel">
            Modell
          </label>

          <input
            id="objectModel"
            type="text"
            value={objectModel}
            onChange={(event) =>
              setObjectModel(event.target.value)
            }
          />
        </div>

        <div>
          <label htmlFor="objectPurchaseDate">
            Kaufdatum
          </label>

          <input
            id="objectPurchaseDate"
            type="date"
            value={objectPurchaseDate}
            onChange={(event) =>
              setObjectPurchaseDate(
                event.target.value
              )
            }
          />
        </div>

        <button type="submit">
          Objekt anlegen
        </button>
      </form>

      {message && <p>{message}</p>}
    </div>
  );
}

export default PropertyDetailPage;