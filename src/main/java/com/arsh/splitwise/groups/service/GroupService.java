package com.arsh.splitwise.groups.service;
import com.arsh.splitwise.groups.dto.AddMemberRequest;

import com.arsh.splitwise.auth.security.CustomUserDetails;
import com.arsh.splitwise.groups.dto.CreateGroupRequest;
import com.arsh.splitwise.groups.dto.GroupMemberResponse;
import com.arsh.splitwise.groups.dto.GroupResponse;
import com.arsh.splitwise.groups.entity.Group;
import com.arsh.splitwise.groups.entity.GroupMember;
import com.arsh.splitwise.groups.enums.GroupMemberStatus;
import com.arsh.splitwise.groups.enums.GroupRole;
import com.arsh.splitwise.groups.repository.GroupMemberRepository;
import com.arsh.splitwise.groups.repository.GroupRepository;
import com.arsh.splitwise.user.entity.User;
import com.arsh.splitwise.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public GroupResponse createGroup(CreateGroupRequest request) {

        // Step 1: Get the currently logged-in user
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        User currentUser = userDetails.getUser();

        // Step 2: Create Group
        Group group = Group.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(currentUser)
                .build();


        // Step 3: Save Group
        Group savedGroup = groupRepository.save(group);

        // Step 4: Create OWNER membership
        GroupMember ownerMembership = GroupMember.builder()
                .group(savedGroup)
                .user(currentUser)
                .role(GroupRole.OWNER)
                .status(GroupMemberStatus.ACTIVE)
                .build();

        // Step 5: Save membership
        groupMemberRepository.save(ownerMembership);

        // Step 6: Return Response
        return GroupResponse.builder()
                .id(savedGroup.getId())
                .name(savedGroup.getName())
                .description(savedGroup.getDescription())
                .build();
    }
    public List<GroupResponse> getMyGroups() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        User currentUser = userDetails.getUser();

        List<GroupMember> memberships =
                groupMemberRepository.findByUser(currentUser);

        return memberships.stream()
                .map(GroupMember::getGroup)
                .map(group -> GroupResponse.builder()
                        .id(group.getId())
                        .name(group.getName())
                        .description(group.getDescription())
                        .build())
                .toList();
    }
    @Transactional
    public void addMember(Long groupId, AddMemberRequest request) {

        // 1. Get logged-in user
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        User currentUser = userDetails.getUser();

        // 2. Find the group
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() ->
                        new RuntimeException("Group not found"));

        // 3. Check whether current user is the owner
        if (!group.getOwner().getId().equals(currentUser.getId())) {
            throw new RuntimeException(
                    "Only the group owner can add members"
            );
        }

        // 4. Find the user being added
        User userToAdd = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // 5. Check if already a member
        if (groupMemberRepository.existsByGroupAndUser(group, userToAdd)) {
            throw new RuntimeException(
                    "User is already a member of this group"
            );
        }

        // 6. Create membership
        GroupMember membership = GroupMember.builder()
                .group(group)
                .user(userToAdd)
                .role(GroupRole.MEMBER)
                .status(GroupMemberStatus.ACTIVE)
                .build();

        // 7. Save membership
        groupMemberRepository.save(membership);
    }
    @Transactional(readOnly = true)
    public List<GroupMemberResponse> getGroupMembers(Long groupId) {

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        List<GroupMember> members =
                groupMemberRepository.findByGroup(group);

        return members.stream()
                .map(member -> GroupMemberResponse.builder()
                        .userId(member.getUser().getId())
                        .name(member.getUser().getName())
                        .email(member.getUser().getEmail())
                        .role(member.getRole())
                        .status(member.getStatus())
                        .build())
                .toList();
    }
    @Transactional
    public void removeMember(Long groupId, Long userId) {

        // 1. Get logged-in user
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        User currentUser = userDetails.getUser();

        // 2. Find the group
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() ->
                        new RuntimeException("Group not found"));

        // 3. Check if current user is the owner
        if (!group.getOwner().getId().equals(currentUser.getId())) {
            throw new RuntimeException(
                    "Only the group owner can remove members"
            );
        }

        // 4. Find the member
        User userToRemove = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // 5. Owner cannot remove themselves
        if (userToRemove.getId().equals(group.getOwner().getId())) {
            throw new RuntimeException(
                    "Owner cannot be removed from the group"
            );
        }

        // 6. Find membership
        GroupMember membership =
                groupMemberRepository.findByGroupAndUser(
                        group,
                        userToRemove
                ).orElseThrow(() ->
                        new RuntimeException(
                                "User is not a member of this group"
                        ));

        // 7. Soft delete
        membership.setStatus(GroupMemberStatus.REMOVED);

        // 8. Save
        groupMemberRepository.save(membership);
    }
    @Transactional
    public void leaveGroup(Long groupId) {

        // 1. Get logged-in user
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        User currentUser = userDetails.getUser();

        // 2. Find the group
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() ->
                        new RuntimeException("Group not found"));

        // 3. Owner cannot leave
        if (group.getOwner().getId().equals(currentUser.getId())) {
            throw new RuntimeException(
                    "Owner cannot leave the group"
            );
        }

        // 4. Find current user's membership
        GroupMember membership =
                groupMemberRepository.findByGroupAndUser(
                        group,
                        currentUser
                ).orElseThrow(() ->
                        new RuntimeException(
                                "You are not a member of this group"
                        ));

        // 5. Soft delete by marking as LEFT
        membership.setStatus(GroupMemberStatus.LEFT);

        // 6. Save
        groupMemberRepository.save(membership);
    }
}