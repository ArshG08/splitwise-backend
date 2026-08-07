package com.arsh.splitwise.groups.service;

import com.arsh.splitwise.auth.security.CustomUserDetails;
import com.arsh.splitwise.groups.dto.CreateGroupRequest;
import com.arsh.splitwise.groups.dto.GroupResponse;
import com.arsh.splitwise.groups.repository.GroupMemberRepository;
import com.arsh.splitwise.groups.repository.GroupRepository;
import com.arsh.splitwise.user.entity.User;
import com.arsh.splitwise.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public GroupResponse createGroup(CreateGroupRequest request) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();
        User currentUser = userDetails.getUser();
    }

}