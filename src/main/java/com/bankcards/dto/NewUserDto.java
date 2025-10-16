package com.bankcards.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewUserDto {
    @NotBlank(message = "Field: name. Error: must not be blank. Value: null")
    @Size(min = 2, max = 250, message = "name length must be between 2 and 250 characters")
    String firstName;

    @NotBlank(message = "Field: name. Error: must not be blank. Value: null")
    @Size(min = 2, max = 250, message = "name length must be between 2 and 250 characters")
    String lastName;

    @Email(message = "Электронная почта не может быть пустой и должна содержать символ @")
    @Size(min = 6, max = 254, message = "Email length must be between 2 and 250 characters")
    @NotNull(message = "Email не должен быть null")
    @NotBlank
    String email;

    @NotNull(message = "Пароль не должен быть null")
    @NotBlank
    String password;
}
