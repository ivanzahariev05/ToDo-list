import { useAuth } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";

function parseUsername(token?: string | null): string | null {
  if (!token) return null;
  try {
    const payload = JSON.parse(atob(token.split(".")[1]));
    return payload.username || payload.sub || null;
  } catch {
    return null;
  }
}

export function LogoutButton() {
  const { accessToken, logout } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState<string | null>(null);

  useEffect(() => {
    setUsername(parseUsername(accessToken));
  }, [accessToken]);

  if (!accessToken) return null;

  return (
    <div style={{
      position: "fixed",
      top: 24,
      right: 24,
      zIndex: 10000,
      display: "flex",
      alignItems: "center",
      gap: 12
    }}>
      {username && (
        <span style={{
          color: "#a259ff",
          fontWeight: 600,
          background: "#fff",
          borderRadius: 8,
          padding: "0.4rem 0.9rem",
          marginRight: 8,
          boxShadow: "0 1px 4px 0 rgba(162,89,255,0.10)",
          fontSize: "1rem"
        }}>{username}</span>
      )}
      <button
        onClick={() => {
          logout();
          navigate("/login", { replace: true });
        }}
        style={{
          background: "linear-gradient(90deg, #ff5858 0%, #a259ff 100%)",
          color: "#fff",
          border: "none",
          borderRadius: 8,
          padding: "0.5rem 1.2rem",
          fontWeight: 700,
          fontSize: "1rem",
          cursor: "pointer",
          boxShadow: "0 1px 4px 0 rgba(162,89,255,0.10)",
          transition: "background 0.2s, transform 0.1s"
        }}
      >
        Logout
      </button>
    </div>
  );
}
