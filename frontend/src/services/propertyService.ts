export type PropertyRequest = {
  name: string;
  address?: string;
};

export type PropertyResponse = {
  id: number;
  name: string;
  address?: string;
  createdAt: string;
};

export async function createProperty(
  property: PropertyRequest
): Promise<PropertyResponse> {
  const response = await fetch(
    "http://localhost:8080/api/properties",
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      credentials: "include",
      body: JSON.stringify(property),
    }
  );

  if (!response.ok) {
    throw new Error("Immobilie konnte nicht angelegt werden.");
  }

  return response.json();
}

export async function getProperty(
  id: number
): Promise<PropertyResponse> {
  const response = await fetch(
    `http://localhost:8080/api/properties/${id}`,
    {
      method: "GET",
      credentials: "include",
    }
  );

  if (!response.ok) {
    throw new Error("Immobilie konnte nicht geladen werden.");
  }

  return response.json();
}

export async function updateProperty(
  id: number,
  property: PropertyRequest
): Promise<PropertyResponse> {
  const response = await fetch(
    `http://localhost:8080/api/properties/${id}`,
    {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      credentials: "include",
      body: JSON.stringify(property),
    }
  );

  if (!response.ok) {
    throw new Error("Immobilie konnte nicht aktualisiert werden.");
  }

  return response.json();
}

export async function getProperties(): Promise<PropertyResponse[]> {
  const response = await fetch(
    "http://localhost:8080/api/properties",
    {
      method: "GET",
      credentials: "include",
    }
  );

  if (!response.ok) {
    throw new Error("Immobilien konnten nicht geladen werden.");
  }

  return response.json();
}

export async function deleteProperty(
  propertyId: number
): Promise<void> {
  const response = await fetch(
    `http://localhost:8080/api/properties/${propertyId}`,
    {
      method: "DELETE",
      credentials: "include",
    }
  );

  if (!response.ok) {
    throw new Error(
      "Immobilie konnte nicht gelöscht werden."
    );
  }
}