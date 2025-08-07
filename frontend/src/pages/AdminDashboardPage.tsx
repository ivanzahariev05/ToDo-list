
import { useEffect, useState } from "react";
import { getAllUsers, promoteUser, deleteUser, type AdminUser } from "../services/adminService";
import '../admin.css';

export default function AdminDashboardPage() {
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchUsers = async () => {
    try {
      const data = await getAllUsers();
      setUsers(data);
      setError(null);
    } catch (err: any) {
      setError(err.response?.data?.message ?? "Failed to load users");
    } finally {
      setLoading(false);
    }
  };

  const handlePromote = async (id: string) => {
  try {
    await promoteUser(id);
    fetchUsers();
  } catch (err: any) {
    setError(err.response?.data?.message ?? "Promote failed");
  }
};

const handleDelete = async (id: string) => {
  try {
    await deleteUser(id);
    fetchUsers();
  } catch (err: any) {
    setError(err.response?.data?.message ?? "Delete failed");
  }
};


  useEffect(() => {
    fetchUsers();
  }, []);


  if (loading)
    return (
      <div className="admin-bg">
        <p className="admin-empty">Loading users…</p>
      </div>
    );
  if (error)
    return (
      <div className="admin-bg">
        <p className="admin-error">{error}</p>
      </div>
    );

  return (
    <div className="admin-bg">
      <div className="admin-card">
        <h1 className="admin-title">Admin Dashboard</h1>
        {users.length === 0 ? (
          <p className="admin-empty">No users found.</p>
        ) : (
          <div className="admin-table-wrap">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Username</th>
                  <th>Email</th>
                  <th>Role</th>
                  <th>Tasks Done</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.map((u) => (
                  <tr key={u.id}>
                    <td>{u.username}</td>
                    <td>{u.email}</td>
                    <td>{u.role}</td>
                    <td>{u.tasksDone}</td>
                    <td>
                      <button
                        onClick={() => handlePromote(u.id)}
                        className="admin-action-btn promote"
                      >
                        Promote
                      </button>
                      <button
                        onClick={() => handleDelete(u.id)}
                        className="admin-action-btn delete"
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
