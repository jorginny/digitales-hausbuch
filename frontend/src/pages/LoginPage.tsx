import { useState } from "react";
import { Link, useNavigate } from "react-router";

import {
  Alert,
  Box,
  Button,
  Paper,
  Stack,
  TextField,
  Typography,
} from "@mui/material";

import LoginIcon from "@mui/icons-material/Login";

import { login } from "../services/authService";

function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [error, setError] = useState("");

  const navigate = useNavigate();

  const handleSubmit = async (
    event: React.SubmitEvent<HTMLFormElement>
  ) => {
    event.preventDefault();

    setError("");

    try {
      await login(email, password);

      navigate("/dashboard");
    } catch (error) {
      if (error instanceof Error) {
        setError(error.message);
      } else {
        setError(
          "Bei der Anmeldung ist ein unbekannter Fehler aufgetreten."
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
              Digitales Hausbuch
            </Typography>

            <Typography
              variant="body1"
              color="text.secondary"
            >
              Melde dich an, um deine Immobilien,
              Räume und Wartungsaufgaben zu verwalten.
            </Typography>
          </div>

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
              autoComplete="current-password"
            />

            <Button
              type="submit"
              variant="contained"
              size="large"
              startIcon={<LoginIcon />}
              fullWidth
            >
              Anmelden
            </Button>
          </Stack>

          <Typography
            variant="body2"
            color="text.secondary"
            sx={{
            textAlign: "center"
            }}
          >
            Noch kein Konto?{" "}
            <Link to="/register">
              Jetzt registrieren
            </Link>
          </Typography>
        </Stack>
      </Paper>
    </Box>
  );
}

export default LoginPage;