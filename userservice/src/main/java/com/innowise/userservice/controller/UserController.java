package com.innowise.userservice.controller;

import com.innowise.userservice.details.UserDetailsImpl;
import com.innowise.userservice.dto.user.CreateUserDto;
import com.innowise.userservice.dto.user.ResponseUserDto;
import com.innowise.userservice.dto.user.UpdateUserDto;
import com.innowise.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<ResponseUserDto>> getUsers(@PageableDefault Pageable pageable,
                                                          @RequestParam(required = false) String name,
                                                          @RequestParam(required = false) String surname) {
        return ResponseEntity.ok(userService.getUsers(pageable, name, surname));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE')")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseUserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/me")
    public ResponseEntity<ResponseUserDto> getSelfById(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return new ResponseEntity<>(userService.getUserById(userDetails.getUserId()), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ResponseUserDto> createUser(@RequestBody @Valid CreateUserDto userDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUser(userDto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable Long id) {
        userService.activateUser(id);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ResponseUserDto> updateUser(@PathVariable Long id,
                                                      @RequestBody @Valid UpdateUserDto userDto) {
        return ResponseEntity.ok(userService.updateUser(id, userDto));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/search")
    public ResponseEntity<ResponseUserDto> findByEmail(@RequestParam String email) {
        return ResponseEntity.ok(userService.findByEmail(email));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/batch")
    public ResponseEntity<List<ResponseUserDto>> findAllUsersByIds(@RequestBody List<Long> ids) {
        return ResponseEntity.ok(userService.findAllUsersByIds(ids));
    }

    @PreAuthorize("hasRole('SERVICE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

}
