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
  Paper,
  Stack,
  TextField,
  Typography,
} from "@mui/material";

import AddIcon from "@mui/icons-material/Add";
import CheckCircleIcon from "@mui/icons-material/CheckCircle";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";

import {
  getRooms,
  createRoom,
  updateRoom,
  deleteRoom,
  type RoomResponse,
} from "../services/roomService";

type RoomSectionProps = {
  propertyId: number;
  selectedRoomId: number | null;
  onSelectRoom: (roomId: number | null) => void;
};

/**
 * Displays and manages the rooms of a property.
 *
 * The component handles room selection and the create, update and delete
 * operations used by the property detail view.
 */
function RoomSection({
  propertyId,
  selectedRoomId,
  onSelectRoom,
}: RoomSectionProps) {
  const [rooms, setRooms] =
    useState<RoomResponse[]>([]);

  const [showCreateForm, setShowCreateForm] =
    useState(false);

  const [newRoomName, setNewRoomName] =
    useState("");

  const [editingRoomId, setEditingRoomId] =
    useState<number | null>(null);

  const [editingRoomName, setEditingRoomName] =
    useState("");

  const [message, setMessage] =
    useState("");

  const [error, setError] =
    useState("");

  useEffect(() => {
    const loadRooms = async () => {
      setError("");

      try {
        const loadedRooms =
          await getRooms(propertyId);

        setRooms(loadedRooms);
      } catch (error) {
        if (error instanceof Error) {
          setError(error.message);
        }
      }
    };

    loadRooms();
  }, [propertyId]);

  const handleCreateRoom = async (
    event: React.SubmitEvent<HTMLFormElement>
  ) => {
    event.preventDefault();

    setMessage("");
    setError("");

    try {
      const createdRoom =
        await createRoom(propertyId, {
          name: newRoomName,
        });

      setRooms((currentRooms) => [
        ...currentRooms,
        createdRoom,
      ]);

      setNewRoomName("");
      setShowCreateForm(false);

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

    setMessage("");
    setError("");
  };

  const handleCancelEdit = () => {
    setEditingRoomId(null);
    setEditingRoomName("");
  };

  const handleUpdateRoom = async (
    roomId: number
  ) => {
    setMessage("");
    setError("");

    try {
      const updatedRoom =
        await updateRoom(
          propertyId,
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
    const confirmed = window.confirm(
      "Möchtest du diesen Raum wirklich löschen?"
    );

    if (!confirmed) {
      return;
    }

    setMessage("");
    setError("");

    try {
      await deleteRoom(propertyId, roomId);

      setRooms((currentRooms) =>
        currentRooms.filter(
          (room) => room.id !== roomId
        )
      );

      if (selectedRoomId === roomId) {
        onSelectRoom(null);
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

  return (
    <Paper
      elevation={2}
      sx={{
        p: 3,
      }}
    >
      <Stack spacing={3}>
        <Stack
          direction={{
            xs: "column",
            sm: "row",
          }}
          sx={{
          justifyContent: "space-between",
          alignItems: {
            xs: "flex-start",
            sm: "center",
            }
          }}
          spacing={2}
        >
          <div>
            <Typography
              variant="h5"
              component="h2"
              gutterBottom
            >
              Räume
            </Typography>

            <Typography
              variant="body2"
              color="text.secondary"
            >
              Wähle einen Raum aus, um seine
              Objekte und Wartungen zu verwalten.
            </Typography>
          </div>

          {!showCreateForm && (
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={() => {
                setShowCreateForm(true);
                setMessage("");
                setError("");
              }}
            >
              Neuer Raum
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

        {showCreateForm && (
          <Box
            component="form"
            onSubmit={handleCreateRoom}
          >
            <Stack spacing={2}>
              <Typography
                variant="h6"
                component="h3"
              >
                Neuen Raum anlegen
              </Typography>

              <TextField
                label="Raumname"
                value={newRoomName}
                onChange={(event) =>
                  setNewRoomName(
                    event.target.value
                  )
                }
                required
                fullWidth
              />

              <Stack
                direction="row"
                spacing={1}
              >
                <Button
                  type="submit"
                  variant="contained"
                >
                  Raum anlegen
                </Button>

                <Button
                  type="button"
                  onClick={() => {
                    setShowCreateForm(false);
                    setNewRoomName("");
                  }}
                >
                  Abbrechen
                </Button>
              </Stack>
            </Stack>
          </Box>
        )}

        {rooms.length === 0 ? (
          <Alert severity="info">
            Noch keine Räume angelegt.
          </Alert>
        ) : (
          <Box
            sx={{
              display: "grid",
              gridTemplateColumns: {
                xs: "1fr",
                sm: "repeat(2, 1fr)",
                md: "repeat(3, 1fr)",
              },
              gap: 2,
            }}
          >
            {rooms.map((room) => {
              const isSelected =
                selectedRoomId === room.id;

              const isEditing =
                editingRoomId === room.id;

              return (
                <Card
                  key={room.id}
                  variant="outlined"
                  sx={{
                    borderWidth: isSelected
                      ? 2
                      : 1,
                    borderColor: isSelected
                      ? "primary.main"
                      : "divider",
                  }}
                >
                  {isEditing ? (
                    <CardContent>
                      <Stack spacing={2}>
                        <TextField
                          label="Raumname"
                          value={
                            editingRoomName
                          }
                          onChange={(
                            event
                          ) =>
                            setEditingRoomName(
                              event.target
                                .value
                            )
                          }
                          required
                          fullWidth
                        />

                        <Stack
                          direction="row"
                          spacing={1}
                        >
                          <Button
                            variant="contained"
                            onClick={() =>
                              handleUpdateRoom(
                                room.id
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
                  ) : (
                    <>
                      <CardActionArea
                        onClick={() =>
                          onSelectRoom(
                            isSelected
                              ? null
                              : room.id
                          )
                        }
                      >
                        <CardContent
                          sx={{
                            minHeight: 110,
                          }}
                        >
                          <Stack
                            spacing={2}
                            sx={{
                            alignItems: "flex-start"
                            }}
                          >
                            <Typography
                              variant="h6"
                              component="h3"
                            >
                              {room.name}
                            </Typography>

                            {isSelected && (
                              <Chip
                                icon={
                                  <CheckCircleIcon />
                                }
                                label="Ausgewählt"
                                color="primary"
                                size="small"
                              />
                            )}
                          </Stack>
                        </CardContent>
                      </CardActionArea>

                      <CardActions>
                        <Button
                          size="small"
                          startIcon={
                            <EditIcon />
                          }
                          onClick={() =>
                            handleEditRoom(
                              room
                            )
                          }
                        >
                          Bearbeiten
                        </Button>

                        <Button
                          size="small"
                          color="error"
                          startIcon={
                            <DeleteIcon />
                          }
                          onClick={() =>
                            handleDeleteRoom(
                              room.id
                            )
                          }
                        >
                          Löschen
                        </Button>
                      </CardActions>
                    </>
                  )}
                </Card>
              );
            })}
          </Box>
        )}
      </Stack>
    </Paper>
  );
}

export default RoomSection;