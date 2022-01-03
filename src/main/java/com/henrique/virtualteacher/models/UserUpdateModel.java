package com.henrique.virtualteacher.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Constraint;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
public class UserUpdateModel extends MessageBuilder {

    public static final String FIRSTNAME_INVALID_SIZE_MSG = "First name length must be between 2 and 20 characters";
    public static final String LASTNAME_INVALID_SIZE_MSG = "Last name length must be between 2 and 20 characters";
    public static final String INVALID_EMAIL_MSG = "Email is not formed correctly";
    public static final String EMAIL_INVALID_SIZE_MSG = "Email length should be between 6 and 40 characters";
    public static final String PASSWORD_INVALID_SIZE_MSG = "Password must be between 5 and 25 characters";
    public static final String PASSWORD_CONFIRM_INVALID_SIZE_MSG = "Password confirm must be between 5 and 25 characters";
    public static final String INVALID_PASSWORD_MSG = "Password must contain at least, 1 digit, 1 upper and lower case letter and a special character";

    @NotBlank
    @Size(min = 2, max = 20, message = FIRSTNAME_INVALID_SIZE_MSG )
    private String firstname;

    @NotBlank
    @Size(min = 2, max = 20, message = LASTNAME_INVALID_SIZE_MSG)
    private String lastname;

    @NotBlank
    @Email(message = INVALID_EMAIL_MSG)
    @Size(min = 6, max = 40, message = EMAIL_INVALID_SIZE_MSG)
    private String email;

    @NotBlank
    @Size(min = 5, max = 25, message =  PASSWORD_INVALID_SIZE_MSG)
    @Pattern(regexp = "^?:(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).*$", message = INVALID_PASSWORD_MSG)
    private String password;

    @NotBlank
    @Size(min = 5, max = 25, message = PASSWORD_CONFIRM_INVALID_SIZE_MSG)
    @Pattern(regexp = "^?:(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).*$", message = INVALID_PASSWORD_MSG)
    private String passwordConfirm;



}
