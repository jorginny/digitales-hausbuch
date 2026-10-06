import {
  useEffect,
  useRef,
  useState,
} from "react";

import {
  Link,
  useNavigate,
  useParams,
} from "react-router";

import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Stack,
  TextField,
  Typography,
} from "@mui/material";

import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import BuildIcon from "@mui/icons-material/Build";
import EditIcon from "@mui/icons-material/Edit";
import SaveIcon from "@mui/icons-material/Save";
import DeleteIcon from "@mui/icons-material/Delete";

import RoomSection from "../components/RoomSection";
import HouseholdObjectSection from "../components/HouseholdObjectSection";
import MaintenanceTaskSection from "../components/MaintenanceTaskSection";

import {
  getProperty,
  updateProperty,
  deleteProperty,
  type PropertyResponse,
} from "../services/propertyService";

/**
 * Central detail view of a property.
 *
 * Coordinates the selected room and household object and combines the room,
 * household-object and maintenance-task sections. The page also supports
 * editing and deleting the complete property.
 */
function PropertyDetailPage() {
  const { id } = useParams();

  const objectSectionRef =
    useRef<HTMLDivElement | null>(null);

  const maintenanceSectionRef =
    useRef<HTMLDivElement | null>(null);
  
  const navigate = useNavigate();  

  const [property, setProperty] =
    useState<PropertyResponse | null>(null);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState("");

  const [message, setMessage] =
    useState("");

  const [editing, setEditing] =
    useState(false);

  const [name, setName] =
    useState("");

  const [address, setAddress] =
    useState("");

  const [
    selectedRoomId,
    setSelectedRoomId,
  ] = useState<number | null>(null);

  const [
    selectedObjectId,
    setSelectedObjectId,
  ] = useState<number | null>(null);

  const [deleteDialogOpen, setDeleteDialogOpen] =
  useState(false);

const [deleting, setDeleting] =
  useState(false);

  useEffect(() => {
    if (!id) {
      setError(
        "Keine Immobilien-ID vorhanden."
      );

      setLoading(false);
      return;
    }

    const loadProperty = async () => {
      setError("");
      setLoading(true);

      try {
        const loadedProperty =
          await getProperty(Number(id));

        setProperty(loadedProperty);

        setName(loadedProperty.name);

        setAddress(
          loadedProperty.address ?? ""
        );
      } catch (error) {
        if (error instanceof Error) {
          setError(error.message);
        }
      } finally {
        setLoading(false);
      }
    };

    loadProperty();
  }, [id]);

  const handleSaveProperty = async (
    event: React.SubmitEvent<HTMLFormElement>
  ) => {
    event.preventDefault();

    if (!property) {
      return;
    }

    setError("");
    setMessage("");

    try {
      const updatedProperty =
        await updateProperty(
          property.id,
          {
            name,
            address,
          }
        );

      setProperty(updatedProperty);
      setEditing(false);

      setMessage(
        "Immobilie wurde erfolgreich aktualisiert."
      );
    } catch (error) {
      if (error instanceof Error) {
        setError(error.message);
      }
    }
  };

  const handleCancelEditing = () => {
    if (!property) {
      return;
    }

    setName(property.name);
    setAddress(
      property.address ?? ""
    );

    setEditing(false);
    setError("");
  };

  const handleSelectRoom = (
    roomId: number | null
  ) => {
    setSelectedRoomId(roomId);

    // Beim Raumwechsel wird die
    // Objektauswahl zurückgesetzt.
    setSelectedObjectId(null);

    // Nur bei Auswahl eines Raums scrollen.
    if (roomId !== null) {
      setTimeout(() => {
        objectSectionRef.current?.scrollIntoView({
          behavior: "smooth",
          block: "start",
        });
      }, 100);
    }
  };

  const handleSelectObject = (
    objectId: number | null
  ) => {
    setSelectedObjectId(objectId);

    // Nur bei Auswahl eines Objekts scrollen.
    if (objectId !== null) {
      setTimeout(() => {
        maintenanceSectionRef.current?.scrollIntoView({
          behavior: "smooth",
          block: "start",
        });
      }, 100);
    }
  };

  const handleDeleteProperty = async () => {
  if (!property) {
    return;
  }

  setDeleting(true);
  setError("");

  try {
    await deleteProperty(property.id);

    navigate("/properties");
  } catch (error) {
    if (error instanceof Error) {
      setError(error.message);
    }

    setDeleteDialogOpen(false);
  } finally {
    setDeleting(false);
  }
};

  if (loading) {
    return (
      <Stack
        direction="row"
        spacing={2}
        sx={{
          alignItems: "center",
        }}
      >
        <CircularProgress size={24} />

        <Typography>
          Immobilie wird geladen...
        </Typography>
      </Stack>
    );
  }

  if (error && !property) {
    return (
      <Alert severity="error">
        {error}
      </Alert>
    );
  }

  if (!property) {
    return (
      <Alert severity="error">
        Immobilie konnte nicht geladen werden.
      </Alert>
    );
  }

  const propertyId = property.id;

  return (
    <Box>
      <Stack spacing={4}>
        <Box>
          {editing ? (
            <Stack
              component="form"
              onSubmit={handleSaveProperty}
              spacing={2}
              sx={{
                maxWidth: 650,
              }}
            >
              <TextField
                label="Bezeichnung"
                value={name}
                onChange={(event) =>
                  setName(event.target.value)
                }
                required
                fullWidth
              />

              <TextField
                label="Adresse"
                value={address}
                onChange={(event) =>
                  setAddress(event.target.value)
                }
                fullWidth
              />

              <Stack
                direction={{
                  xs: "column",
                  sm: "row",
                }}
                spacing={2}
                sx={{
                  justifyContent: "space-between",
                  alignItems: {
                    xs: "stretch",
                    sm: "center",
                  },
                }}
              >
                <Stack
                  direction="row"
                  spacing={1}
                  sx={{
                    flexWrap: "wrap",
                  }}
                >
                  <Button
                    type="submit"
                    variant="contained"
                    startIcon={<SaveIcon />}
                  >
                    Speichern
                  </Button>

                  <Button
                    type="button"
                    onClick={handleCancelEditing}
                  >
                    Abbrechen
                  </Button>
                </Stack>

                <Button
                  type="button"
                  color="error"
                  variant="outlined"
                  startIcon={<DeleteIcon />}
                  onClick={() =>
                    setDeleteDialogOpen(true)
                  }
                >
                  Immobilie löschen
                </Button>
              </Stack>
            </Stack>
          ) : (
            <Stack
              direction={{
                xs: "column",
                md: "row",
              }}
              spacing={2}
              sx={{
                justifyContent: "space-between",
                alignItems: {
                  xs: "flex-start",
                  md: "center",
                },
              }}
            >
              <div>
                <Typography
                  variant="h3"
                  component="h1"
                  gutterBottom
                >
                  {property.name}
                </Typography>

                <Typography
                  variant="body1"
                  color="text.secondary"
                >
                  {property.address ||
                    "Keine Adresse hinterlegt"}
                </Typography>
              </div>

              <Stack
                direction={{
                  xs: "column",
                  sm: "row",
                }}
                spacing={1}
                sx={{
                  alignItems: {
                    xs: "stretch",
                    sm: "center",
                  },
                }}
              >
                <Button
                  variant="outlined"
                  startIcon={<EditIcon />}
                  onClick={() => {
                    setMessage("");
                    setEditing(true);
                  }}
                >
                  Bearbeiten
                </Button>

                <Button
                  component={Link}
                  to={`/properties/${propertyId}/maintenance`}
                  variant="contained"
                  startIcon={<BuildIcon />}
                >
                  Wartungsübersicht
                </Button>

                <Button
                  component={Link}
                  to="/properties"
                  variant="text"
                  startIcon={<ArrowBackIcon />}
                >
                  Immobilien
                </Button>
              </Stack>
            </Stack>
          )}
        </Box>

        {message && (
          <Alert severity="success">
            {message}
          </Alert>
        )}

        {error && (
          <Alert severity="error">
            {error}
          </Alert>
        )}

        <RoomSection
          propertyId={propertyId}
          selectedRoomId={selectedRoomId}
          onSelectRoom={handleSelectRoom}
        />

        <Box
          ref={objectSectionRef}
          sx={{
            scrollMarginTop: 24,
          }}
        >
          <HouseholdObjectSection
            propertyId={propertyId}
            selectedRoomId={selectedRoomId}
            selectedObjectId={selectedObjectId}
            onSelectObject={handleSelectObject}
          />
        </Box>

        <Box
          ref={maintenanceSectionRef}
          sx={{
            scrollMarginTop: 24,
          }}
        >
          <MaintenanceTaskSection
            propertyId={propertyId}
            selectedRoomId={selectedRoomId}
            selectedObjectId={selectedObjectId}
          />
        </Box>
      </Stack>

      <Dialog
        open={deleteDialogOpen}
        onClose={() => {
          if (!deleting) {
            setDeleteDialogOpen(false);
          }
        }}
        fullWidth
        maxWidth="sm"
      >
        <DialogTitle>
          Immobilie wirklich löschen?
        </DialogTitle>

        <DialogContent>
          <DialogContentText>
            Möchtest du die Immobilie „{property.name}“
            wirklich löschen?
          </DialogContentText>

          <Alert
            severity="warning"
            sx={{
              mt: 2,
            }}
          >
            Dabei werden auch alle zugehörigen Räume,
            Objekte, Wartungsaufgaben und
            Wartungshistorien dauerhaft gelöscht.
            Dieser Vorgang kann nicht rückgängig
            gemacht werden.
          </Alert>
        </DialogContent>

        <DialogActions>
          <Button
            onClick={() =>
              setDeleteDialogOpen(false)
            }
            disabled={deleting}
          >
            Abbrechen
          </Button>

          <Button
            onClick={handleDeleteProperty}
            color="error"
            variant="contained"
            startIcon={<DeleteIcon />}
            disabled={deleting}
          >
            {deleting
              ? "Wird gelöscht..."
              : "Endgültig löschen"}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
  }

export default PropertyDetailPage;