import api from "./api";

export type AdminUser = {
  id: string;
  username: string;
  email: string;
  tasksDone: number;     // 🆕 от entity-то
  role: string;          // USER / ADMIN
};

export const getAllUsers = async () => {
  const res = await api.get<AdminUser[]>("/admin/users");
  return res.data;
};

export const promoteUser = async (id: string) => {
  const res = await api.post(`/admin/users/${id}/promote`);
  return res.data;
};

export const deleteUser = async (id: string) => {
  await api.delete(`/admin/users/${id}`);
};

