import { useEffect, useState } from "react";
import { useParams } from "react-router";

import {
  getMaintenanceOverview,
  type MaintenanceOverviewResponse,
} from "../services/maintenanceTaskService";

type DueStatus =
  | "Überfällig"
  | "Bald fällig"
  | "Später"
  | "Kein Termin";

function getDueStatus(
  dueDate?: string
): DueStatus {
  if (!dueDate) {
    return "Kein Termin";
  }

  const today = new Date();
  const due = new Date(dueDate);

  today.setHours(0, 0, 0, 0);
  due.setHours(0, 0, 0, 0);

  if (due < today) {
    return "Überfällig";
  }

  const thirtyDaysFromNow =
    new Date(today);

  thirtyDaysFromNow.setDate(
    thirtyDaysFromNow.getDate() + 30
  );

  if (due <= thirtyDaysFromNow) {
    return "Bald fällig";
  }

  return "Später";
}

function MaintenanceOverviewPage() {
  const { id } = useParams();

  const [tasks, setTasks] =
    useState<MaintenanceOverviewResponse[]>([]);

  const [error, setError] =
    useState("");

  const [loading, setLoading] =
    useState(true);

  useEffect(() => {
    if (!id) {
      setError(
        "Keine Immobilien-ID vorhanden."
      );

      setLoading(false);

      return;
    }

    const loadMaintenanceOverview =
      async () => {
        setError("");
        setLoading(true);

        try {
          const loadedTasks =
            await getMaintenanceOverview(
              Number(id)
            );

          setTasks(loadedTasks);
        } catch (error) {
          if (error instanceof Error) {
            setError(error.message);
          }
        } finally {
          setLoading(false);
        }
      };

    loadMaintenanceOverview();
  }, [id]);

  if (loading) {
    return (
      <p>
        Wartungsaufgaben werden geladen...
      </p>
    );
  }

  return (
    <div>
      <h1>Wartungsübersicht</h1>

      {error && <p>{error}</p>}

      {!error && tasks.length === 0 && (
        <p>
          Aktuell sind keine offenen
          Wartungsaufgaben vorhanden.
        </p>
      )}

      {tasks.length > 0 && (
        <ul>
          {tasks.map((task) => (
            <li key={task.taskId}>
              <h3>{task.title}</h3>

              <p>
                Status:{" "}
                {getDueStatus(
                  task.dueDate
                )}
              </p>

              <p>
                Raum: {task.roomName}
              </p>

              <p>
                Objekt: {task.objectName}
              </p>

              <p>
                Beschreibung:{" "}
                {task.description || "-"}
              </p>

              <p>
                Fällig am:{" "}
                {task.dueDate || "-"}
              </p>

              <p>
                Zuletzt erledigt:{" "}
                {task.completedAt || "-"}
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
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export default MaintenanceOverviewPage;