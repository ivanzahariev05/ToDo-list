import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import type { ReactNode } from "react";

type Props = {
  children: ReactNode;
  allowedRoles?: string[];   
}

export default function ProtectedRoute({ children, allowedRoles }: Props) {
  const { accessToken, roles } = useAuth();

  if (!accessToken) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !roles.some(r => allowedRoles.includes(r))) {
    return <Navigate to="/tasks" replace />; // няма достъп, пращаме го обратно
  }

  return <>{children}</>;
}
