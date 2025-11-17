package com.example.demo.service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Task;
import com.example.demo.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Tests")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private Task testTask;

    @BeforeEach
    void setUp() {
        testTask = new Task();
        testTask.setId("1");
        testTask.setName("Test Task");
        testTask.setDescription("Test Description");
        testTask.setStatus("in-progress");
        testTask.setAssigneeId("user1");
        testTask.setProjectId("project1");
        testTask.setConventionId("conv1");
        testTask.setCreatedAt(LocalDateTime.now());
        testTask.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should create task successfully")
    void testCreateTask() {
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        Task result = taskService.createTask(testTask);

        assertNotNull(result);
        assertEquals("Test Task", result.getName());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("Should get all tasks successfully")
    void testGetAllTasks() {
        Task task2 = new Task();
        task2.setId("2");
        task2.setName("Task 2");
        
        List<Task> tasks = Arrays.asList(testTask, task2);
        when(taskRepository.findAll()).thenReturn(tasks);

        List<Task> result = taskService.getAllTasks();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(taskRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should get task by ID successfully")
    void testGetTaskById() {
        when(taskRepository.findById("1")).thenReturn(Optional.of(testTask));

        Task result = taskService.getTaskById("1");

        assertNotNull(result);
        assertEquals("1", result.getId());
        assertEquals("Test Task", result.getName());
        verify(taskRepository, times(1)).findById("1");
    }

    @Test
    @DisplayName("Should throw exception when task not found")
    void testGetTaskByIdNotFound() {
        when(taskRepository.findById("999")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.getTaskById("999"));
        verify(taskRepository, times(1)).findById("999");
    }

    @Test
    @DisplayName("Should get tasks by assignee")
    void testGetTasksByAssignee() {
        List<Task> tasks = Arrays.asList(testTask);
        when(taskRepository.findByAssigneeId("user1")).thenReturn(tasks);

        List<Task> result = taskService.getTasksByAssignee("user1");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("user1", result.get(0).getAssigneeId());
        verify(taskRepository, times(1)).findByAssigneeId("user1");
    }

    @Test
    @DisplayName("Should get tasks by project")
    void testGetTasksByProject() {
        List<Task> tasks = Arrays.asList(testTask);
        when(taskRepository.findByProjectId("project1")).thenReturn(tasks);

        List<Task> result = taskService.getTasksByProject("project1");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("project1", result.get(0).getProjectId());
        verify(taskRepository, times(1)).findByProjectId("project1");
    }

    @Test
    @DisplayName("Should get tasks by convention")
    void testGetTasksByConvention() {
        List<Task> tasks = Arrays.asList(testTask);
        when(taskRepository.findByConventionId("conv1")).thenReturn(tasks);

        List<Task> result = taskService.getTasksByConvention("conv1");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("conv1", result.get(0).getConventionId());
        verify(taskRepository, times(1)).findByConventionId("conv1");
    }

    @Test
    @DisplayName("Should get tasks by status")
    void testGetTasksByStatus() {
        List<Task> tasks = Arrays.asList(testTask);
        when(taskRepository.findByStatus("in-progress")).thenReturn(tasks);

        List<Task> result = taskService.getTasksByStatus("in-progress");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("in-progress", result.get(0).getStatus());
        verify(taskRepository, times(1)).findByStatus("in-progress");
    }

    @Test
    @DisplayName("Should get delayed tasks")
    void testGetDelayedTasks() {
        List<Task> tasks = Arrays.asList(testTask);
        when(taskRepository.findDelayedTasks(any(LocalDateTime.class))).thenReturn(tasks);

        List<Task> result = taskService.getDelayedTasks();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(taskRepository, times(1)).findDelayedTasks(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should get active tasks")
    void testGetActiveTasks() {
        List<Task> tasks = Arrays.asList(testTask);
        when(taskRepository.findByStatusIn(anyList())).thenReturn(tasks);

        List<Task> result = taskService.getActiveTasks();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(taskRepository, times(1)).findByStatusIn(anyList());
    }

    @Test
    @DisplayName("Should get tasks between dates")
    void testGetTasksBetweenDates() {
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();
        List<Task> tasks = Arrays.asList(testTask);
        
        when(taskRepository.findTasksBetweenDates(start, end)).thenReturn(tasks);

        List<Task> result = taskService.getTasksBetweenDates(start, end);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(taskRepository, times(1)).findTasksBetweenDates(start, end);
    }
}
