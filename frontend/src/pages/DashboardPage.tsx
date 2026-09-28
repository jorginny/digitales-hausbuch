import {
  Box,
  Button,
  Card,
  CardActions,
  CardContent,
  Stack,
  Typography,
} from "@mui/material";

import AddHomeIcon from "@mui/icons-material/AddHome";
import HomeWorkIcon from "@mui/icons-material/HomeWork";

import { Link } from "react-router";

function DashboardPage() {
  return (
    <Box>
      <Stack spacing={1} sx={{ mb: 4 }}>
        <Typography variant="h4" component="h1">
          Dashboard
        </Typography>

        <Typography
          variant="body1"
          color="text.secondary"
        >
          Verwalte deine Immobilien, Räume,
          Haushaltsobjekte und Wartungsaufgaben.
        </Typography>
      </Stack>

      <Stack
        direction={{
          xs: "column",
          md: "row",
        }}
        spacing={3}
      >
        <Card sx={{ flex: 1 }}>
          <CardContent>
            <HomeWorkIcon
              sx={{
                fontSize: 40,
                mb: 1,
              }}
            />

            <Typography
              variant="h6"
              component="h2"
              gutterBottom
            >
              Immobilien verwalten
            </Typography>

            <Typography
              variant="body2"
              color="text.secondary"
            >
              Öffne deine Immobilien und verwalte
              Räume, Objekte und Wartungsaufgaben.
            </Typography>
          </CardContent>

          <CardActions>
            <Button
              component={Link}
              to="/properties"
              variant="outlined"
            >
              Immobilien anzeigen
            </Button>
          </CardActions>
        </Card>

        <Card sx={{ flex: 1 }}>
          <CardContent>
            <AddHomeIcon
              sx={{
                fontSize: 40,
                mb: 1,
              }}
            />

            <Typography
              variant="h6"
              component="h2"
              gutterBottom
            >
              Neue Immobilie
            </Typography>

            <Typography
              variant="body2"
              color="text.secondary"
            >
              Lege eine neue Immobilie an und füge
              anschließend Räume und Objekte hinzu.
            </Typography>
          </CardContent>

          <CardActions>
            <Button
              component={Link}
              to="/properties/new"
              variant="contained"
            >
              Immobilie anlegen
            </Button>
          </CardActions>
        </Card>
      </Stack>
    </Box>
  );
}

export default DashboardPage;