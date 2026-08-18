package com.arsh.splitwise.groups.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupResponse {

    private long id;
    private String name ;
    private String description;
}
