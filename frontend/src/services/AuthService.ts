import api from "./api";
import { AxiosError } from "axios";

export type RegisterRequest = {
  username: string;
  email: string;
  password: string;
};

export type LoginRequest = {
  username: string;
  password: string;
};

export type AuthResponse = {
  accessToken: string;
  refreshToken: string;
};


export const register = async (data: RegisterRequest) => {
  try {
    const res = await api.post<AuthResponse>("/auth/register", data);
    return res.data;
  } catch (err) {
    let message = "Registration failed.";
    let fieldErrors: Record<string, string> = {};
    if (err && typeof err === "object" && (err as AxiosError).isAxiosError) {
      const axiosErr = err as AxiosError;
      if (axiosErr.response) {
        const data = axiosErr.response.data as any;
        if (data && typeof data === "object") {
          if (data.message) message = data.message;
          if (data.errors && typeof data.errors === "object") fieldErrors = data.errors;
        }
        if (axiosErr.response.status === 500) message = "A server error occurred. Please try again later.";
      }
    }
    throw { message, fieldErrors };
  }
};


export const login = async (data: LoginRequest) => {
  try {
    const res = await api.post<AuthResponse>("/auth/login", data);
    return res.data;
  } catch (err) {
    let message = "Login failed.";
    if (err && typeof err === "object" && (err as AxiosError).isAxiosError) {
      const axiosErr = err as AxiosError;
      if (axiosErr.response) {
        const data = axiosErr.response.data as any;
        if (data && typeof data === "object" && data.message) message = data.message;
        if (axiosErr.response.status === 401) message = "Wrong username or password";
        if (axiosErr.response.status === 500) message = "A server error occurred. Please try again later.";
      }
    }
    throw { message };
  }
};

export const refresh = async (refreshToken: string) => {
  const res = await api.post<AuthResponse>("/auth/refresh", { refreshToken });
  return res.data;
};

