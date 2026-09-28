import { useEffect, useState } from "react";
import { Link } from "react-router";

import {
  Alert,
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

import {
  getProperties,
  type PropertyResponse,
} from "../services/propertyService";

function PropertyListPage() {
  const [properties, setProperties] =
    useState<PropertyResponse[]>([]);

  const [error, setError] = useState("");

  useEffect(() => {
    const loadProperties = async () => {
      try {
        const data = await getProperties();
        setProperties(data);
      } catch (error) {
        if (error instanceof Error) {
          setError(error.message);
        }
      }
    };

    loadProperties();
  }, []);

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
          },
        }}
          spacing={2}
        >
          <div>
            <Typography
              variant="h4"
              component="h1"
              gutterBottom
            >
              Meine Immobilien
            </Typography>

            <Typography
              variant="body1"
              color="text.secondary"
            >
              Wähle eine Immobilie aus oder lege
              eine neue Immobilie an.
            </Typography>
          </div>

          <Button
            component={Link}
            to="/properties/new"
            variant="contained"
            startIcon={<AddHomeIcon />}
          >
            Neue Immobilie
          </Button>
        </Stack>

        {error && (
          <Alert severity="error">
            {error}
          </Alert>
        )}

        {!error && properties.length === 0 && (
          <Alert severity="info">
            Noch keine Immobilien angelegt.
          </Alert>
        )}

        {properties.length > 0 && (
          <Box
            sx={{
              display: "grid",
              gridTemplateColumns: {
                xs: "1fr",
                sm: "repeat(2, 1fr)",
                lg: "repeat(3, 1fr)",
              },
              gap: 3,
            }}
          >
            {properties.map((property) => (
              <Card
                key={property.id}
                variant="outlined"
              >
                <CardContent>
                  <Stack spacing={2}>
                    <HomeWorkIcon
                      sx={{
                        fontSize: 40,
                      }}
                      color="primary"
                    />

                    <Typography
                      variant="h6"
                      component="h2"
                    >
                      {property.name}
                    </Typography>

                    <Typography
                      variant="body2"
                      color="text.secondary"
                    >
                      {property.address ||
                        "Keine Adresse hinterlegt"}
                    </Typography>
                  </Stack>
                </CardContent>

                <CardActions>
                  <Button
                    component={Link}
                    to={`/properties/${property.id}`}
                    variant="outlined"
                  >
                    Öffnen
                  </Button>

                  <Button
                    component={Link}
                    to={`/properties/${property.id}/maintenance`}
                  >
                    Wartungen
                  </Button>
                </CardActions>
              </Card>
            ))}
          </Box>
        )}
      </Stack>
    </Box>
  );
}

export default PropertyListPage;