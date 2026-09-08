type ApiErrorResponse = {
  message?: string;
  email?: string;
  password?: string;
};

export async function register(
  email: string,
  password: string
): Promise<void> {
  const response = await fetch(
    "http://localhost:8080/api/auth/register",
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        email,
        password,
      }),
    }
  );

  if (!response.ok) {
    const errorData: ApiErrorResponse = await response.json();

    if (errorData.message) {
      throw new Error(errorData.message);
    }

    const validationErrors = [
      errorData.email,
      errorData.password,
    ].filter(Boolean);

    if (validationErrors.length > 0) {
      throw new Error(validationErrors.join(" "));
    }

    throw new Error("Registrierung fehlgeschlagen.");
  }
}