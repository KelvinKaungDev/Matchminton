package com.badminton_manager.badminton.dto.user;

import com.badminton_manager.badminton.enums.Provider;
import com.badminton_manager.badminton.enums.Tier;
import lombok.Data;

@Data
public class UserRequestDTO {
    private String name;
    private String email;
    private String avatar;
    private Provider provider;
    private Tier tier;
}
