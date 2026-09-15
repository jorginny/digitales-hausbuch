import { useEffect, useState } from "react";
import { Link } from "react-router";
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
    <div>
      <h1>Meine Immobilien</h1>

      {error && <p>{error}</p>}

      {properties.length === 0 && !error && (
        <p>Noch keine Immobilien angelegt.</p>
      )}

      <ul>
        {properties.map((property) => (
          <li key={property.id}>
            <Link to={`/properties/${property.id}`}>
              {property.name}
            </Link>
          </li>
        ))}
      </ul>

      <Link to="/properties/new">
        Neue Immobilie anlegen
      </Link>
    </div>
  );
}

export default PropertyListPage;