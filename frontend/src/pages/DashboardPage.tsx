import { logout } from "../services/authService";
import { useNavigate } from "react-router";



function DashboardPage() {

  const navigate = useNavigate();

  const handleLogout = async () => {
  try {
    await logout();
    navigate("/login");
  } catch (error) {
    console.error(error);
  }
};

  return (
    <div>
      <h1>Dashboard</h1>

      <p>Du bist eingeloggt.</p>

      <button onClick={handleLogout}>
        Abmelden
      </button>
    </div>
  );
}

export default DashboardPage;