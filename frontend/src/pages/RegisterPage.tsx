import { useState, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { register, type RegisterRequest } from "../services/AuthService";
import { useAuth } from "../context/AuthContext";
import '../register.css';

export default function RegisterPage() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [form, setForm] = useState<RegisterRequest>({
    username: "",
    email: "",
    password: "",
  });
  const [error, setError] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setFieldErrors({});
    try {
      const { accessToken, refreshToken } = await register(form);
      login(accessToken, refreshToken);
      navigate("/tasks", { replace: true });
    } catch (err: any) {
      setError(err?.message || "Registration failed");
      if (err?.fieldErrors) setFieldErrors(err.fieldErrors);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="register-bg">
      <div className="register-card">
        <h1 className="register-title">Register</h1>
        <form onSubmit={handleSubmit} className="register-form">
          <input
            type="text"
            name="username"
            placeholder="Username"
            value={form.username}
            onChange={handleChange}
            required
            className="register-input"
          />
          {fieldErrors.username && <div className="register-error">{fieldErrors.username}</div>}
          <input
            type="email"
            name="email"
            placeholder="Email"
            value={form.email}
            onChange={handleChange}
            required
            className="register-input"
          />
          {fieldErrors.email && <div className="register-error">{fieldErrors.email}</div>}
          <input
            type="password"
            name="password"
            placeholder="Password"
            value={form.password}
            onChange={handleChange}
            required
            className="register-input"
          />
          {fieldErrors.password && <div className="register-error">{fieldErrors.password}</div>}
          <button
            type="submit"
            disabled={loading}
            className="register-btn"
          >
            {loading ? "Registering…" : "Register"}
          </button>
        </form>
        {error && <p className="register-error">{error}</p>}
        <a href="/login" className="register-link">Already have an account? Login</a>
      </div>
    </div>
  );
}