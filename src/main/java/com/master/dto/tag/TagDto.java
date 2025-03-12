package com.master.dto.tag;

import com.master.dto.ABasicAdminDto;
import lombok.Data;

@Data
public class TagDto extends ABasicAdminDto {
    private String name;
    private String color;
}