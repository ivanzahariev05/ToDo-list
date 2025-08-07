import { createContext, useContext, useState, useCallback, useRef, useEffect } from "react";
import type { ReactNode } from "react";

interface ErrorContextType {
  error: string | null;
  showError: (msg: string) => void;
  clearError: () => void;
}

const ErrorContext = createContext<ErrorContextType | undefined>(undefined);

export function useError() {
  const ctx = useContext(ErrorContext);
  if (!ctx) throw new Error("useError must be used within ErrorProvider");
  return ctx;
}

export function ErrorProvider({ children }: { children: ReactNode }) {
  const [error, setError] = useState<string | null>(null);
  const timer = useRef<number | null>(null);

  const showError = useCallback((msg: string) => {
    setError(msg);
    if (timer.current) clearTimeout(timer.current);
    timer.current = window.setTimeout(() => setError(null), 4000);
  }, []);

  const clearError = useCallback(() => {
    setError(null);
    if (timer.current) clearTimeout(timer.current);
  }, []);

  useEffect(() => () => { if (timer.current) clearTimeout(timer.current); }, []);

  return (
    <ErrorContext.Provider value={{ error, showError, clearError }}>
      {children}
      {error && (
        <div
          style={{
            position: "fixed",
            top: 24,
            right: 24,
            zIndex: 9999,
            background: "#ff3b6b",
            color: "#fff",
            padding: "1rem 2rem",
            borderRadius: "1rem",
            boxShadow: "0 4px 24px 0 rgba(0,0,0,0.12)",
            fontWeight: 500,
            fontSize: "1.1rem",
            display: "flex",
            alignItems: "center",
            gap: "1rem",
            minWidth: 220,
            maxWidth: 400,
            animation: "fadeIn 0.3s"
          }}
        >
          <span style={{ flex: 1 }}>{error}</span>
          <button
            onClick={clearError}
            style={{
              background: "none",
              border: "none",
              color: "#fff",
              fontWeight: "bold",
              fontSize: "1.2rem",
              cursor: "pointer"
            }}
            aria-label="Close error notification"
          >
            ×
          </button>
        </div>
      )}
      <style>{`
        @keyframes fadeIn { from { opacity: 0; transform: translateY(-20px);} to { opacity: 1; transform: none; } }
      `}</style>
    </ErrorContext.Provider>
  );
}
