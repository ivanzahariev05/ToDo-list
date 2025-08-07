import { useEffect } from "react";
import { useLocation } from "react-router-dom";

const TITLES: Record<string, string> = {
  "/": "Todo - Login",
  "/login": "Todo - Login",
  "/register": "Todo - Register",
  "/tasks": "Todo - Tasks",
  "/admin": "Todo - Dashboard",
};

export function PageTitleUpdater() {
  const location = useLocation();
  useEffect(() => {
    const title = TITLES[location.pathname] || "Todo App";
    document.title = title;
  }, [location.pathname]);
  return null;
}
