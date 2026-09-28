import { useState } from "react";
import { Link } from "react-router";

import {
  Alert,
  Box,
  Button,
  Paper,
  Stack,
  TextField,
  Typography,
} from "@mui/material";

import AddHomeIcon from "@mui/icons-material/AddHome";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";

import { createProperty } from "../services/propertyService";

function CreatePropertyPage() {
  const [name, setName] = useState("");
  const [address, setAddress] = useState("");

  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const handleSubmit = async (
    event: React.SubmitEvent<HTMLFormElement>
  ) => {
    event.preventDefault();

    setMessage("");
    setError("");

    try {
      const property = await createProperty({
        name,
        address,
      });

      setMessage(
        `Immobilie "${property.name}" wurde erfolgreich angelegt.`
      );

      setName("");
      setAddress("");
    } catch (error) {
      if (error instanceof Error) {
        setError(error.message);
      } else {
        setError(
          "Ein unbekannter Fehler ist aufgetreten."
        );
      }
    }
  };

  return (
    <Box>
      <Stack spacing={4}>
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
              variant="h4"
              component="h1"
              gutterBottom
            >
              Immobilie anlegen
            </Typography>

            <Typography
              variant="body1"
              color="text.secondary"
            >
              Lege eine neue Immobilie an.
              Räume und Objekte kannst du
              anschließend hinzufügen.
            </Typography>
          </div>

          <Button
            component={Link}
            to="/properties"
            variant="outlined"
            startIcon={<ArrowBackIcon />}
          >
            Zurück
          </Button>
        </Stack>

        <Paper
          elevation={2}
          sx={{
            p: 3,
            maxWidth: 700,
          }}
        >
          <Stack spacing={3}>
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

            <Stack
              component="form"
              onSubmit={handleSubmit}
              spacing={2}
            >
              <TextField
                label="Bezeichnung"
                value={name}
                onChange={(event) =>
                  setName(
                    event.target.value
                  )
                }
                required
                fullWidth
              />

              <TextField
                label="Adresse"
                value={address}
                onChange={(event) =>
                  setAddress(
                    event.target.value
                  )
                }
                fullWidth
              />

              <Button
                type="submit"
                variant="contained"
                startIcon={<AddHomeIcon />}
                sx={{
                  alignSelf:
                    "flex-start",
                }}
              >
                Immobilie anlegen
              </Button>
            </Stack>
          </Stack>
        </Paper>
      </Stack>
    </Box>
  );
}

export default CreatePropertyPage;