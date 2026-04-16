package com.vacation.Vacation_Planner_Backend.repository;

import com.vacation.Vacation_Planner_Backend.model.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {
}
