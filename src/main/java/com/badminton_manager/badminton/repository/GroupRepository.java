package com.badminton_manager.badminton.repository;

import com.badminton_manager.badminton.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GroupRepository extends JpaRepository<Group, UUID> {
    List<Group> findByOrganizerId(UUID organizerId);
    List<Group> findByIsActiveTrue();
}
