package demo.todolist.service;

import demo.todolist.entity.Task;
import demo.todolist.entity.User;
import demo.todolist.repository.TaskRepository;
import demo.todolist.utils.DtoMapper;
import demo.todolist.web.dto.TaskRequest;
import demo.todolist.web.dto.TaskResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;

    @Transactional
    public TaskResponse createTask(TaskRequest request) {
        User owner = userService.getCurrentUser();
        Task task = DtoMapper.toTaskEntity(request, owner);
        task.setActive(true);
        Task savedTask = taskRepository.save(task);
        return DtoMapper.toTaskResponse(savedTask);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksForCurrentUser() {
        User owner = userService.getCurrentUser();
        return owner.getTasks()
                .stream()
                .map(DtoMapper::toTaskResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(UUID id) {
        Task task = getTaskWithAccessCheck(id);
        return DtoMapper.toTaskResponse(task);
    }

    @Transactional
    public TaskResponse updateTask(UUID id, TaskRequest request) {
        Task task = getTaskWithAccessCheck(id);

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setActive(request.isActive());

        Task updatedTask = taskRepository.save(task);
        return DtoMapper.toTaskResponse(updatedTask);
    }

    @Transactional
    public void deleteTask(UUID id) {
        Task task = getTaskWithAccessCheck(id);
        taskRepository.delete(task);
    }

    @Transactional
    public TaskResponse toggleTaskCompletion(UUID id) {
        Task task = getTaskWithAccessCheck(id);

        boolean wasActive = task.isActive();

        task.setActive(!wasActive);


        if (wasActive && !task.isActive() && !task.isCountedAsDone()) {
            User owner = task.getOwner();
            owner.setTasksDone(owner.getTasksDone() + 1);
            task.setCountedAsDone(true);
             userService.saveUser(owner);
        }

        Task savedTask = taskRepository.save(task);
        return DtoMapper.toTaskResponse(savedTask);
    }


    private Task getTaskWithAccessCheck(UUID id) {
        User currentUser = userService.getCurrentUser();
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task with id: " + id + " does not exist!"));

        if (currentUser.getId().equals(task.getOwner().getId()) ||
                currentUser.getRole().name().equals("ADMIN")) {
            return task;
        }

        throw new AccessDeniedException("You are not allowed to access this task");
    }


}