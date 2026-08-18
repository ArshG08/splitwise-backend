package com.arsh.splitwise.groups.controller;

import com.arsh.splitwise.groups.dto.AddMemberRequest;
import com.arsh.splitwise.groups.dto.CreateGroupRequest;
import com.arsh.splitwise.groups.dto.GroupMemberResponse;
import com.arsh.splitwise.groups.dto.GroupResponse;
import com.arsh.splitwise.groups.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    @PostMapping
    public GroupResponse createGroup(
            @RequestBody CreateGroupRequest request){

        return groupService.createGroup(request);
    }
    @GetMapping
    public List<GroupResponse> getMyGroups() {
        return groupService.getMyGroups();
    }
    @PostMapping("/{groupId}/members")
    public void addMember(
            @PathVariable Long groupId,
            @Valid @RequestBody AddMemberRequest request) {

        groupService.addMember(groupId, request);
    }
    @GetMapping("/{groupId}/members")
    public List<GroupMemberResponse> getGroupMembers(
            @PathVariable Long groupId) {

        return groupService.getGroupMembers(groupId);
    }
    @DeleteMapping("/{groupId}/members/{userId}")
    public void removeMember(
            @PathVariable Long groupId,
            @PathVariable Long userId) {

        groupService.removeMember(groupId, userId);
    }
    @DeleteMapping("/{groupId}/leave")
    public void leaveGroup(
            @PathVariable Long groupId) {

        groupService.leaveGroup(groupId);
    }
}