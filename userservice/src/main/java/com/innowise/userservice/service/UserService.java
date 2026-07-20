package com.innowise.userservice.service;

import com.innowise.userservice.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserDTO createUser(UserDTO userDTO);

    UserDTO getUserById(Long id);

    Page<UserDTO> getUsers(Pageable pageable, String name, String surname);

    void activateUser();

    void deactivateUser();

    UserDTO updateUser(UserDTO userDTO);
    
}
