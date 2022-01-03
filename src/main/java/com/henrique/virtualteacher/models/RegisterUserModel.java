package com.henrique.virtualteacher.models;

import com.henrique.virtualteacher.entities.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
public class RegisterUserModel extends LoginModel {

    @Size(min = 2, max = 15, message = UserUpdateModel.FIRSTNAME_INVALID_SIZE_MSG)
    private String firstName;

    @Size(min = 2, max = 15, message = UserUpdateModel.LASTNAME_INVALID_SIZE_MSG)
    private String lastName;

    @Size(min = 5, max = 25, message = UserUpdateModel.PASSWORD_CONFIRM_INVALID_SIZE_MSG)
    private String passwordConfirm;


}
