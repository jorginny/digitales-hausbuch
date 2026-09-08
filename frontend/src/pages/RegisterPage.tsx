import { useState } from "react";
import { register } from "../services/authService";


function RegisterPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const handleSubmit = async(
    event: React.SubmitEvent<HTMLFormElement>
) => {
    event.preventDefault();

    setMessage("");
    setError("");

    try {
    await register(email, password);

    setMessage("Registrierung erfolgreich.");

    setEmail("");
    setPassword("");
  } catch (error) {
    if (error instanceof Error) {
      setError(error.message);
    } else {
      setError("Bei der Registrierung ist ein unbekannter Fehler aufgetreten.");
    }
  }
};
  

  return (
    <div>
      <h1>Registrierung</h1>

      <form onSubmit={handleSubmit}>
        <div>
          <label htmlFor="email">E-Mail-Adresse</label>
          <input
            id="email"
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
          />
        </div>

        <div>
          <label htmlFor="password">Passwort</label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
          />
        </div>

        <button type="submit">Registrieren</button>
      </form>
      
      {message && <p>{message}</p>}
      {error && <p>{error}</p>}
    </div>
  );
}

export default RegisterPage;