package com.henrique.virtualteacher.models;

import com.henrique.virtualteacher.entities.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
public class LoginModel {

    @Size(min = 2, max = 40, message = UserUpdateModel.EMAIL_INVALID_SIZE_MSG)
    @Email
    private String email;

    @Size(min = 2, max = 15, message = UserUpdateModel.PASSWORD_INVALID_SIZE_MSG)
    private String password;

}
