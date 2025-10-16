package com.bankcards.dto;

import com.bankcards.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateUserDto {
    @Size(min = 2, max = 250, message = "name length must be between 2 and 250 characters")
    String firstName;

    @Size(min = 2, max = 250, message = "name length must be between 2 and 250 characters")
    String lastName;

    @Email(message = "Электронная почта не может быть пустой и должна содержать символ @")
    @Size(min = 6, max = 254, message = "Email length must be between 2 and 250 characters")
    String email;

    Set<Role> roles;
}
