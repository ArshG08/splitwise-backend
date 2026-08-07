package com.arsh.splitwise.groups.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupResponse {

    private int id;
    private String name ;
    private String description;
}
