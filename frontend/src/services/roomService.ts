export type RoomRequest = {
  name: string;
};

export type RoomResponse = {
  id: number;
  name: string;
  propertyId: number;
};

export async function getRooms(
  propertyId: number
): Promise<RoomResponse[]> {
  const response = await fetch(
    `http://localhost:8080/api/properties/${propertyId}/rooms`,
    {
      method: "GET",
      credentials: "include",
    }
  );

  if (!response.ok) {
    throw new Error("Räume konnten nicht geladen werden.");
  }

  return response.json();
}

export async function createRoom(
  propertyId: number,
  room: RoomRequest
): Promise<RoomResponse> {
  const response = await fetch(
    `http://localhost:8080/api/properties/${propertyId}/rooms`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      credentials: "include",
      body: JSON.stringify(room),
    }
  );

  if (!response.ok) {
    throw new Error("Raum konnte nicht angelegt werden.");
  }

  return response.json();
}

export async function updateRoom(
  propertyId: number,
  roomId: number,
  room: RoomRequest
): Promise<RoomResponse> {
  const response = await fetch(
    `http://localhost:8080/api/properties/${propertyId}/rooms/${roomId}`,
    {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      credentials: "include",
      body: JSON.stringify(room),
    }
  );

  if (!response.ok) {
    throw new Error("Raum konnte nicht aktualisiert werden.");
  }

  return response.json();
}

export async function deleteRoom(
  propertyId: number,
  roomId: number
): Promise<void> {
  const response = await fetch(
    `http://localhost:8080/api/properties/${propertyId}/rooms/${roomId}`,
    {
      method: "DELETE",
      credentials: "include",
    }
  );

  if (!response.ok) {
    throw new Error("Raum konnte nicht gelöscht werden.");
  }
}