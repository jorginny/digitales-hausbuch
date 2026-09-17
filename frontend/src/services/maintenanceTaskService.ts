export type MaintenanceTaskRequest = {
  title: string;
  description?: string;
  dueDate?: string;
};

export type MaintenanceTaskResponse = {
  id: number;
  title: string;
  description?: string;
  dueDate?: string;
  completed: boolean;
  householdObjectId: number;
};

export async function createMaintenanceTask(
  propertyId: number,
  roomId: number,
  objectId: number,
  task: MaintenanceTaskRequest
): Promise<MaintenanceTaskResponse> {
  const response = await fetch(
    `http://localhost:8080/api/properties/${propertyId}/rooms/${roomId}/objects/${objectId}/maintenance-tasks`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      credentials: "include",
      body: JSON.stringify(task),
    }
  );

  if (!response.ok) {
    throw new Error(
      "Wartungsaufgabe konnte nicht angelegt werden."
    );
  }

  return response.json();
}

export async function getMaintenanceTasks(
  propertyId: number,
  roomId: number,
  objectId: number
): Promise<MaintenanceTaskResponse[]> {
  const response = await fetch(
    `http://localhost:8080/api/properties/${propertyId}/rooms/${roomId}/objects/${objectId}/maintenance-tasks`,
    {
      method: "GET",
      credentials: "include",
    }
  );

  if (!response.ok) {
    throw new Error(
      "Wartungsaufgaben konnten nicht geladen werden."
    );
  }

  return response.json();
}