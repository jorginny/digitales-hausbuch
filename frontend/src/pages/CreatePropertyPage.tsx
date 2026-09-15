import { useState } from "react";
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
        setError("Ein unbekannter Fehler ist aufgetreten.");
      }
    }
  };

  return (
    <div>
      <h1>Immobilie anlegen</h1>

      <form onSubmit={handleSubmit}>
        <div>
          <label htmlFor="name">Bezeichnung</label>
          <input
            id="name"
            type="text"
            value={name}
            onChange={(event) => setName(event.target.value)}
          />
        </div>

        <div>
          <label htmlFor="address">Adresse</label>
          <input
            id="address"
            type="text"
            value={address}
            onChange={(event) => setAddress(event.target.value)}
          />
        </div>

        <button type="submit">Immobilie anlegen</button>
      </form>

      {message && <p>{message}</p>}
      {error && <p>{error}</p>}
    </div>
  );
}

export default CreatePropertyPage;