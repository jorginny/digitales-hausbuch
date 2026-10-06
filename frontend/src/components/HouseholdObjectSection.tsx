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
  Divider,
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
  createHouseholdObject,
  deleteHouseholdObject,
  getHouseholdObjects,
  updateHouseholdObject,
  type HouseholdObjectResponse,
} from "../services/householdObjectService";

type HouseholdObjectSectionProps = {
  propertyId: number;
  selectedRoomId: number | null;
  selectedObjectId: number | null;
  onSelectObject: (objectId: number | null) => void;
};

/**
 * Displays and manages the household objects of the currently selected room.
 *
 * The component coordinates object selection as well as create, update and
 * delete operations and passes the selected object back to the parent page.
 */
function HouseholdObjectSection({
  propertyId,
  selectedRoomId,
  selectedObjectId,
  onSelectObject,
}: HouseholdObjectSectionProps) {
  const [householdObjects, setHouseholdObjects] =
    useState<HouseholdObjectResponse[]>([]);

  const [showCreateForm, setShowCreateForm] =
    useState(false);

  const [objectName, setObjectName] = useState("");
  const [objectDescription, setObjectDescription] =
    useState("");
  const [objectManufacturer, setObjectManufacturer] =
    useState("");
  const [objectModel, setObjectModel] = useState("");
  const [objectPurchaseDate, setObjectPurchaseDate] =
    useState("");

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

  const [editingObjectModel, setEditingObjectModel] =
    useState("");

  const [
    editingObjectPurchaseDate,
    setEditingObjectPurchaseDate,
  ] = useState("");

  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    setMessage("");
    setError("");

    if (selectedRoomId === null) {
      setHouseholdObjects([]);
      setShowCreateForm(false);
      setEditingObjectId(null);
      onSelectObject(null);
      return;
    }

    const loadHouseholdObjects = async () => {
      try {
        const loadedObjects =
          await getHouseholdObjects(
            propertyId,
            selectedRoomId
          );

        setHouseholdObjects(loadedObjects);
      } catch (error) {
        if (error instanceof Error) {
          setError(error.message);
        }
      }
    };

    loadHouseholdObjects();
  }, [propertyId, selectedRoomId, onSelectObject]);

  const handleCreateHouseholdObject = async (
    event: React.SubmitEvent<HTMLFormElement>
  ) => {
    event.preventDefault();

    if (selectedRoomId === null) {
      return;
    }

    setError("");
    setMessage("");

    try {
      const createdObject =
        await createHouseholdObject(
          propertyId,
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

      setShowCreateForm(false);

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

    setMessage("");
    setError("");
  };

  const handleCancelEdit = () => {
    setEditingObjectId(null);
  };

  const handleUpdateHouseholdObject =
    async (objectId: number) => {
      if (selectedRoomId === null) {
        return;
      }

      setError("");
      setMessage("");

      try {
        const updatedObject =
          await updateHouseholdObject(
            propertyId,
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
          currentObjects.map((householdObject) =>
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

  const handleDeleteHouseholdObject =
    async (objectId: number) => {
      if (selectedRoomId === null) {
        return;
      }

      const confirmed = window.confirm(
        "Möchtest du dieses Objekt wirklich löschen?"
      );

      if (!confirmed) {
        return;
      }

      setError("");
      setMessage("");

      try {
        await deleteHouseholdObject(
          propertyId,
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
          onSelectObject(null);
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

  return (
    <Paper elevation={2} sx={{ p: 3 }}>
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
            sm: "center"
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
              Objekte
            </Typography>

            <Typography
              variant="body2"
              color="text.secondary"
            >
              Wähle ein Objekt aus, um dessen
              Wartungsaufgaben zu verwalten.
            </Typography>
          </div>

          {selectedRoomId !== null &&
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
                Neues Objekt
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
        ) : (
          <>
            {showCreateForm && (
              <Box
                component="form"
                onSubmit={
                  handleCreateHouseholdObject
                }
              >
                <Stack spacing={2}>
                  <Typography
                    variant="h6"
                    component="h3"
                  >
                    Neues Objekt anlegen
                  </Typography>

                  <TextField
                    label="Name"
                    value={objectName}
                    onChange={(event) =>
                      setObjectName(
                        event.target.value
                      )
                    }
                    required
                    fullWidth
                  />

                  <TextField
                    label="Beschreibung"
                    value={objectDescription}
                    onChange={(event) =>
                      setObjectDescription(
                        event.target.value
                      )
                    }
                    multiline
                    minRows={2}
                    fullWidth
                  />

                  <TextField
                    label="Hersteller"
                    value={objectManufacturer}
                    onChange={(event) =>
                      setObjectManufacturer(
                        event.target.value
                      )
                    }
                    fullWidth
                  />

                  <TextField
                    label="Modell"
                    value={objectModel}
                    onChange={(event) =>
                      setObjectModel(
                        event.target.value
                      )
                    }
                    fullWidth
                  />

                  <TextField
                    label="Kaufdatum"
                    type="date"
                    value={objectPurchaseDate}
                    onChange={(event) =>
                      setObjectPurchaseDate(
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
                    direction="row"
                    spacing={1}
                  >
                    <Button
                      type="submit"
                      variant="contained"
                    >
                      Objekt anlegen
                    </Button>

                    <Button
                      type="button"
                      onClick={() => {
                        setShowCreateForm(false);
                        setObjectName("");
                        setObjectDescription("");
                        setObjectManufacturer("");
                        setObjectModel("");
                        setObjectPurchaseDate("");
                      }}
                    >
                      Abbrechen
                    </Button>
                  </Stack>
                </Stack>
              </Box>
            )}

            {householdObjects.length === 0 ? (
              <Alert severity="info">
                Noch keine Objekte in diesem
                Raum vorhanden.
              </Alert>
            ) : (
              <Box
                sx={{
                  display: "grid",
                  gridTemplateColumns: {
                    xs: "1fr",
                    md: "repeat(2, 1fr)",
                  },
                  gap: 2,
                }}
              >
                {householdObjects.map(
                  (householdObject) => {
                    const isSelected =
                      selectedObjectId ===
                      householdObject.id;

                    const isEditing =
                      editingObjectId ===
                      householdObject.id;

                    return (
                      <Card
                        key={householdObject.id}
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
                                label="Name"
                                value={
                                  editingObjectName
                                }
                                onChange={(event) =>
                                  setEditingObjectName(
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
                                  editingObjectDescription
                                }
                                onChange={(event) =>
                                  setEditingObjectDescription(
                                    event.target
                                      .value
                                  )
                                }
                                multiline
                                minRows={2}
                                fullWidth
                              />

                              <TextField
                                label="Hersteller"
                                value={
                                  editingObjectManufacturer
                                }
                                onChange={(event) =>
                                  setEditingObjectManufacturer(
                                    event.target
                                      .value
                                  )
                                }
                                fullWidth
                              />

                              <TextField
                                label="Modell"
                                value={
                                  editingObjectModel
                                }
                                onChange={(event) =>
                                  setEditingObjectModel(
                                    event.target
                                      .value
                                  )
                                }
                                fullWidth
                              />

                              <TextField
                                label="Kaufdatum"
                                type="date"
                                value={
                                  editingObjectPurchaseDate
                                }
                                onChange={(event) =>
                                  setEditingObjectPurchaseDate(
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
                                direction="row"
                                spacing={1}
                              >
                                <Button
                                  variant="contained"
                                  onClick={() =>
                                    handleUpdateHouseholdObject(
                                      householdObject.id
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
                                onSelectObject(
                                  isSelected
                                    ? null
                                    : householdObject.id
                                )
                              }
                            >
                              <CardContent>
                                <Stack spacing={1.5}>
                                  <Stack
                                    direction="row"
                                    sx={{
                                    justifyContent: "space-between",
                                    alignItems: "flex-start"
                                    }}
                                    spacing={1}
                                  >
                                    <Typography
                                      variant="h6"
                                      component="h3"
                                    >
                                      {
                                        householdObject.name
                                      }
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

                                  <Divider />

                                  <Typography
                                    variant="body2"
                                    color="text.secondary"
                                  >
                                    {householdObject.description ||
                                      "Keine Beschreibung"}
                                  </Typography>

                                  <Typography variant="body2">
                                    <strong>
                                      Hersteller:
                                    </strong>{" "}
                                    {householdObject.manufacturer ||
                                      "-"}
                                  </Typography>

                                  <Typography variant="body2">
                                    <strong>
                                      Modell:
                                    </strong>{" "}
                                    {householdObject.model ||
                                      "-"}
                                  </Typography>

                                  <Typography variant="body2">
                                    <strong>
                                      Kaufdatum:
                                    </strong>{" "}
                                    {householdObject.purchaseDate ||
                                      "-"}
                                  </Typography>
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
                                  handleEditHouseholdObject(
                                    householdObject
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
                                  handleDeleteHouseholdObject(
                                    householdObject.id
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
                  }
                )}
              </Box>
            )}
          </>
        )}
      </Stack>
    </Paper>
  );
}

export default HouseholdObjectSection;