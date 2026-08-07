package com.arsh.splitwise.groups.repository;

import com.arsh.splitwise.groups.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {
}