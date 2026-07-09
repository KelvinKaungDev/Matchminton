package com.badminton_manager.badminton.dto.rotation;

import com.badminton_manager.badminton.enums.RotationPlayerStatus;
import lombok.Data;

@Data
public class RotationPlayerStatusUpdateDTO {
    private RotationPlayerStatus status;
}
