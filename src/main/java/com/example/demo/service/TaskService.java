package com.example.demo.service;

import com.example.demo.model.Task;
import com.example.demo.repository.TaskRepository;
import com.example.demo.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {
    
    @Autowired
    private TaskRepository taskRepository;
    
    /**
     * Créer une nouvelle tâche
     */
    public Task createTask(Task task) {
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }
    
    /**
     * Récupérer toutes les tâches
     */
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }
    
    /**
     * Récupérer une tâche par ID
     */
    public Task getTaskById(String id) {
        return taskRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Tâche non trouvée avec l'ID: " + id));
    }
    
    /**
     * Récupérer les tâches d'un utilisateur
     */
    public List<Task> getTasksByAssignee(String assigneeId) {
        return taskRepository.findByAssigneeId(assigneeId);
    }
    
    /**
     * Récupérer les tâches d'un projet
     */
    public List<Task> getTasksByProject(String projectId) {
        return taskRepository.findByProjectId(projectId);
    }
    
    /**
     * Récupérer les tâches d'une convention
     */
    public List<Task> getTasksByConvention(String conventionId) {
        return taskRepository.findByConventionId(conventionId);
    }
    
    /**
     * Récupérer les tâches par statut
     */
    public List<Task> getTasksByStatus(String status) {
        return taskRepository.findByStatus(status);
    }
    
    /**
     * Récupérer les tâches en retard
     */
    public List<Task> getDelayedTasks() {
        return taskRepository.findDelayedTasks(LocalDateTime.now());
    }
    
    /**
     * Récupérer les tâches actives (en cours ou en retard)
     */
    public List<Task> getActiveTasks() {
        return taskRepository.findByStatusIn(List.of("in-progress", "delayed"));
    }
    
    /**
     * Récupérer les tâches entre deux dates
     */
    public List<Task> getTasksBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return taskRepository.findTasksBetweenDates(startDate, endDate);
    }
    
    /**
     * Mettre à jour une tâche
     */
    public Task updateTask(String id, Task taskDetails) {
        Task task = getTaskById(id);
        
        if (taskDetails.getName() != null) {
            task.setName(taskDetails.getName());
        }
        if (taskDetails.getDescription() != null) {
            task.setDescription(taskDetails.getDescription());
        }
        if (taskDetails.getStartDate() != null) {
            task.setStartDate(taskDetails.getStartDate());
        }
        if (taskDetails.getEndDate() != null) {
            task.setEndDate(taskDetails.getEndDate());
        }
        if (taskDetails.getProgress() != null) {
            task.setProgress(taskDetails.getProgress());
        }
        if (taskDetails.getStatus() != null) {
            task.setStatus(taskDetails.getStatus());
        }
        if (taskDetails.getAssignee() != null) {
            task.setAssignee(taskDetails.getAssignee());
        }
        if (taskDetails.getAssigneeId() != null) {
            task.setAssigneeId(taskDetails.getAssigneeId());
        }
        if (taskDetails.getPriority() != null) {
            task.setPriority(taskDetails.getPriority());
        }
        if (taskDetails.getColor() != null) {
            task.setColor(taskDetails.getColor());
        }
        if (taskDetails.getDependencies() != null) {
            task.setDependencies(taskDetails.getDependencies());
        }
        
        task.setUpdatedAt(LocalDateTime.now());
        task.setUpdatedBy(taskDetails.getUpdatedBy());
        
        return taskRepository.save(task);
    }
    
    /**
     * Mettre à jour la progression d'une tâche
     */
    public Task updateTaskProgress(String id, Integer progress) {
        Task task = getTaskById(id);
        task.setProgress(progress);
        task.setUpdatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }
    
    /**
     * Supprimer une tâche
     */
    public void deleteTask(String id) {
        Task task = getTaskById(id);
        taskRepository.delete(task);
    }
    
    /**
     * Obtenir les statistiques des tâches
     */
    public TaskStatistics getTaskStatistics() {
        List<Task> allTasks = taskRepository.findAll();
        
        long totalTasks = allTasks.size();
        long completedTasks = taskRepository.countByStatus("completed");
        long inProgressTasks = taskRepository.countByStatus("in-progress");
        long notStartedTasks = taskRepository.countByStatus("not-started");
        long delayedTasks = getDelayedTasks().size();
        
        double completionRate = totalTasks > 0 ? (completedTasks * 100.0 / totalTasks) : 0;
        
        return new TaskStatistics(
            totalTasks,
            completedTasks,
            inProgressTasks,
            notStartedTasks,
            delayedTasks,
            completionRate
        );
    }
    
    /**
     * Obtenir les statistiques d'un utilisateur
     */
    public TaskStatistics getUserTaskStatistics(String assigneeId) {
        List<Task> userTasks = taskRepository.findByAssigneeId(assigneeId);
        
        long totalTasks = userTasks.size();
        long completedTasks = userTasks.stream().filter(t -> "completed".equals(t.getStatus())).count();
        long inProgressTasks = userTasks.stream().filter(t -> "in-progress".equals(t.getStatus())).count();
        long notStartedTasks = userTasks.stream().filter(t -> "not-started".equals(t.getStatus())).count();
        long delayedTasks = userTasks.stream().filter(Task::isDelayed).count();
        
        double completionRate = totalTasks > 0 ? (completedTasks * 100.0 / totalTasks) : 0;
        
        return new TaskStatistics(
            totalTasks,
            completedTasks,
            inProgressTasks,
            notStartedTasks,
            delayedTasks,
            completionRate
        );
    }
    
    /**
     * Classe interne pour les statistiques
     */
    public static class TaskStatistics {
        private long totalTasks;
        private long completedTasks;
        private long inProgressTasks;
        private long notStartedTasks;
        private long delayedTasks;
        private double completionRate;
        
        public TaskStatistics(long totalTasks, long completedTasks, long inProgressTasks,
                            long notStartedTasks, long delayedTasks, double completionRate) {
            this.totalTasks = totalTasks;
            this.completedTasks = completedTasks;
            this.inProgressTasks = inProgressTasks;
            this.notStartedTasks = notStartedTasks;
            this.delayedTasks = delayedTasks;
            this.completionRate = completionRate;
        }
        
        // Getters
        public long getTotalTasks() { return totalTasks; }
        public long getCompletedTasks() { return completedTasks; }
        public long getInProgressTasks() { return inProgressTasks; }
        public long getNotStartedTasks() { return notStartedTasks; }
        public long getDelayedTasks() { return delayedTasks; }
        public double getCompletionRate() { return completionRate; }
    }
}
