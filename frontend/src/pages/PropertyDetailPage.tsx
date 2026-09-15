import { useEffect, useState } from "react";
import { useParams } from "react-router";
import {
  getProperty,
  updateProperty,
  type PropertyResponse,
} from "../services/propertyService";

function PropertyDetailPage() {
  const { id } = useParams();

  const [property, setProperty] =
    useState<PropertyResponse | null>(null);

  const [name, setName] = useState("");
  const [address, setAddress] = useState("");

  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    const loadProperty = async () => {
      if (!id) {
        setError("Keine Immobilien-ID vorhanden.");
        return;
      }

      try {
        const loadedProperty =
          await getProperty(Number(id));

        setProperty(loadedProperty);
        setName(loadedProperty.name);
        setAddress(loadedProperty.address ?? "");
      } catch (error) {
        if (error instanceof Error) {
          setError(error.message);
        }
      }
    };

    loadProperty();
  }, [id]);

  const handleSubmit = async (
    event: React.SubmitEvent<HTMLFormElement>
  ) => {
    event.preventDefault();

    if (!id) {
      return;
    }

    setMessage("");
    setError("");

    try {
      const updatedProperty =
        await updateProperty(Number(id), {
          name,
          address,
        });

      setProperty(updatedProperty);
      setMessage("Immobilie wurde erfolgreich aktualisiert.");
    } catch (error) {
      if (error instanceof Error) {
        setError(error.message);
      }
    }
  };

  if (!property && !error) {
    return <p>Immobilie wird geladen...</p>;
  }

  return (
    <div>
      <h1>Immobilie bearbeiten</h1>

      {error && <p>{error}</p>}

      {property && (
        <form onSubmit={handleSubmit}>
          <div>
            <label htmlFor="name">
              Bezeichnung
            </label>

            <input
              id="name"
              type="text"
              value={name}
              onChange={(event) =>
                setName(event.target.value)
              }
            />
          </div>

          <div>
            <label htmlFor="address">
              Adresse
            </label>

            <input
              id="address"
              type="text"
              value={address}
              onChange={(event) =>
                setAddress(event.target.value)
              }
            />
          </div>

          <button type="submit">
            Änderungen speichern
          </button>
        </form>
      )}

      {message && <p>{message}</p>}
    </div>
  );
}

export default PropertyDetailPage;