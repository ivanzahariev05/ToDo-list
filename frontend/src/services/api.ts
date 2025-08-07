import axios, { AxiosError, type AxiosRequestConfig } from "axios";
import { refresh as refreshTokenApi } from "./AuthService";

const api = axios.create({
  baseURL: "http://localhost:8080/api",
});

// ---------- 1. Authorization header за всяка заявка ----------
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// ---------- 2. Response интерцептор за автоматичен refresh ----------
let isRefreshing = false;
let failedQueue: ((token: string | null) => void)[] = [];

const processQueue = (token: string | null) => {
  failedQueue.forEach((cb) => cb(token));
  failedQueue = [];
};

let globalShowError: ((msg: string) => void) | null = null;

// Helper to set the global error handler from ErrorProvider
export function setGlobalShowError(fn: (msg: string) => void) {
  globalShowError = fn;
}

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalReq = error.config as AxiosRequestConfig & { _retry?: boolean };

    // ако е 401 И не сме опитвали вече refresh за тази заявка
    if (error.response?.status === 401 && !originalReq._retry) {
      originalReq._retry = true;

      if (isRefreshing) {
        // чакаме текущия refresh да приключи
        return new Promise((resolve) => {
          failedQueue.push((token) => {
            if (token) originalReq.headers!.Authorization = `Bearer ${token}`;
            resolve(api(originalReq));
          });
        });
      }

      isRefreshing = true;
      try {
        const storedRefresh = localStorage.getItem("refreshToken");
        if (!storedRefresh) throw new Error("No refresh token");

        const { accessToken, refreshToken } = await refreshTokenApi(storedRefresh);

        // 1) записваме новите токени
        localStorage.setItem("accessToken", accessToken);
        localStorage.setItem("refreshToken", refreshToken);

        // 2) подменяме Header и пускаме отново опашката
        processQueue(accessToken);
        originalReq.headers!.Authorization = `Bearer ${accessToken}`;

        return api(originalReq);          // връщаме ретри заявката
      } catch (refreshErr) {
        processQueue(null);               // неуспех → всички чакащи падат
        // почистваме токените – потребителят ще бъде изкаран от профила
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        return Promise.reject(refreshErr);
      } finally {
        isRefreshing = false;
      }
    }

    // ако не е 401 → глобално съобщаваме грешката
    if (globalShowError) {
      let msg = "An error occurred.";
      if (error.response?.data && typeof error.response.data === "object") {
        // Try to extract backend error message
        msg = (error.response.data as any).message || msg;
      } else if (typeof error.message === "string") {
        msg = error.message;
      }
      globalShowError(msg);
    }
    return Promise.reject(error);
  }
);

export default api;
