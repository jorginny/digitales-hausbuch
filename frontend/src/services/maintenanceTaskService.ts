export type MaintenanceTaskRequest = {
  title: string;
  description?: string;
  dueDate?: string;
  recurrenceInterval?: number;
  recurrenceUnit?: RecurrenceUnit;
};

export type MaintenanceTaskResponse = {
  id: number;
  title: string;
  description?: string;
  dueDate?: string;
  completed: boolean;
  recurrenceInterval?: number;
  recurrenceUnit?: RecurrenceUnit;
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

export type RecurrenceUnit =
  | "MONTHS"
  | "YEARS";


export async function updateMaintenanceTask(
  propertyId: number,
  roomId: number,
  objectId: number,
  taskId: number,
  task: MaintenanceTaskRequest
): Promise<MaintenanceTaskResponse> {
  const response = await fetch(
    `http://localhost:8080/api/properties/${propertyId}/rooms/${roomId}/objects/${objectId}/maintenance-tasks/${taskId}`,
    {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      credentials: "include",
      body: JSON.stringify(task),
    }
  );

  if (!response.ok) {
    throw new Error(
      "Wartungsaufgabe konnte nicht aktualisiert werden."
    );
  }

  return response.json();
}

export async function completeMaintenanceTask(
  propertyId: number,
  roomId: number,
  objectId: number,
  taskId: number
): Promise<MaintenanceTaskResponse> {
  const response = await fetch(
    `http://localhost:8080/api/properties/${propertyId}/rooms/${roomId}/objects/${objectId}/maintenance-tasks/${taskId}/complete`,
    {
      method: "PUT",
      credentials: "include",
    }
  );

  if (!response.ok) {
    throw new Error(
      "Wartungsaufgabe konnte nicht abgeschlossen werden."
    );
  }

  return response.json();
}