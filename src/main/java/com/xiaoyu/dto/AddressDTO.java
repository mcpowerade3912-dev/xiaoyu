package com.xiaoyu.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class AddressDTO {
    // 编辑时传入，新增时不传
    private Long id;

    @NotBlank(message = "收件人姓名不能为空")
    private String name;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "详细地址不能为空")
    private String address;

    // 是否设为默认地址：0否 1是
    private Integer isDefault = 0;
}