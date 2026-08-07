package com.arsh.splitwise.groups.repository;

import com.arsh.splitwise.groups.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMemberRepository
        extends JpaRepository<GroupMember, Long> {
}