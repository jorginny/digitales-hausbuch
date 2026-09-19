import { useEffect, useState } from "react";

import {
  getRooms,
  createRoom,
  updateRoom,
  deleteRoom,
  type RoomResponse,
} from "../services/roomService";

type RoomSectionProps = {
  propertyId: number;
  selectedRoomId: number | null;
  onSelectRoom: (roomId: number | null) => void;
};

function RoomSection({
  propertyId,
  selectedRoomId,
  onSelectRoom,
}: RoomSectionProps) {
  const [rooms, setRooms] =
    useState<RoomResponse[]>([]);

  const [newRoomName, setNewRoomName] =
    useState("");

  const [editingRoomId, setEditingRoomId] =
    useState<number | null>(null);

  const [editingRoomName, setEditingRoomName] =
    useState("");

  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    const loadRooms = async () => {
      setError("");

      try {
        const loadedRooms =
          await getRooms(propertyId);

        setRooms(loadedRooms);
      } catch (error) {
        if (error instanceof Error) {
          setError(error.message);
        }
      }
    };

    loadRooms();
  }, [propertyId]);

  const handleCreateRoom = async (
    event: React.SubmitEvent<HTMLFormElement>
  ) => {
    event.preventDefault();

    setMessage("");
    setError("");

    try {
      const createdRoom =
        await createRoom(propertyId, {
          name: newRoomName,
        });

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

  const handleEditRoom = (
    room: RoomResponse
  ) => {
    setEditingRoomId(room.id);
    setEditingRoomName(room.name);
  };

  const handleUpdateRoom = async (
    roomId: number
  ) => {
    setMessage("");
    setError("");

    try {
      const updatedRoom =
        await updateRoom(
          propertyId,
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
    const confirmed = window.confirm(
      "Möchtest du diesen Raum wirklich löschen?"
    );

    if (!confirmed) {
      return;
    }

    setMessage("");
    setError("");

    try {
      await deleteRoom(propertyId, roomId);

      setRooms((currentRooms) =>
        currentRooms.filter(
          (room) => room.id !== roomId
        )
      );

      if (selectedRoomId === roomId) {
        onSelectRoom(null);
      }

      setMessage(
        "Raum wurde erfolgreich gelöscht."
      );
    } catch (error) {
      if (error instanceof Error) {
        setError(error.message);
      }
    }
  };

  return (
    <section>
      <h2>Räume</h2>

      {error && <p>{error}</p>}
      {message && <p>{message}</p>}

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

      <h3>Raum auswählen</h3>

      <select
        value={selectedRoomId ?? ""}
        onChange={(event) => {
          const value = event.target.value;

          if (value === "") {
            onSelectRoom(null);
            return;
          }

          onSelectRoom(Number(value));
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

      <h3>Neuen Raum anlegen</h3>

      <form onSubmit={handleCreateRoom}>
        <input
          type="text"
          value={newRoomName}
          onChange={(event) =>
            setNewRoomName(event.target.value)
          }
          placeholder="Raumname"
          required
        />

        <button type="submit">
          Raum anlegen
        </button>
      </form>
    </section>
  );
}

export default RoomSection;