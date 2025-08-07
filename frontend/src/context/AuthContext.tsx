import {
  createContext,
  useContext,
  useState,
  useEffect,
  type ReactNode,
} from "react";
import { jwtDecode } from "jwt-decode";

// Тип за payload от токена
type JwtPayload = {
  sub: string;        // username
  roles: string[];    // ["ROLE_USER"] или ["ROLE_ADMIN"]
  exp: number;        // expiration timestamp
};

// Тип за Auth контекста
type AuthState = {
  accessToken: string | null;
  refreshToken: string | null;
  roles: string[];
  login: (at: string, rt: string) => void;
  logout: () => void;
};

// Създаваме контекста
const AuthContext = createContext<AuthState | undefined>(undefined);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  // Държим токените и ролите в state
  const [accessToken, setAccessToken] = useState<string | null>(() =>
    localStorage.getItem("accessToken")
  );
  const [refreshToken, setRefreshToken] = useState<string | null>(() =>
    localStorage.getItem("refreshToken")
  );
  const [roles, setRoles] = useState<string[]>([]);

  // Извличаме roles от токена при зареждане
  useEffect(() => {
    if (accessToken) {
      try {
        const decoded = jwtDecode<JwtPayload>(accessToken);
        setRoles(decoded.roles || []);
      } catch {
        setRoles([]);
      }
    } else {
      setRoles([]);
    }
  }, [accessToken]);

  // Логин
  const login = (at: string, rt: string) => {
    localStorage.setItem("accessToken", at);
    localStorage.setItem("refreshToken", rt);
    setAccessToken(at);
    setRefreshToken(rt);

    try {
      const decoded = jwtDecode<JwtPayload>(at);
      setRoles(decoded.roles || []);
    } catch {
      setRoles([]);
    }
  };

  // Логаут
  const logout = () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    setAccessToken(null);
    setRefreshToken(null);
    setRoles([]);
  };

  return (
    <AuthContext.Provider value={{ accessToken, refreshToken, roles, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

// Hook за достъп до контекста
export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used inside AuthProvider");
  return ctx;
};
