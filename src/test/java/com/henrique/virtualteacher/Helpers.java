package com.henrique.virtualteacher;

import com.henrique.virtualteacher.entities.Role;
import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.models.EnumRoles;
import com.henrique.virtualteacher.models.RegisterUserModel;

import java.util.List;

public class Helpers {

    public static User createMockUser() {
        return createUser();
    }

    public static User createMockAdmin() {
        return createAdmin();
    }

    public static User createMockTeacher() {
        return createTeacher();
    }

    public static RegisterUserModel createUserRegisterModel() {
        return createRegisterModel();
    }

    private static RegisterUserModel createRegisterModel() {
    RegisterUserModel registerModel = new RegisterUserModel();
    registerModel.setFirstName("test");
    registerModel.setLastName("test");
    registerModel.setPassword("Test123@");
    registerModel.setPasswordConfirm("Test123@");
    registerModel.setEmail("test123@gmai.com");
    return registerModel;
    }

    private static User createTeacher() {
        User teacher = new User();
        addBasicInfo(teacher);
        teacher.setRoles(List.of(new Role(2, EnumRoles.TEACHER), new Role(1, EnumRoles.STUDENT)));

        return teacher;
    }

    private static User createAdmin() {
        User admin = new User();
        addBasicInfo(admin);
        admin.setRoles(List.of(new Role(2,EnumRoles.TEACHER), new Role(3, EnumRoles.ADMIN), new Role(1, EnumRoles.STUDENT)));

        return admin;
    }

    private static User createUser() {
        User user = new User();
        addBasicInfo(user);
        user.setRoles(List.of(new Role(1,EnumRoles.STUDENT)));

        return user;
    }

    private static void addBasicInfo(User user) {
        user.setId(1);
        user.setFirstName("test");
        user.setLastName("test");
        user.setPassword("123123@El");
        user.setEmail("test@gmail.com");
        user.setEnabled(true);
    }



}
