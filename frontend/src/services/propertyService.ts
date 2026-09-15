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