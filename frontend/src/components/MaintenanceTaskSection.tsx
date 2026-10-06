import { useEffect, useState } from "react";

import {
  Alert,
  Box,
  Button,
  Card,
  CardActionArea,
  CardActions,
  CardContent,
  Chip,
  Collapse,
  Divider,
  MenuItem,
  Paper,
  Stack,
  TextField,
  Typography,
} from "@mui/material";

import AddIcon from "@mui/icons-material/Add";
import BuildIcon from "@mui/icons-material/Build";
import CheckCircleIcon from "@mui/icons-material/CheckCircle";
import EditIcon from "@mui/icons-material/Edit";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import HistoryIcon from "@mui/icons-material/History";
import DeleteIcon from "@mui/icons-material/Delete";

import {
  completeMaintenanceTask,
  createMaintenanceTask,
  getMaintenanceHistory,
  getMaintenanceTasks,
  updateMaintenanceTask,
  deleteMaintenanceTask,
  type MaintenanceRecordResponse,
  type MaintenanceTaskResponse,
  type RecurrenceUnit,
} from "../services/maintenanceTaskService";

type MaintenanceTaskSectionProps = {
  propertyId: number;
  selectedRoomId: number | null;
  selectedObjectId: number | null;
};

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

function getDueStatusColor(
  status: DueStatus
):
  | "error"
  | "warning"
  | "success"
  | "default" {
  switch (status) {
    case "Überfällig":
      return "error";

    case "Bald fällig":
      return "warning";

    case "Später":
      return "success";

    case "Kein Termin":
      return "default";
  }
}

/**
 * Manages the maintenance tasks of the currently selected household object.
 *
 * Supports creating, editing, completing and deleting tasks, displaying due
 * status and recurrence information, and loading the corresponding maintenance
 * history.
 */
function MaintenanceTaskSection({
  propertyId,
  selectedRoomId,
  selectedObjectId,
}: MaintenanceTaskSectionProps) {
  const [maintenanceTasks, setMaintenanceTasks] =
    useState<MaintenanceTaskResponse[]>([]);

  const [showCreateForm, setShowCreateForm] =
    useState(false);

  const [expandedTaskId, setExpandedTaskId] =
    useState<number | null>(null);

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

  const [editingTaskId, setEditingTaskId] =
    useState<number | null>(null);

  const [
    editingTaskTitle,
    setEditingTaskTitle,
  ] = useState("");

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

  const [
    completionNotes,
    setCompletionNotes,
  ] = useState<Record<number, string>>({});

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
    setMessage("");
    setError("");

    if (
      selectedRoomId === null ||
      selectedObjectId === null
    ) {
      setMaintenanceTasks([]);
      setShowCreateForm(false);
      setExpandedTaskId(null);
      setEditingTaskId(null);
      setHistoryTaskId(null);
      setHistory([]);

      return;
    }

    const loadMaintenanceTasks =
      async () => {
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
        return;
      }

      setError("");
      setMessage("");

      const recurrenceInterval =
        taskRecurrenceInterval === ""
          ? undefined
          : Number(
              taskRecurrenceInterval
            );

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
              description:
                taskDescription,
              dueDate:
                taskDueDate ||
                undefined,
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

        setShowCreateForm(false);

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

    setExpandedTaskId(task.id);
    setMessage("");
    setError("");
  };

  const handleCancelEdit = () => {
    setEditingTaskId(null);
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

        setMessage(
          "Wartungsaufgabe wurde erfolgreich aktualisiert."
        );
      } catch (error) {
        if (error instanceof Error) {
          setError(error.message);
        }
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

        if (
          historyTaskId === taskId
        ) {
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

const handleDeleteMaintenanceTask = async (
  taskId: number) => {
    if (
        selectedRoomId === null ||
        selectedObjectId === null
        ) {
            return;
        }

        const confirmed = window.confirm(
            "Möchtest du diese Wartungsaufgabe wirklich löschen?"
        );

        if (!confirmed) {
            return;
        }

    try {
        await deleteMaintenanceTask(
        propertyId,
        selectedRoomId,
        selectedObjectId,
        taskId
        );

    setMaintenanceTasks((currentTasks) =>
      currentTasks.filter(
        (task) => task.id !== taskId
      )
    );

    setExpandedTaskId(null);
    setHistoryTaskId(null);
    setHistory([]);

    setMessage(
      "Wartungsaufgabe wurde erfolgreich gelöscht."
    );
  } catch (error) {
    if (error instanceof Error) {
      setError(error.message);
    }
  }
};

  return (
    <Paper elevation={2} sx={{ p: 3 }}>
      <Stack spacing={3}>
        <Stack
          direction={{
            xs: "column",
            sm: "row",
          }}
          spacing={2}
            sx={{
                justifyContent: "space-between",
                alignItems: {
                xs: "flex-start",
                sm: "center",
                },
            }}
            >
          <div>
            <Typography
              variant="h5"
              component="h2"
              gutterBottom
            >
              Wartungsaufgaben
            </Typography>

            <Typography
              variant="body2"
              color="text.secondary"
            >
              Verwalte Wartungen und
              durchgeführte Arbeiten des
              ausgewählten Objekts.
            </Typography>
          </div>

          {selectedRoomId !== null &&
            selectedObjectId !== null &&
            !showCreateForm && (
              <Button
                variant="contained"
                startIcon={<AddIcon />}
                onClick={() => {
                  setShowCreateForm(true);
                  setMessage("");
                  setError("");
                }}
              >
                Neue Wartungsaufgabe
              </Button>
            )}
        </Stack>

        {error && (
          <Alert severity="error">
            {error}
          </Alert>
        )}

        {message && (
          <Alert severity="success">
            {message}
          </Alert>
        )}

        {selectedRoomId === null ? (
          <Alert severity="info">
            Bitte zuerst einen Raum auswählen.
          </Alert>
        ) : selectedObjectId === null ? (
          <Alert severity="info">
            Bitte zuerst ein Objekt auswählen.
          </Alert>
        ) : (
          <>
            {showCreateForm && (
              <Box
                component="form"
                onSubmit={
                  handleCreateMaintenanceTask
                }
              >
                <Stack spacing={2}>
                  <Typography
                    variant="h6"
                    component="h3"
                  >
                    Neue Wartungsaufgabe
                  </Typography>

                  <TextField
                    label="Titel"
                    value={taskTitle}
                    onChange={(event) =>
                      setTaskTitle(
                        event.target.value
                      )
                    }
                    required
                    fullWidth
                  />

                  <TextField
                    label="Beschreibung"
                    value={taskDescription}
                    onChange={(event) =>
                      setTaskDescription(
                        event.target.value
                      )
                    }
                    multiline
                    minRows={2}
                    fullWidth
                  />

                  <TextField
                    label="Fälligkeitsdatum"
                    type="date"
                    value={taskDueDate}
                    onChange={(event) =>
                      setTaskDueDate(
                        event.target.value
                      )
                    }
                    fullWidth
                    slotProps={{
                      inputLabel: {
                        shrink: true,
                      },
                    }}
                  />

                  <Stack
                    direction={{
                      xs: "column",
                      sm: "row",
                    }}
                    spacing={2}
                  >
                    <TextField
                      label="Wiederholungsintervall"
                      type="number"
                      value={
                        taskRecurrenceInterval
                      }
                      onChange={(event) =>
                        setTaskRecurrenceInterval(
                          event.target.value
                        )
                      }
                      fullWidth
                    />

                    <TextField
                      select
                      label="Einheit"
                      value={
                        taskRecurrenceUnit
                      }
                      onChange={(event) =>
                        setTaskRecurrenceUnit(
                          event.target
                            .value as
                            | ""
                            | RecurrenceUnit
                        )
                      }
                      fullWidth
                    >
                      <MenuItem value="">
                        Keine Wiederholung
                      </MenuItem>

                      <MenuItem value="MONTHS">
                        Monate
                      </MenuItem>

                      <MenuItem value="YEARS">
                        Jahre
                      </MenuItem>
                    </TextField>
                  </Stack>

                  <Stack
                    direction="row"
                    spacing={1}
                  >
                    <Button
                      type="submit"
                      variant="contained"
                    >
                      Wartungsaufgabe anlegen
                    </Button>

                    <Button
                      type="button"
                      onClick={() => {
                        setShowCreateForm(false);

                        setTaskTitle("");
                        setTaskDescription("");
                        setTaskDueDate("");
                        setTaskRecurrenceInterval("");
                        setTaskRecurrenceUnit("");
                      }}
                    >
                      Abbrechen
                    </Button>
                  </Stack>
                </Stack>
              </Box>
            )}

            {maintenanceTasks.length ===
            0 ? (
              <Alert severity="info">
                Noch keine Wartungsaufgaben
                für dieses Objekt vorhanden.
              </Alert>
            ) : (
              <Stack spacing={2}>
                {maintenanceTasks.map(
                  (task) => {
                    const isExpanded =
                      expandedTaskId ===
                      task.id;

                    const isEditing =
                      editingTaskId ===
                      task.id;

                    const dueStatus =
                      getDueStatus(
                        task.dueDate
                      );

                    return (
                      <Card
                        key={task.id}
                        variant="outlined"
                      >
                        {!isEditing && (
                          <CardActionArea
                            onClick={() =>
                              setExpandedTaskId(
                                isExpanded
                                  ? null
                                  : task.id
                              )
                            }
                          >
                            <CardContent>
                              <Stack
                                direction={{
                                  xs: "column",
                                  sm: "row",
                                }}
                                sx={{
                                justifyContent: "space-between",
                                alignItems: {
                                    xs: "flex-start",
                                    sm: "center"}
                                }}
                                spacing={2}
                              >
                                <Stack
                                  direction="row"
                                  spacing={1.5}
                                  sx={{
                                    alignItems: "center",
                                }}
                                    
                                >
                                  <BuildIcon
                                    color="action"
                                  />

                                  <div>
                                    <Typography
                                      variant="h6"
                                      component="h3"
                                    >
                                      {task.title}
                                    </Typography>

                                    <Typography
                                      variant="body2"
                                      color="text.secondary"
                                    >
                                      Fällig:{" "}
                                      {task.dueDate ||
                                        "Kein Termin"}
                                    </Typography>
                                  </div>
                                </Stack>

                                <Stack
                                  direction="row"
                                  spacing={1}
                                  sx={{
                                    alignItems: "center"
                                }}
                                  
                                >
                                  <Chip
                                    label={
                                      dueStatus
                                    }
                                    color={getDueStatusColor(
                                      dueStatus
                                    )}
                                    size="small"
                                  />

                                  <ExpandMoreIcon
                                    sx={{
                                      transform:
                                        isExpanded
                                          ? "rotate(180deg)"
                                          : "rotate(0deg)",
                                      transition:
                                        "transform 0.2s",
                                    }}
                                  />
                                </Stack>
                              </Stack>
                            </CardContent>
                          </CardActionArea>
                        )}

                        {isEditing && (
                          <CardContent>
                            <Stack spacing={2}>
                              <Typography
                                variant="h6"
                                component="h3"
                              >
                                Wartungsaufgabe
                                bearbeiten
                              </Typography>

                              <TextField
                                label="Titel"
                                value={
                                  editingTaskTitle
                                }
                                onChange={(event) =>
                                  setEditingTaskTitle(
                                    event.target
                                      .value
                                  )
                                }
                                required
                                fullWidth
                              />

                              <TextField
                                label="Beschreibung"
                                value={
                                  editingTaskDescription
                                }
                                onChange={(event) =>
                                  setEditingTaskDescription(
                                    event.target
                                      .value
                                  )
                                }
                                multiline
                                minRows={2}
                                fullWidth
                              />

                              <TextField
                                label="Fälligkeitsdatum"
                                type="date"
                                value={
                                  editingTaskDueDate
                                }
                                onChange={(event) =>
                                  setEditingTaskDueDate(
                                    event.target
                                      .value
                                  )
                                }
                                fullWidth
                                slotProps={{
                                  inputLabel: {
                                    shrink: true,
                                  },
                                }}
                              />

                              <Stack
                                direction={{
                                  xs: "column",
                                  sm: "row",
                                }}
                                spacing={2}
                              >
                                <TextField
                                  label="Wiederholungsintervall"
                                  type="number"
                                  value={
                                    editingTaskRecurrenceInterval
                                  }
                                  onChange={(event) =>
                                    setEditingTaskRecurrenceInterval(
                                      event
                                        .target
                                        .value
                                    )
                                  }
                                  fullWidth
                                />

                                <TextField
                                  select
                                  label="Einheit"
                                  value={
                                    editingTaskRecurrenceUnit
                                  }
                                  onChange={(event) =>
                                    setEditingTaskRecurrenceUnit(
                                      event
                                        .target
                                        .value as
                                        | ""
                                        | RecurrenceUnit
                                    )
                                  }
                                  fullWidth
                                >
                                  <MenuItem value="">
                                    Keine
                                    Wiederholung
                                  </MenuItem>

                                  <MenuItem value="MONTHS">
                                    Monate
                                  </MenuItem>

                                  <MenuItem value="YEARS">
                                    Jahre
                                  </MenuItem>
                                </TextField>
                              </Stack>

                              <Stack
                                direction="row"
                                spacing={1}
                              >
                                <Button
                                  variant="contained"
                                  onClick={() =>
                                    handleUpdateMaintenanceTask(
                                      task.id
                                    )
                                  }
                                >
                                  Speichern
                                </Button>

                                <Button
                                  onClick={
                                    handleCancelEdit
                                  }
                                >
                                  Abbrechen
                                </Button>
                              </Stack>
                            </Stack>
                          </CardContent>
                        )}

                        {!isEditing && (
                          <Collapse
                            in={isExpanded}
                            timeout="auto"
                            unmountOnExit
                          >
                            <Divider />

                            <CardContent>
                              <Stack spacing={2}>
                                <Typography variant="body2">
                                  <strong>
                                    Beschreibung:
                                  </strong>{" "}
                                  {task.description ||
                                    "Keine Beschreibung hinterlegt"}
                                </Typography>

                                <Typography variant="body2">
                                  <strong>
                                    Wiederholung:
                                  </strong>{" "}
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
                                </Typography>

                                <Typography variant="body2">
                                  <strong>
                                    Zuletzt erledigt:
                                  </strong>{" "}
                                  {task.completedAt ||
                                    "-"}
                                </Typography>

                                {!task.completed && (
                                  <TextField
                                    label="Notiz zur Durchführung"
                                    value={
                                      completionNotes[
                                        task.id
                                      ] ?? ""
                                    }
                                    onChange={(event) =>
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
                                    multiline
                                    minRows={2}
                                    fullWidth
                                  />
                                )}

                                {historyTaskId ===
                                  task.id && (
                                  <Box
                                    sx={{
                                      p: 2,
                                      bgcolor:
                                        "action.hover",
                                      borderRadius: 1,
                                    }}
                                  >
                                    <Typography
                                      variant="h6"
                                      component="h4"
                                      gutterBottom
                                    >
                                      Historie
                                    </Typography>

                                    {historyLoading ? (
                                      <Typography>
                                        Historie wird
                                        geladen...
                                      </Typography>
                                    ) : history.length ===
                                      0 ? (
                                      <Typography
                                        variant="body2"
                                        color="text.secondary"
                                      >
                                        Noch keine
                                        durchgeführten
                                        Arbeiten
                                        vorhanden.
                                      </Typography>
                                    ) : (
                                      <Stack
                                        spacing={2}
                                      >
                                        {history.map(
                                          (
                                            record
                                          ) => (
                                            <Box
                                              key={
                                                record.id
                                              }
                                            >
                                              <Typography variant="body2">
                                                <strong>
                                                  Durchgeführt
                                                  am:
                                                </strong>{" "}
                                                {
                                                  record.completedAt
                                                }
                                              </Typography>

                                              <Typography variant="body2">
                                                <strong>
                                                  Notiz:
                                                </strong>{" "}
                                                {record.note ||
                                                  "-"}
                                              </Typography>

                                              <Divider
                                                sx={{
                                                  mt: 2,
                                                }}
                                              />
                                            </Box>
                                          )
                                        )}
                                      </Stack>
                                    )}
                                  </Box>
                                )}
                              </Stack>
                            </CardContent>

                            <CardActions
                              sx={{
                                px: 2,
                                pb: 2,
                                flexWrap:
                                  "wrap",
                                gap: 1,
                              }}
                            >
                              <Button
                                startIcon={
                                  <EditIcon />
                                }
                                onClick={() =>
                                  handleEditMaintenanceTask(
                                    task
                                  )
                                }
                              >
                                Bearbeiten
                              </Button>

                              {!task.completed && (
                                <Button
                                  variant="contained"
                                  color="success"
                                  startIcon={
                                    <CheckCircleIcon />
                                  }
                                  onClick={() =>
                                    handleCompleteMaintenanceTask(
                                      task.id
                                    )
                                  }
                                >
                                  Erledigt
                                </Button>
                              )}

                              <Button
                                variant="outlined"
                                startIcon={
                                  <HistoryIcon />
                                }
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
                              </Button>

                              <Button
                                color="error"
                                startIcon={<DeleteIcon />}
                                onClick={() =>
                                    handleDeleteMaintenanceTask(task.id)
                                }
                                >
                                Löschen
                            </Button>
                            </CardActions>
                          </Collapse>
                        )}
                      </Card>
                    );
                  }
                )}
              </Stack>
            )}
          </>
        )}
      </Stack>
    </Paper>
  );
}

export default MaintenanceTaskSection;