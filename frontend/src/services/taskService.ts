// src/services/taskService.ts
import api from "./api";
import type { Task } from "../types/Task";

export const getAllTasks = async () => {
  const res = await api.get<Task[]>("/tasks");
  return res.data;
};

export type CreateTaskRequest = {
  title: string;
  description: string;
};

export const createTask = async (data: CreateTaskRequest) => {
  const res = await api.post<Task>("/tasks", data);
  return res.data;
};

export type UpdateTaskRequest = {
  title: string;
  description: string;
  isActive: boolean;
};

export const updateTask = async (id: number, data: UpdateTaskRequest) => {
  await api.put(`/tasks/${id}`, data);
};

export const deleteTask = async (id: number) => {
  await api.delete(`/tasks/${id}`);
};

export const toggleTaskCompletion = async (id: number) => {
  const res = await api.put<Task>(`/tasks/${id}/toggle-completion`);
  return res.data;
};


