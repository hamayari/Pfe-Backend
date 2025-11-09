package com.example.demo.repository;

import com.example.demo.model.Task;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends MongoRepository<Task, String> {
    
    // Recherche par assigné
    List<Task> findByAssigneeId(String assigneeId);
    
    // Recherche par projet
    List<Task> findByProjectId(String projectId);
    
    // Recherche par convention
    List<Task> findByConventionId(String conventionId);
    
    // Recherche par statut
    List<Task> findByStatus(String status);
    
    // Recherche par priorité
    List<Task> findByPriority(String priority);
    
    // Recherche des tâches en retard
    @Query("{ 'endDate': { $lt: ?0 }, 'progress': { $lt: 100 } }")
    List<Task> findDelayedTasks(LocalDateTime currentDate);
    
    // Recherche des tâches actives (en cours)
    List<Task> findByStatusIn(List<String> statuses);
    
    // Recherche des tâches entre deux dates
    @Query("{ 'startDate': { $gte: ?0, $lte: ?1 } }")
    List<Task> findTasksBetweenDates(LocalDateTime startDate, LocalDateTime endDate);
    
    // Recherche des tâches d'un utilisateur avec un statut spécifique
    List<Task> findByAssigneeIdAndStatus(String assigneeId, String status);
    
    // Recherche des tâches d'un projet avec un statut spécifique
    List<Task> findByProjectIdAndStatus(String projectId, String status);
    
    // Compter les tâches par statut
    long countByStatus(String status);
    
    // Compter les tâches d'un utilisateur
    long countByAssigneeId(String assigneeId);
}
