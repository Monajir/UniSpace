package com.techManiacs.UniSpace.utils;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.techManiacs.UniSpace.domain.Role;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequest {

    private Role requestedRole;
    private String reason;
}
