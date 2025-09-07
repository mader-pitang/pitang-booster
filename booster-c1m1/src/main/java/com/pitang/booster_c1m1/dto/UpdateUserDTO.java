package com.pitang.booster_c1m1.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserDTO {

  @NotBlank(message = "Name is mandatory")
  private String name;

  @Email(message = "Email should be valid")
  private String email;

  @NotBlank(message = "Password is mandatory")
  private String password;

}
