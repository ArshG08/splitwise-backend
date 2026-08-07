package com.arsh.splitwise.groups.entity;

import com.arsh.splitwise.common.audit.BaseEntity;
import com.arsh.splitwise.groups.enums.GroupMemberStatus;
import com.arsh.splitwise.groups.enums.GroupRole;
import com.arsh.splitwise.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "group_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupMemberStatus status;
}