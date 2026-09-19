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
  getHouseholdObjects,
  updateHouseholdObject,
  deleteHouseholdObject,
  type HouseholdObjectResponse,
} from "../services/householdObjectService";

import {
  createMaintenanceTask,
  getMaintenanceTasks,
  updateMaintenanceTask,
  completeMaintenanceTask,
  type MaintenanceTaskResponse,
  type RecurrenceUnit,
} from "../services/maintenanceTaskService";

function PropertyDetailPage() {
  const { id } = useParams();

  const [property, setProperty] =
    useState<PropertyResponse | null>(null);

  const [name, setName] = useState("");
  const [address, setAddress] = useState("");

  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  // Räume
  const [rooms, setRooms] =
    useState<RoomResponse[]>([]);

  const [newRoomName, setNewRoomName] =
    useState("");

  const [editingRoomId, setEditingRoomId] =
    useState<number | null>(null);

  const [editingRoomName, setEditingRoomName] =
    useState("");

  // Household Objects
  const [selectedRoomId, setSelectedRoomId] =
    useState<number | null>(null);

  const [householdObjects, setHouseholdObjects] =
    useState<HouseholdObjectResponse[]>([]);

  const [objectName, setObjectName] =
    useState("");

  const [objectDescription, setObjectDescription] =
    useState("");

  const [objectManufacturer, setObjectManufacturer] =
    useState("");

  const [objectModel, setObjectModel] =
    useState("");

  const [objectPurchaseDate, setObjectPurchaseDate] =
    useState("");

  // HouseholdObject bearbeiten
  const [editingObjectId, setEditingObjectId] =
    useState<number | null>(null);

  const [editingObjectName, setEditingObjectName] =
    useState("");

  const [
    editingObjectDescription,
    setEditingObjectDescription,
  ] = useState("");

  const [
    editingObjectManufacturer,
    setEditingObjectManufacturer,
  ] = useState("");

  const [
    editingObjectModel,
    setEditingObjectModel,
  ] = useState("");

  const [
    editingObjectPurchaseDate,
    setEditingObjectPurchaseDate,
  ] = useState("");

  // Wartungsaufgaben
  const [selectedObjectId, setSelectedObjectId] =
    useState<number | null>(null);

  const [maintenanceTasks, setMaintenanceTasks] =
    useState<MaintenanceTaskResponse[]>([]);

  const [taskTitle, setTaskTitle] =
    useState("");

  const [taskDescription, setTaskDescription] =
    useState("");

  const [taskDueDate, setTaskDueDate] =
    useState("");

  const [
    taskRecurrenceInterval,
    setTaskRecurrenceInterval,
  ] = useState("");

  const [
    taskRecurrenceUnit,
    setTaskRecurrenceUnit,
  ] = useState<"" | RecurrenceUnit>("");

  // Wartungsaufgabe bearbeiten
  const [editingTaskId, setEditingTaskId] =
    useState<number | null>(null);

  const [editingTaskTitle, setEditingTaskTitle] =
    useState("");

  const [
    editingTaskDescription,
    setEditingTaskDescription,
  ] = useState("");

  const [
    editingTaskDueDate,
    setEditingTaskDueDate,
  ] = useState("");

  const [
    editingTaskRecurrenceInterval,
    setEditingTaskRecurrenceInterval,
  ] = useState("");

  const [
    editingTaskRecurrenceUnit,
    setEditingTaskRecurrenceUnit,
  ] = useState<"" | RecurrenceUnit>("");

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

    setMessage("");
    setError("");

    try {
      const createdRoom =
        await createRoom(Number(id), {
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
    if (!id) {
      return;
    }

    setMessage("");
    setError("");

    try {
      const updatedRoom =
        await updateRoom(
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

    setMessage("");
    setError("");

    try {
      await deleteRoom(Number(id), roomId);

      setRooms((currentRooms) =>
        currentRooms.filter(
          (room) => room.id !== roomId
        )
      );

      if (selectedRoomId === roomId) {
        setSelectedRoomId(null);
        setHouseholdObjects([]);
        setSelectedObjectId(null);
        setMaintenanceTasks([]);
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

  const loadHouseholdObjects = async (
    roomId: number
  ) => {
    if (!id) {
      return;
    }

    setError("");

    try {
      const loadedObjects =
        await getHouseholdObjects(
          Number(id),
          roomId
        );

      setHouseholdObjects(loadedObjects);
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
      setError(
        "Bitte zuerst einen Raum auswählen."
      );
      return;
    }

    setMessage("");
    setError("");

    try {
      const createdObject =
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

      setHouseholdObjects((currentObjects) => [
        ...currentObjects,
        createdObject,
      ]);

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

  const handleEditHouseholdObject = (
    householdObject: HouseholdObjectResponse
  ) => {
    setEditingObjectId(householdObject.id);
    setEditingObjectName(householdObject.name);
    setEditingObjectDescription(
      householdObject.description ?? ""
    );
    setEditingObjectManufacturer(
      householdObject.manufacturer ?? ""
    );
    setEditingObjectModel(
      householdObject.model ?? ""
    );
    setEditingObjectPurchaseDate(
      householdObject.purchaseDate ?? ""
    );
  };

  const handleUpdateHouseholdObject = async (
    objectId: number
  ) => {
    if (!id || selectedRoomId === null) {
      return;
    }

    setMessage("");
    setError("");

    try {
      const updatedObject =
        await updateHouseholdObject(
          Number(id),
          selectedRoomId,
          objectId,
          {
            name: editingObjectName,
            description:
              editingObjectDescription,
            manufacturer:
              editingObjectManufacturer,
            model: editingObjectModel,
            purchaseDate:
              editingObjectPurchaseDate ||
              undefined,
          }
        );

      setHouseholdObjects((currentObjects) =>
        currentObjects.map(
          (householdObject) =>
            householdObject.id === objectId
              ? updatedObject
              : householdObject
        )
      );

      setEditingObjectId(null);

      setMessage(
        "Objekt wurde erfolgreich aktualisiert."
      );
    } catch (error) {
      if (error instanceof Error) {
        setError(error.message);
      }
    }
  };

  const handleDeleteHouseholdObject = async (
    objectId: number
  ) => {
    if (!id || selectedRoomId === null) {
      return;
    }

    const confirmed = window.confirm(
      "Möchtest du dieses Objekt wirklich löschen?"
    );

    if (!confirmed) {
      return;
    }

    setMessage("");
    setError("");

    try {
      await deleteHouseholdObject(
        Number(id),
        selectedRoomId,
        objectId
      );

      setHouseholdObjects((currentObjects) =>
        currentObjects.filter(
          (householdObject) =>
            householdObject.id !== objectId
        )
      );

      if (selectedObjectId === objectId) {
        setSelectedObjectId(null);
        setMaintenanceTasks([]);
      }

      setMessage(
        "Objekt wurde erfolgreich gelöscht."
      );
    } catch (error) {
      if (error instanceof Error) {
        setError(error.message);
      }
    }
  };

  const loadMaintenanceTasks = async (
    objectId: number
  ) => {
    if (!id || selectedRoomId === null) {
      return;
    }

    setError("");

    try {
      const loadedTasks =
        await getMaintenanceTasks(
          Number(id),
          selectedRoomId,
          objectId
        );

      setMaintenanceTasks(loadedTasks);
    } catch (error) {
      if (error instanceof Error) {
        setError(error.message);
      }
    }
  };

  const handleCreateMaintenanceTask = async (
    event: React.SubmitEvent<HTMLFormElement>
  ) => {
    event.preventDefault();

    if (
      !id ||
      selectedRoomId === null ||
      selectedObjectId === null
    ) {
      setError(
        "Bitte zuerst einen Raum und ein Objekt auswählen."
      );
      return;
    }

    setError("");
    setMessage("");

    const recurrenceInterval =
      taskRecurrenceInterval === ""
        ? undefined
        : Number(taskRecurrenceInterval);

    const recurrenceUnit =
      taskRecurrenceUnit === ""
        ? undefined
        : taskRecurrenceUnit;

    try {
      const createdTask =
        await createMaintenanceTask(
          Number(id),
          selectedRoomId,
          selectedObjectId,
          {
            title: taskTitle,
            description: taskDescription,
            dueDate:
              taskDueDate || undefined,
            recurrenceInterval,
            recurrenceUnit,
          }
        );

      setMaintenanceTasks((currentTasks) => [
        ...currentTasks,
        createdTask,
      ]);

      setTaskTitle("");
      setTaskDescription("");
      setTaskDueDate("");
      setTaskRecurrenceInterval("");
      setTaskRecurrenceUnit("");

      setMessage(
        "Wartungsaufgabe wurde erfolgreich angelegt."
      );
    } catch (error) {
      if (error instanceof Error) {
        setError(error.message);
      }
    }
  };

  const handleEditMaintenanceTask = (
    task: MaintenanceTaskResponse
  ) => {
    setEditingTaskId(task.id);
    setEditingTaskTitle(task.title);
    setEditingTaskDescription(
      task.description ?? ""
    );
    setEditingTaskDueDate(
      task.dueDate ?? ""
    );

    setEditingTaskRecurrenceInterval(
      task.recurrenceInterval?.toString() ?? ""
    );

    setEditingTaskRecurrenceUnit(
      task.recurrenceUnit ?? ""
    );
  };

  const handleUpdateMaintenanceTask = async (
    taskId: number
  ) => {
    if (
      !id ||
      selectedRoomId === null ||
      selectedObjectId === null
    ) {
      return;
    }

    setError("");
    setMessage("");

    const recurrenceInterval =
      editingTaskRecurrenceInterval === ""
        ? undefined
        : Number(
            editingTaskRecurrenceInterval
          );

    const recurrenceUnit =
      editingTaskRecurrenceUnit === ""
        ? undefined
        : editingTaskRecurrenceUnit;

    try {
      const updatedTask =
        await updateMaintenanceTask(
          Number(id),
          selectedRoomId,
          selectedObjectId,
          taskId,
          {
            title: editingTaskTitle,
            description:
              editingTaskDescription,
            dueDate:
              editingTaskDueDate || undefined,
            recurrenceInterval,
            recurrenceUnit,
          }
        );

      setMaintenanceTasks((currentTasks) =>
        currentTasks.map((task) =>
          task.id === taskId
            ? updatedTask
            : task
        )
      );

      setEditingTaskId(null);

      setMessage(
        "Wartungsaufgabe wurde erfolgreich aktualisiert."
      );
    } catch (error) {
      if (error instanceof Error) {
        setError(error.message);
      }
    }
  };

  const handleCompleteMaintenanceTask = async (
    taskId: number
  ) => {
    if (
      !id ||
      selectedRoomId === null ||
      selectedObjectId === null
    ) {
      return;
    }

    setError("");
    setMessage("");

    try {
      const updatedTask =
        await completeMaintenanceTask(
          Number(id),
          selectedRoomId,
          selectedObjectId,
          taskId
        );

      setMaintenanceTasks((currentTasks) =>
        currentTasks.map((task) =>
          task.id === taskId
            ? updatedTask
            : task
        )
      );

      setMessage(
        "Wartungsaufgabe wurde abgeschlossen."
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
      {message && <p>{message}</p>}

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

      <hr />

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
                    onClick={() =>
                      setEditingRoomId(null)
                    }
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

      <h3>Neuen Raum anlegen</h3>

      <form onSubmit={handleCreateRoom}>
        <input
          type="text"
          value={newRoomName}
          onChange={(event) =>
            setNewRoomName(event.target.value)
          }
          required
        />

        <button type="submit">
          Raum anlegen
        </button>
      </form>

      <hr />

      <h2>Objekte</h2>

      <select
        value={selectedRoomId ?? ""}
        onChange={(event) => {
          const value = event.target.value;

          if (value === "") {
            setSelectedRoomId(null);
            setHouseholdObjects([]);
            setSelectedObjectId(null);
            setMaintenanceTasks([]);
            return;
          }

          const roomId = Number(value);

          setSelectedRoomId(roomId);
          setHouseholdObjects([]);
          setSelectedObjectId(null);
          setMaintenanceTasks([]);

          loadHouseholdObjects(roomId);
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

      {selectedRoomId !== null && (
        <>
          <h3>Objekte im Raum</h3>

          {householdObjects.map(
            (householdObject) => (
              <div key={householdObject.id}>
                {editingObjectId ===
                householdObject.id ? (
                  <>
                    <input
                      value={editingObjectName}
                      onChange={(event) =>
                        setEditingObjectName(
                          event.target.value
                        )
                      }
                    />

                    <input
                      value={
                        editingObjectDescription
                      }
                      onChange={(event) =>
                        setEditingObjectDescription(
                          event.target.value
                        )
                      }
                    />

                    <input
                      value={
                        editingObjectManufacturer
                      }
                      onChange={(event) =>
                        setEditingObjectManufacturer(
                          event.target.value
                        )
                      }
                    />

                    <input
                      value={editingObjectModel}
                      onChange={(event) =>
                        setEditingObjectModel(
                          event.target.value
                        )
                      }
                    />

                    <input
                      type="date"
                      value={
                        editingObjectPurchaseDate
                      }
                      onChange={(event) =>
                        setEditingObjectPurchaseDate(
                          event.target.value
                        )
                      }
                    />

                    <button
                      type="button"
                      onClick={() =>
                        handleUpdateHouseholdObject(
                          householdObject.id
                        )
                      }
                    >
                      Speichern
                    </button>

                    <button
                      type="button"
                      onClick={() =>
                        setEditingObjectId(null)
                      }
                    >
                      Abbrechen
                    </button>
                  </>
                ) : (
                  <>
                    <p>
                      <strong>
                        {householdObject.name}
                      </strong>
                    </p>

                    <p>
                      Hersteller:{" "}
                      {householdObject.manufacturer ||
                        "-"}
                    </p>

                    <p>
                      Modell:{" "}
                      {householdObject.model ||
                        "-"}
                    </p>

                    <p>
                      Kaufdatum:{" "}
                      {householdObject.purchaseDate ||
                        "-"}
                    </p>

                    <p>
                      Beschreibung:{" "}
                      {householdObject.description ||
                        "-"}
                    </p>

                    <button
                      type="button"
                      onClick={() =>
                        handleEditHouseholdObject(
                          householdObject
                        )
                      }
                    >
                      Bearbeiten
                    </button>

                    <button
                      type="button"
                      onClick={() =>
                        handleDeleteHouseholdObject(
                          householdObject.id
                        )
                      }
                    >
                      Löschen
                    </button>
                  </>
                )}
              </div>
            )
          )}

          <h3>Objekt anlegen</h3>

          <form
            onSubmit={
              handleCreateHouseholdObject
            }
          >
            <input
              placeholder="Name"
              value={objectName}
              onChange={(event) =>
                setObjectName(
                  event.target.value
                )
              }
              required
            />

            <input
              placeholder="Beschreibung"
              value={objectDescription}
              onChange={(event) =>
                setObjectDescription(
                  event.target.value
                )
              }
            />

            <input
              placeholder="Hersteller"
              value={objectManufacturer}
              onChange={(event) =>
                setObjectManufacturer(
                  event.target.value
                )
              }
            />

            <input
              placeholder="Modell"
              value={objectModel}
              onChange={(event) =>
                setObjectModel(
                  event.target.value
                )
              }
            />

            <input
              type="date"
              value={objectPurchaseDate}
              onChange={(event) =>
                setObjectPurchaseDate(
                  event.target.value
                )
              }
            />

            <button type="submit">
              Objekt anlegen
            </button>
          </form>
        </>
      )}

      <hr />

      <h2>Wartungsaufgaben</h2>

      {selectedRoomId !== null && (
        <select
          value={selectedObjectId ?? ""}
          onChange={(event) => {
            const value = event.target.value;

            if (value === "") {
              setSelectedObjectId(null);
              setMaintenanceTasks([]);
              return;
            }

            const objectId =
              Number(value);

            setSelectedObjectId(objectId);
            setMaintenanceTasks([]);
            setEditingTaskId(null);

            loadMaintenanceTasks(objectId);
          }}
        >
          <option value="">
            Objekt auswählen
          </option>

          {householdObjects.map(
            (householdObject) => (
              <option
                key={householdObject.id}
                value={householdObject.id}
              >
                {householdObject.name}
              </option>
            )
          )}
        </select>
      )}

      {selectedObjectId !== null && (
        <>
          <h3>Wartungsaufgaben</h3>

          {maintenanceTasks.length === 0 ? (
            <p>
              Noch keine Wartungsaufgaben
              vorhanden.
            </p>
          ) : (
            <ul>
              {maintenanceTasks.map(
                (task) => (
                  <li key={task.id}>
                    {editingTaskId ===
                    task.id ? (
                      <>
                        <div>
                          <label>
                            Titel
                          </label>

                          <input
                            type="text"
                            value={
                              editingTaskTitle
                            }
                            onChange={(event) =>
                              setEditingTaskTitle(
                                event.target.value
                              )
                            }
                          />
                        </div>

                        <div>
                          <label>
                            Beschreibung
                          </label>

                          <input
                            type="text"
                            value={
                              editingTaskDescription
                            }
                            onChange={(event) =>
                              setEditingTaskDescription(
                                event.target.value
                              )
                            }
                          />
                        </div>

                        <div>
                          <label>
                            Fälligkeitsdatum
                          </label>

                          <input
                            type="date"
                            value={
                              editingTaskDueDate
                            }
                            onChange={(event) =>
                              setEditingTaskDueDate(
                                event.target.value
                              )
                            }
                          />
                        </div>

                        <div>
                          <label>
                            Wiederholungsintervall
                          </label>

                          <input
                            type="number"
                            min="1"
                            value={
                              editingTaskRecurrenceInterval
                            }
                            onChange={(event) =>
                              setEditingTaskRecurrenceInterval(
                                event.target.value
                              )
                            }
                          />
                        </div>

                        <div>
                          <label>
                            Einheit
                          </label>

                          <select
                            value={
                              editingTaskRecurrenceUnit
                            }
                            onChange={(event) =>
                              setEditingTaskRecurrenceUnit(
                                event.target
                                  .value as
                                  | ""
                                  | RecurrenceUnit
                              )
                            }
                          >
                            <option value="">
                              Keine Wiederholung
                            </option>

                            <option value="MONTHS">
                              Monate
                            </option>

                            <option value="YEARS">
                              Jahre
                            </option>
                          </select>
                        </div>

                        <button
                          type="button"
                          onClick={() =>
                            handleUpdateMaintenanceTask(
                              task.id
                            )
                          }
                        >
                          Speichern
                        </button>

                        <button
                          type="button"
                          onClick={() =>
                            setEditingTaskId(
                              null
                            )
                          }
                        >
                          Abbrechen
                        </button>
                      </>
                    ) : (
                      <>
                        <p>
                          <strong>
                            {task.title}
                          </strong>
                        </p>

                        <p>
                          Beschreibung:{" "}
                          {task.description ||
                            "-"}
                        </p>

                        <p>
                          Fällig am:{" "}
                          {task.dueDate ||
                            "-"}
                        </p>

                        <p>
                          Status:{" "}
                          {task.completed
                            ? "Erledigt"
                            : "Offen"}
                        </p>

                        <p>
                          Zuletzt erledigt:{" "}
                          {task.completedAt ||
                            "-"}
                        </p>

                        <p>
                          Wiederholung:{" "}
                          {task.recurrenceInterval &&
                          task.recurrenceUnit
                            ? `${task.recurrenceInterval} ${
                                task.recurrenceUnit ===
                                "MONTHS"
                                  ? "Monate"
                                  : "Jahre"
                              }`
                            : "Keine"}
                        </p>

                        <button
                          type="button"
                          onClick={() =>
                            handleEditMaintenanceTask(
                              task
                            )
                          }
                        >
                          Bearbeiten
                        </button>

                        {!task.completed && (
                          <button
                            type="button"
                            onClick={() =>
                              handleCompleteMaintenanceTask(
                                task.id
                              )
                            }
                          >
                            Erledigt
                          </button>
                        )}
                      </>
                    )}
                  </li>
                )
              )}
            </ul>
          )}

          <h3>
            Wartungsaufgabe anlegen
          </h3>

          <form
            onSubmit={
              handleCreateMaintenanceTask
            }
          >
            <div>
              <label htmlFor="taskTitle">
                Titel
              </label>

              <input
                id="taskTitle"
                type="text"
                value={taskTitle}
                onChange={(event) =>
                  setTaskTitle(
                    event.target.value
                  )
                }
                required
              />
            </div>

            <div>
              <label htmlFor="taskDescription">
                Beschreibung
              </label>

              <input
                id="taskDescription"
                type="text"
                value={taskDescription}
                onChange={(event) =>
                  setTaskDescription(
                    event.target.value
                  )
                }
              />
            </div>

            <div>
              <label htmlFor="taskDueDate">
                Fälligkeitsdatum
              </label>

              <input
                id="taskDueDate"
                type="date"
                value={taskDueDate}
                onChange={(event) =>
                  setTaskDueDate(
                    event.target.value
                  )
                }
              />
            </div>

            <div>
              <label htmlFor="taskRecurrenceInterval">
                Wiederholungsintervall
              </label>

              <input
                id="taskRecurrenceInterval"
                type="number"
                min="1"
                value={
                  taskRecurrenceInterval
                }
                onChange={(event) =>
                  setTaskRecurrenceInterval(
                    event.target.value
                  )
                }
              />
            </div>

            <div>
              <label htmlFor="taskRecurrenceUnit">
                Einheit
              </label>

              <select
                id="taskRecurrenceUnit"
                value={
                  taskRecurrenceUnit
                }
                onChange={(event) =>
                  setTaskRecurrenceUnit(
                    event.target.value as
                      | ""
                      | RecurrenceUnit
                  )
                }
              >
                <option value="">
                  Keine Wiederholung
                </option>

                <option value="MONTHS">
                  Monate
                </option>

                <option value="YEARS">
                  Jahre
                </option>
              </select>
            </div>

            <button type="submit">
              Wartungsaufgabe anlegen
            </button>
          </form>
        </>
      )}
    </div>
  );
}

export default PropertyDetailPage;