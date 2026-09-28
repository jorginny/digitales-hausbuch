import {
  AppBar,
  Box,
  Button,
  Container,
  Toolbar,
  Typography,
} from "@mui/material";

import { Link, Outlet, useNavigate } from "react-router";

import { logout } from "../services/authService";

function AppLayout() {
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      await logout();

      navigate("/login");
    } catch (error) {
      console.error(
        "Abmelden fehlgeschlagen.",
        error
      );
    }
  };

  return (
    <Box>
      <AppBar position="static">
        <Toolbar>
          <Typography
            variant="h6"
            component="div"
            sx={{
              flexGrow: 1,
            }}
          >
            Digitales Hausbuch
          </Typography>

          <Button
            color="inherit"
            component={Link}
            to="/dashboard"
          >
            Dashboard
          </Button>

          <Button
            color="inherit"
            onClick={handleLogout}
          >
            Abmelden
          </Button>
        </Toolbar>
      </AppBar>

      <Container
        maxWidth="lg"
        sx={{
          py: 4,
        }}
      >
        <Outlet />
      </Container>
    </Box>
  );
}

export default AppLayout;