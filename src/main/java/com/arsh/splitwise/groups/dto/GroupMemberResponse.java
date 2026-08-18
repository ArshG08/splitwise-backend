package com.arsh.splitwise.groups.dto;

import com.arsh.splitwise.groups.enums.GroupMemberStatus;
import com.arsh.splitwise.groups.enums.GroupRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupMemberResponse {

    private Long userId;
    private String name;
    private String email;
    private GroupRole role;
    private GroupMemberStatus status;
}