package com.example.demo.repository;

import com.example.demo.model.InternalComment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InternalCommentRepository extends MongoRepository<InternalComment, String> {
    List<InternalComment> findByCreatedByOrderByDateDesc(String createdBy);
    List<InternalComment> findByMentionedCommercialIdOrderByDateDesc(String commercialId);
    List<InternalComment> findAllByOrderByDateDesc();
    List<InternalComment> findByDateBetweenOrderByDateDesc(LocalDateTime start, LocalDateTime end);
}
