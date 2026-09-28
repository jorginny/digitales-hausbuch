import { useEffect, useState } from "react";
import { Link, useParams } from "react-router";

import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  Divider,
  Stack,
  Typography,
} from "@mui/material";

import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import BuildIcon from "@mui/icons-material/Build";

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

function getStatusColor(
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
    <Box>
        <Stack spacing={4}>
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
                variant="h4"
                component="h1"
                gutterBottom
            >
                Wartungsübersicht
            </Typography>

            <Typography
                variant="body1"
                color="text.secondary"
            >
                Alle offenen Wartungsaufgaben
                der Immobilie auf einen Blick.
            </Typography>
            </div>

            {id && (
            <Button
                component={Link}
                to={`/properties/${id}`}
                variant="outlined"
                startIcon={<ArrowBackIcon />}
            >
                Zur Immobilie
            </Button>
            )}
        </Stack>

        {error && (
            <Alert severity="error">
            {error}
            </Alert>
        )}

        {!error && tasks.length === 0 && (
            <Alert severity="success">
            Aktuell sind keine offenen
            Wartungsaufgaben vorhanden.
            </Alert>
        )}

        {tasks.length > 0 && (
            <Stack spacing={2}>
            {tasks.map((task) => {
                const dueStatus =
                getDueStatus(task.dueDate);

                return (
                <Card
                    key={task.taskId}
                    variant="outlined"
                >
                    <CardContent>
                  <Stack spacing={2}>
                    <Stack
                      direction={{
                        xs: "column",
                        sm: "row",
                      }}
                      spacing={1}
                      sx={{
                        justifyContent:
                          "space-between",
                        alignItems: {
                          xs: "flex-start",
                          sm: "center",
                        },
                      }}
                    >
                      <Stack
                        direction="row"
                        spacing={1}
                        sx={{
                          alignItems: "center",
                        }}
                      >
                        <BuildIcon
                          color="action"
                        />

                        <Typography
                          variant="h6"
                          component="h2"
                        >
                          {task.title}
                        </Typography>
                      </Stack>

                      <Chip
                        label={dueStatus}
                        color={getStatusColor(
                          dueStatus
                        )}
                        size="small"
                      />
                    </Stack>

                    <Divider />

                    <Box
                      sx={{
                        display: "grid",
                        gridTemplateColumns: {
                          xs: "1fr",
                          sm: "repeat(2, 1fr)",
                        },
                        gap: 1.5,
                      }}
                    >
                      <Typography variant="body2">
                        <strong>Raum:</strong>{" "}
                        {task.roomName}
                      </Typography>

                      <Typography variant="body2">
                        <strong>Objekt:</strong>{" "}
                        {task.objectName}
                      </Typography>

                      <Typography variant="body2">
                        <strong>Fällig am:</strong>{" "}
                        {task.dueDate ||
                          "Kein Termin"}
                      </Typography>

                      <Typography variant="body2">
                        <strong>
                          Zuletzt erledigt:
                        </strong>{" "}
                        {task.completedAt || "-"}
                      </Typography>
                    </Box>

                    <Typography variant="body2">
                      <strong>
                        Beschreibung:
                      </strong>{" "}
                      {task.description || "-"}
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
                  </Stack>
                </CardContent>
              </Card>
            );
          })}
        </Stack>
      )}
    </Stack>
  </Box>
);
  }}

  

export default MaintenanceOverviewPage;