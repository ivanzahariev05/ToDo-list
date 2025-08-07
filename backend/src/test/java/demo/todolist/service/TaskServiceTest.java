package demo.todolist.service;

import demo.todolist.entity.Task;
import demo.todolist.entity.User;
import demo.todolist.entity.UserRole;
import demo.todolist.repository.TaskRepository;
import demo.todolist.web.dto.TaskRequest;
import demo.todolist.web.dto.TaskResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private TaskService taskService;


    @Test
    void createTask_Success() {
        // given
        User owner = buildUser(UserRole.USER);
        when(userService.getCurrentUser()).thenReturn(owner);

        TaskRequest request = new TaskRequest("Title", "Desc", true);

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            t.setId(UUID.randomUUID());
            return t;
        });

        // when
        TaskResponse response = taskService.createTask(request);

        // then
        assertNotNull(response);
        assertEquals(request.title(), response.title());
        verify(taskRepository).save(any(Task.class));
    }


    @Test
    void getTasksForCurrentUser_ReturnsOwnTasks() {
        // given
        User owner = buildUser(UserRole.USER);
        Task task1 = buildTask(owner, "Task1");
        Task task2 = buildTask(owner, "Task2");
        owner.getTasks().addAll(List.of(task1, task2));
        when(userService.getCurrentUser()).thenReturn(owner);

        // when
        List<TaskResponse> responses = taskService.getTasksForCurrentUser();

        // then
        assertEquals(2, responses.size());
    }


    @Test
    void getTaskById_AccessDenied() {
        // given
        User otherUser = buildUser(UserRole.USER);
        User currentUser = buildUser(UserRole.USER);

        Task task = buildTask(otherUser, "OtherTask");
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(userService.getCurrentUser()).thenReturn(currentUser);

        // then
        assertThrows(AccessDeniedException.class, () -> taskService.getTaskById(task.getId()));
    }


    @Test
    void updateTask_Success() {
        // given
        User owner = buildUser(UserRole.USER);
        Task task = buildTask(owner, "OldTitle");
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);
        when(userService.getCurrentUser()).thenReturn(owner);

        TaskRequest request = new TaskRequest("NewTitle", "NewDesc", false);

        // when
        TaskResponse response = taskService.updateTask(task.getId(), request);

        // then
        assertEquals("NewTitle", response.title());
        verify(taskRepository).save(task);
    }


    @Test
    void deleteTask_Success() {
        // given
        User owner = buildUser(UserRole.USER);
        Task task = buildTask(owner, "TaskToDelete");
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(userService.getCurrentUser()).thenReturn(owner);

        // when
        taskService.deleteTask(task.getId());

        // then
        verify(taskRepository).delete(task);
    }


    private User buildUser(UserRole role) {
        User u = new User();
        u.setId(UUID.randomUUID());
        u.setUsername("user" + u.getId().toString().substring(0, 5));
        u.setRole(role);
        return u;
    }

    private Task buildTask(User owner, String title) {
        Task t = new Task();
        t.setId(UUID.randomUUID());
        t.setTitle(title);
        t.setDescription(title + " desc");
        t.setActive(true);
        t.setOwner(owner);
        return t;
    }
}
