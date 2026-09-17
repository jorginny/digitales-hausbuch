export type HouseholdObjectRequest = {
  name: string;
  description?: string;
  manufacturer?: string;
  model?: string;
  purchaseDate?: string;
};

export type HouseholdObjectResponse = {
  id: number;
  name: string;
  description?: string;
  manufacturer?: string;
  model?: string;
  purchaseDate?: string;
  roomId: number;
};

export async function createHouseholdObject(
  propertyId: number,
  roomId: number,
  householdObject: HouseholdObjectRequest
): Promise<HouseholdObjectResponse> {

  const response = await fetch(
    `http://localhost:8080/api/properties/${propertyId}/rooms/${roomId}/objects`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      credentials: "include",
      body: JSON.stringify(householdObject),
    }
  );

  if (!response.ok) {
    throw new Error(
      "Objekt konnte nicht angelegt werden."
    );
  }

  return response.json();
}