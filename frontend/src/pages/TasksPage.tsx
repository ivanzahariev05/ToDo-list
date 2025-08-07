import { useEffect, useState, type FormEvent } from "react";
import {
  getAllTasks,
  createTask,
  updateTask,
  deleteTask,
  toggleTaskCompletion,
  type CreateTaskRequest,
} from "../services/taskService";
import type { Task } from "../types/Task";
import { useAuth } from "../context/AuthContext";
import { Link } from "react-router-dom";
import '../tasks.css';

export default function TasksPage() {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const { roles } = useAuth();

  const [newTask, setNewTask] = useState<CreateTaskRequest>({
    title: "",
    description: "",
  });

  const fetchTasks = async () => {
    try {
      const data = await getAllTasks();
      setTasks(data);
      setError(null);
    } catch (err: any) {
      setError(err.response?.data?.message ?? "Failed to load tasks");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTasks();
  }, []);

  const handleCreate = async (e: FormEvent) => {
    e.preventDefault();
    if (!newTask.title.trim()) return;
    try {
      await createTask(newTask);
      setNewTask({ title: "", description: "" });
      fetchTasks();
    } catch (err: any) {
      setError(err.response?.data?.message ?? "Create failed");
    }
  };

  const handleToggle = async (t: Task) => {
    try {
      const updatedTask = await toggleTaskCompletion(t.id);
      setTasks((prev) => prev.map((x) => (x.id === t.id ? updatedTask : x)));
    } catch (err: any) {
      setError(err.response?.data?.message ?? "Toggle failed");
    }
  };

  const handleEdit = async (t: Task) => {
    const newTitle = prompt("New title:", t.title);
    if (newTitle === null) return;

    const newDesc = prompt("New description:", t.description);
    if (newDesc === null) return;

    try {
      await updateTask(t.id, {
        title: newTitle,
        description: newDesc,
        isActive: t.isActive,
      });

      setTasks((prev) =>
        prev.map((x) =>
          x.id === t.id ? { ...x, title: newTitle, description: newDesc } : x
        )
      );
    } catch (err: any) {
      setError(err.response?.data?.message ?? "Update failed");
    }
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteTask(id);
      fetchTasks();
    } catch (err: any) {
      setError(err.response?.data?.message ?? "Delete failed");
    }
  };

  if (loading) return <p className="tasks-empty">Loading tasks…</p>;
  if (error) return <p className="tasks-error">{error}</p>;

  return (
    <div className="tasks-bg">
      <div className="tasks-card">
        {roles.includes("ROLE_ADMIN") && (
          <div className="tasks-admin-link">
            <Link to="/admin" className="tasks-admin-btn">Admin Dashboard</Link>
          </div>
        )}
        <h1 className="tasks-title">Your Tasks</h1>
        <form onSubmit={handleCreate} className="tasks-form">
          <input
            type="text"
            placeholder="Title"
            value={newTask.title}
            onChange={(e) => setNewTask({ ...newTask, title: e.target.value })}
            required
            className="tasks-input"
          />
          <textarea
            placeholder="Description"
            value={newTask.description}
            onChange={(e) => setNewTask({ ...newTask, description: e.target.value })}
            rows={3}
            className="tasks-textarea"
          />
          <button type="submit" className="tasks-btn">Add Task</button>
        </form>
        {tasks.length === 0 ? (
          <p className="tasks-empty">No tasks yet.</p>
        ) : (
          <ul className="tasks-list">
            {tasks.map((t) => (
              <li key={t.id} className="tasks-item">
                <div>
                  <span className={`tasks-item-title${t.isActive ? '' : ' done'}`}>{t.title}</span>
                  {" "}— <span className="tasks-item-desc">{t.description}</span>
                </div>
                <div className="tasks-actions">
                  <button
                    onClick={() => handleToggle(t)}
                    className="tasks-action-btn toggle"
                  >
                    {t.isActive ? "Done" : "Undone"}
                  </button>
                  <button
                    onClick={() => handleEdit(t)}
                    className="tasks-action-btn edit"
                  >
                    Edit
                  </button>
                  <button
                    onClick={() => handleDelete(t.id)}
                    className="tasks-action-btn delete"
                  >
                    Delete
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
