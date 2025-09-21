package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {
    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> createUser(@RequestBody @Valid NewUserRequest request) {
        log.info("Gateway: createUser request={}", request);
        return userClient.createUser(request);
    }

    @GetMapping
    public ResponseEntity<Object> getUsers() {
        log.info("Gateway: getUsers");
        return userClient.getUsers();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUserById(@Positive @PathVariable("userId") long userId) {
        log.info("Gateway: getUserById userId={}", userId);
        return userClient.getUser(userId);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> updateUser(@Positive @PathVariable("userId") long userId,
                                             @RequestBody UpdateUserRequest request) {
        log.info("Gateway: updateUser userId={}, request={}", userId, request);
        return userClient.updateUser(userId, request);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteUser(@Positive @PathVariable("userId") long userId) {
        log.info("Gateway: deleteUser userId={}", userId);
        return userClient.deleteUser(userId);
    }
}
