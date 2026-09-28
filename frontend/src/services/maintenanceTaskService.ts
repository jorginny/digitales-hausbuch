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
  completedAt?: string;
  recurrenceInterval?: number;
  recurrenceUnit?: RecurrenceUnit;
  householdObjectId: number;
};

export type MaintenanceRecordResponse = { 
    id: number; 
    completedAt: string; 
    note?: string; 
    maintenanceTaskId: number; 
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
    taskId: number, 
    note?: string 
): Promise<MaintenanceTaskResponse> { 
    const response = await fetch( 
        `http://localhost:8080/api/properties/${propertyId}/rooms/${roomId}/objects/${objectId}/maintenance-tasks/${taskId}/complete`, 
        { 
            method: "PUT", 
            headers: { "Content-Type": "application/json", }, 
            credentials: "include", 
            body: JSON.stringify({ note: note || null, }), 
        } ); 
        
        if (!response.ok) { 
            throw new Error( "Wartungsaufgabe konnte nicht abgeschlossen werden." ); 
        } 
        
        return response.json(); 
    }

export type MaintenanceOverviewResponse = {
  taskId: number;
  title: string;
  description?: string;
  dueDate?: string;
  completedAt?: string;
  recurrenceInterval?: number;
  recurrenceUnit?: RecurrenceUnit;
  objectId: number;
  objectName: string;
  roomId: number;
  roomName: string;
};

export async function getMaintenanceOverview(
  propertyId: number
): Promise<MaintenanceOverviewResponse[]> {
  const response = await fetch(
    `http://localhost:8080/api/properties/${propertyId}/maintenance-tasks`,
    {
      method: "GET",
      credentials: "include",
    }
  );

  if (!response.ok) {
    throw new Error(
      "Wartungsübersicht konnte nicht geladen werden."
    );
  }

  return response.json();
}

export async function getMaintenanceHistory( 
    propertyId: number, 
    roomId: number, 
    objectId: number, 
    taskId: number 
): Promise<MaintenanceRecordResponse[]> { 
    const response = await fetch( 
        `http://localhost:8080/api/properties/${propertyId}/rooms/${roomId}/objects/${objectId}/maintenance-tasks/${taskId}/history`, { 
            method: "GET", 
            credentials: "include", } 
        ); 
        
        if (!response.ok) { 
            throw new Error( "Wartungshistorie konnte nicht geladen werden." ); 
        } 
        
        return response.json(); 
    }

export async function deleteMaintenanceTask(
  propertyId: number,
  roomId: number,
  objectId: number,
  taskId: number
): Promise<void> {
  const response = await fetch(
    `http://localhost:8080/api/properties/${propertyId}/rooms/${roomId}/objects/${objectId}/maintenance-tasks/${taskId}`,
    {
      method: "DELETE",
      credentials: "include",
    }
  );

  if (!response.ok) {
    throw new Error(
      "Wartungsaufgabe konnte nicht gelöscht werden."
    );
  }
}
