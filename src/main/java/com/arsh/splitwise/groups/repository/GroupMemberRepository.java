package com.arsh.splitwise.groups.repository;

import com.arsh.splitwise.groups.entity.Group;
import com.arsh.splitwise.groups.entity.GroupMember;
import com.arsh.splitwise.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository
        extends JpaRepository<GroupMember, Long> {

    List<GroupMember> findByUser(User user);

    List<GroupMember> findByGroup(Group group);
    Optional<GroupMember> findByGroupAndUser(Group group, User user);
    boolean existsByGroupAndUser(Group group, User user);
}