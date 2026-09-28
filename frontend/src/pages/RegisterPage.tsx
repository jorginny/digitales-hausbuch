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

import PersonAddIcon from "@mui/icons-material/PersonAdd";

import { register } from "../services/authService";

function RegisterPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const handleSubmit = async (
    event: React.SubmitEvent<HTMLFormElement>
  ) => {
    event.preventDefault();

    setMessage("");
    setError("");

    try {
      await register(email, password);

      setMessage(
        "Registrierung erfolgreich. Du kannst dich jetzt anmelden."
      );

      setEmail("");
      setPassword("");
    } catch (error) {
      if (error instanceof Error) {
        setError(error.message);
      } else {
        setError(
          "Bei der Registrierung ist ein unbekannter Fehler aufgetreten."
        );
      }
    }
  };

  return (
    <Box
      sx={{
        minHeight: "100vh",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        px: 2,
        bgcolor: "background.default",
      }}
    >
      <Paper
        elevation={4}
        sx={{
          width: "100%",
          maxWidth: 420,
          p: 4,
        }}
      >
        <Stack spacing={3}>
          <div>
            <Typography
              variant="h4"
              component="h1"
              gutterBottom
            >
              Registrierung
            </Typography>

            <Typography
              variant="body1"
              color="text.secondary"
            >
              Erstelle ein Konto für dein
              digitales Hausbuch.
            </Typography>
          </div>

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
              label="E-Mail-Adresse"
              type="email"
              value={email}
              onChange={(event) =>
                setEmail(event.target.value)
              }
              required
              fullWidth
              autoComplete="email"
            />

            <TextField
              label="Passwort"
              type="password"
              value={password}
              onChange={(event) =>
                setPassword(event.target.value)
              }
              required
              fullWidth
              autoComplete="new-password"
            />

            <Button
              type="submit"
              variant="contained"
              size="large"
              startIcon={<PersonAddIcon />}
              fullWidth
            >
              Registrieren
            </Button>
          </Stack>

          <Typography
            variant="body2"
            color="text.secondary"
            sx={{
            textAlign: "center"
            }}
          >
            Bereits registriert?{" "}
            <Link to="/login">
              Zur Anmeldung
            </Link>
          </Typography>
        </Stack>
      </Paper>
    </Box>
  );
}

export default RegisterPage;