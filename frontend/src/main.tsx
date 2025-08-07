import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import App from "./App";
import { AuthProvider } from "./context/AuthContext";
import { ErrorProvider, useError } from "./context/ErrorContext";
import { setGlobalShowError } from "./services/api";
import './index.css'


function ErrorConnector() {
  const { showError } = useError();
  React.useEffect(() => { setGlobalShowError(showError); }, [showError]);
  return null;
}

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <ErrorProvider>
      <ErrorConnector />
      <AuthProvider>
        <BrowserRouter>
          <App />
        </BrowserRouter>
      </AuthProvider>
    </ErrorProvider>
  </React.StrictMode>
);
