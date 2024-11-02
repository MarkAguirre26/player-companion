package com.virtual.app.sicbo.module.services;

import com.virtual.app.sicbo.module.data.User;

import java.util.List;

public interface UserService {

    User findUserByUsername(String username);

    User findUserByEmail(String email);

    User findByUsernameAndIsActive(String username, int isActive);

    User findByEmailAndIsActiveAndLogged(String email, int isActive, int logged);

    User createUser(User user);

    User updateUser(User user);

    void deleteUser(Integer userId);

    User getUserById(Integer userId);

    List<User> getAllUsers();
}
