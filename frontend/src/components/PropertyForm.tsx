import { useEffect, useState } from "react";

import {
  getProperty,
  updateProperty,
  type PropertyResponse,
} from "../services/propertyService";

type PropertyFormProps = {
  propertyId: number;
};

function PropertyForm({
  propertyId,
}: PropertyFormProps) {
  const [property, setProperty] =
    useState<PropertyResponse | null>(null);

  const [name, setName] = useState("");
  const [address, setAddress] = useState("");

  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    const loadProperty = async () => {
      try {
        const loadedProperty =
          await getProperty(propertyId);

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
  }, [propertyId]);

  const handleSubmit = async (
    event: React.SubmitEvent<HTMLFormElement>
  ) => {
    event.preventDefault();

    setMessage("");
    setError("");

    try {
      const updatedProperty =
        await updateProperty(propertyId, {
          name,
          address,
        });

      setProperty(updatedProperty);

      setMessage(
        "Immobilie wurde erfolgreich aktualisiert."
      );
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
    <section>
      <h2>Immobilie</h2>

      {error && <p>{error}</p>}
      {message && <p>{message}</p>}

      {property && (
        <form onSubmit={handleSubmit}>
          <div>
            <label htmlFor="propertyName">
              Bezeichnung
            </label>

            <input
              id="propertyName"
              type="text"
              value={name}
              onChange={(event) =>
                setName(event.target.value)
              }
              required
            />
          </div>

          <div>
            <label htmlFor="propertyAddress">
              Adresse
            </label>

            <input
              id="propertyAddress"
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
    </section>
  );
}

export default PropertyForm;