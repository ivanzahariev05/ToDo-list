import { useState, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { login as loginApi, type LoginRequest } from "../services/AuthService";
import { useAuth } from "../context/AuthContext";
import '../login.css';

export default function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();

  const [form, setForm] = useState<LoginRequest>({
    username: "",
    password: "",
  });
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const { accessToken, refreshToken } = await loginApi(form);
      login(accessToken, refreshToken);
      navigate("/tasks", { replace: true });
    } catch (err: any) {
      let msg = err?.message || "Login failed";
      if (
        msg.toLowerCase().includes("badcredentials") ||
        msg.toLowerCase().includes("401")
      ) {
        msg = "Wrong username or password";
      }
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-bg">
      <div className="login-card">
        <h1 className="login-title">Login</h1>
        <form onSubmit={handleSubmit} className="login-form">
          <input
            type="text"
            name="username"
            placeholder="Username"
            value={form.username}
            onChange={handleChange}
            required
            className="login-input"
          />
          <input
            type="password"
            name="password"
            placeholder="Password"
            value={form.password}
            onChange={handleChange}
            required
            className="login-input"
          />
          <button
            type="submit"
            disabled={loading}
            className="login-btn"
          >
            {loading ? "Logging in…" : "Login"}
          </button>
        </form>
        {error && <p className="login-error">{error}</p>}
        <a href="/register" className="login-link">No account? Register</a>
      </div>
    </div>
  );
}
