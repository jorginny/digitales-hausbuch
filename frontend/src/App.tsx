import { Navigate, Route, Routes } from "react-router";

import RegisterPage from "./pages/RegisterPage";
import LoginPage from "./pages/LoginPage";
import DashboardPage from "./pages/DashboardPage";
import ProtectedRoute from "./components/ProtectedRoute";
import CreatePropertyPage from "./pages/CreatePropertyPage";
import PropertyDetailPage from "./pages/PropertyDetailPage";
import PropertyListPage from "./pages/PropertyListPage";
import MaintenanceOverviewPage from "./pages/MaintenanceOverviewPage";


function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />

      <Route path="/register" element={<RegisterPage />} />

      <Route path="/login" element={<LoginPage />} />

      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <DashboardPage />
          </ProtectedRoute>
        }
        />

      <Route
        path="/properties/new"
        element={
          <ProtectedRoute>
            <CreatePropertyPage />
          </ProtectedRoute>     
        }
        />

        <Route
          path="/properties/:id"
          element={
          <ProtectedRoute>
            <PropertyDetailPage />
          </ProtectedRoute>
        }
        />

        <Route
          path="/properties"
          element={
            <ProtectedRoute>
              <PropertyListPage />
            </ProtectedRoute>
          }
        />

        <Route
          path="/properties/:id/maintenance"
          element={
            <ProtectedRoute>
              <MaintenanceOverviewPage />
            </ProtectedRoute>
          }
        />

    </Routes>
  );
}

export default App;