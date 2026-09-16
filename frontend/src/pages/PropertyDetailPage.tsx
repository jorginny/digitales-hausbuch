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

function PropertyDetailPage() {
  const { id } = useParams();

  const [property, setProperty] =
    useState<PropertyResponse | null>(null);

  const [name, setName] = useState("");
  const [address, setAddress] = useState("");

  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const [rooms, setRooms] = useState<RoomResponse[]>([]);
  const [newRoomName, setNewRoomName] = useState("");

  const [editingRoomId, setEditingRoomId] =
    useState<number | null>(null);

  const [editingRoomName, setEditingRoomName] =
    useState("");

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

    try {
      await deleteRoom(Number(id), roomId);

      setRooms((currentRooms) =>
        currentRooms.filter(
          (room) => room.id !== roomId
        )
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
        />

        <button type="submit">
          Raum anlegen
        </button>
      </form>

      {message && <p>{message}</p>}
    </div>
  );
}

export default PropertyDetailPage;