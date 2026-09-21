import { useEffect, useState } from "react";

import {
  completeMaintenanceTask,
  createMaintenanceTask,
  getMaintenanceHistory,
  getMaintenanceTasks,
  updateMaintenanceTask,
  type MaintenanceRecordResponse,
  type MaintenanceTaskResponse,
  type RecurrenceUnit,
} from "../services/maintenanceTaskService";

type MaintenanceTaskSectionProps = {
  propertyId: number;
  selectedRoomId: number | null;
  selectedObjectId: number | null;
};

function MaintenanceTaskSection({
  propertyId,
  selectedRoomId,
  selectedObjectId,
}: MaintenanceTaskSectionProps) {
  const [maintenanceTasks, setMaintenanceTasks] =
    useState<MaintenanceTaskResponse[]>([]);

  // Neue Aufgabe
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

  // Aufgabe bearbeiten
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

  // Abschluss / Historie
  const [completionNotes, setCompletionNotes] =
    useState<Record<number, string>>({});

  const [historyTaskId, setHistoryTaskId] =
    useState<number | null>(null);

  const [history, setHistory] =
    useState<MaintenanceRecordResponse[]>([]);

  const [historyLoading, setHistoryLoading] =
    useState(false);

  const [message, setMessage] =
    useState("");

  const [error, setError] =
    useState("");

  useEffect(() => {
    if (
      selectedRoomId === null ||
      selectedObjectId === null
    ) {
      setMaintenanceTasks([]);
      setHistory([]);
      setHistoryTaskId(null);
      return;
    }

    const loadMaintenanceTasks =
      async () => {
        setError("");

        try {
          const loadedTasks =
            await getMaintenanceTasks(
              propertyId,
              selectedRoomId,
              selectedObjectId
            );

          setMaintenanceTasks(
            loadedTasks
          );
        } catch (error) {
          if (error instanceof Error) {
            setError(error.message);
          }
        }
      };

    loadMaintenanceTasks();
  }, [
    propertyId,
    selectedRoomId,
    selectedObjectId,
  ]);

  const handleCreateMaintenanceTask =
    async (
      event: React.SubmitEvent<HTMLFormElement>
    ) => {
      event.preventDefault();

      if (
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
            propertyId,
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

        setMaintenanceTasks(
          (currentTasks) => [
            ...currentTasks,
            createdTask,
          ]
        );

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

    setEditingTaskTitle(
      task.title
    );

    setEditingTaskDescription(
      task.description ?? ""
    );

    setEditingTaskDueDate(
      task.dueDate ?? ""
    );

    setEditingTaskRecurrenceInterval(
      task.recurrenceInterval?.toString() ??
        ""
    );

    setEditingTaskRecurrenceUnit(
      task.recurrenceUnit ?? ""
    );
  };

  const handleUpdateMaintenanceTask =
    async (taskId: number) => {
      if (
        selectedRoomId === null ||
        selectedObjectId === null
      ) {
        return;
      }

      setError("");
      setMessage("");

      const recurrenceInterval =
        editingTaskRecurrenceInterval ===
        ""
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
            propertyId,
            selectedRoomId,
            selectedObjectId,
            taskId,
            {
              title:
                editingTaskTitle,

              description:
                editingTaskDescription,

              dueDate:
                editingTaskDueDate ||
                undefined,

              recurrenceInterval,

              recurrenceUnit,
            }
          );

        setMaintenanceTasks(
          (currentTasks) =>
            currentTasks.map(
              (task) =>
                task.id === taskId
                  ? updatedTask
                  : task
            )
        );

        setEditingTaskId(null);

        setEditingTaskTitle("");
        setEditingTaskDescription("");
        setEditingTaskDueDate("");
        setEditingTaskRecurrenceInterval("");
        setEditingTaskRecurrenceUnit("");

        setMessage(
          "Wartungsaufgabe wurde erfolgreich aktualisiert."
        );
      } catch (error) {
        if (error instanceof Error) {
          setError(error.message);
        }
      }
    };

  const handleCompleteMaintenanceTask =
    async (taskId: number) => {
      if (
        selectedRoomId === null ||
        selectedObjectId === null
      ) {
        return;
      }

      setError("");
      setMessage("");

      try {
        const note =
          completionNotes[taskId] ?? "";

        const updatedTask =
          await completeMaintenanceTask(
            propertyId,
            selectedRoomId,
            selectedObjectId,
            taskId,
            note
          );

        setMaintenanceTasks(
          (currentTasks) =>
            currentTasks.map(
              (task) =>
                task.id === taskId
                  ? updatedTask
                  : task
            )
        );

        setCompletionNotes(
          (currentNotes) => ({
            ...currentNotes,
            [taskId]: "",
          })
        );

        setMessage(
          "Wartungsaufgabe wurde abgeschlossen und in der Historie gespeichert."
        );

        if (historyTaskId === taskId) {
          await refreshHistory(
            taskId
          );
        }
      } catch (error) {
        if (error instanceof Error) {
          setError(error.message);
        }
      }
    };

  const handleLoadHistory =
    async (taskId: number) => {
      if (
        selectedRoomId === null ||
        selectedObjectId === null
      ) {
        return;
      }

      // Erneuter Klick schließt die Historie
      if (historyTaskId === taskId) {
        setHistoryTaskId(null);
        setHistory([]);
        return;
      }

      setError("");
      setHistoryLoading(true);

      try {
        const loadedHistory =
          await getMaintenanceHistory(
            propertyId,
            selectedRoomId,
            selectedObjectId,
            taskId
          );

        setHistory(
          loadedHistory
        );

        setHistoryTaskId(
          taskId
        );
      } catch (error) {
        if (error instanceof Error) {
          setError(error.message);
        }
      } finally {
        setHistoryLoading(false);
      }
    };

    const refreshHistory = async (
        taskId: number
        ) => {
        if (
            selectedRoomId === null ||
            selectedObjectId === null
        ) {
            return;
        }

        try {
            const loadedHistory =
            await getMaintenanceHistory(
                propertyId,
                selectedRoomId,
                selectedObjectId,
                taskId
            );

            setHistory(loadedHistory);
        } catch (error) {
            if (error instanceof Error) {
            setError(error.message);
            }
        }
    };

  if (selectedRoomId === null) {
    return (
      <section>
        <h2>Wartungsaufgaben</h2>

        <p>
          Bitte zuerst einen Raum
          auswählen.
        </p>
      </section>
    );
  }

  if (selectedObjectId === null) {
    return (
      <section>
        <h2>Wartungsaufgaben</h2>

        <p>
          Bitte zuerst ein Objekt
          auswählen.
        </p>
      </section>
    );
  }

  return (
    <section>
      <h2>Wartungsaufgaben</h2>

      {error && <p>{error}</p>}
      {message && <p>{message}</p>}

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
                        onChange={(
                          event
                        ) =>
                          setEditingTaskTitle(
                            event.target
                              .value
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
                        onChange={(
                          event
                        ) =>
                          setEditingTaskDescription(
                            event.target
                              .value
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
                        onChange={(
                          event
                        ) =>
                          setEditingTaskDueDate(
                            event.target
                              .value
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
                        onChange={(
                          event
                        ) =>
                          setEditingTaskRecurrenceInterval(
                            event.target
                              .value
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
                        onChange={(
                          event
                        ) =>
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
                      {task.dueDate || "-"}
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
                        ? `${
                            task.recurrenceInterval
                          } ${
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
                      <>
                        <div>
                          <label
                            htmlFor={`completionNote-${task.id}`}
                          >
                            Notiz zur Durchführung
                          </label>

                          <input
                            id={`completionNote-${task.id}`}
                            type="text"
                            value={
                              completionNotes[
                                task.id
                              ] ?? ""
                            }
                            onChange={(
                              event
                            ) =>
                              setCompletionNotes(
                                (
                                  currentNotes
                                ) => ({
                                  ...currentNotes,
                                  [task.id]:
                                    event
                                      .target
                                      .value,
                                })
                              )
                            }
                            placeholder="Optional"
                          />
                        </div>

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
                      </>
                    )}

                    <button
                      type="button"
                      onClick={() =>
                        handleLoadHistory(
                          task.id
                        )
                      }
                    >
                      {historyTaskId ===
                      task.id
                        ? "Historie schließen"
                        : "Historie anzeigen"}
                    </button>

                    {historyTaskId ===
                      task.id && (
                      <div>
                        <h4>
                          Historie
                        </h4>

                        {historyLoading ? (
                          <p>
                            Historie wird
                            geladen...
                          </p>
                        ) : history.length ===
                          0 ? (
                          <p>
                            Noch keine
                            durchgeführten
                            Arbeiten vorhanden.
                          </p>
                        ) : (
                          <ul>
                            {history.map(
                              (record) => (
                                <li
                                  key={
                                    record.id
                                  }
                                >
                                  <p>
                                    Durchgeführt
                                    am:{" "}
                                    {
                                      record.completedAt
                                    }
                                  </p>

                                  <p>
                                    Notiz:{" "}
                                    {record.note ||
                                      "-"}
                                  </p>
                                </li>
                              )
                            )}
                          </ul>
                        )}
                      </div>
                    )}
                  </>
                )}
              </li>
            )
          )}
        </ul>
      )}

      <h3>
        Neue Wartungsaufgabe
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
            value={taskRecurrenceUnit}
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
    </section>
  );
}

export default MaintenanceTaskSection;