package com.learnplatform.web;

import com.learnplatform.entity.User;
import com.learnplatform.service.UserService;
import com.learnplatform.service.dto.UserCreateCommand;
import com.learnplatform.service.dto.UserUpdateCommand;
import com.learnplatform.web.dto.UserCreateRequest;
import com.learnplatform.web.dto.UserPatchRequest;
import com.learnplatform.web.dto.UserView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserView> create(@Valid @RequestBody UserCreateRequest req) {
        User created = userService.create(new UserCreateCommand(req.name(), req.email(), req.role()));
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(toView(created));
    }

    @GetMapping
    public List<UserView> list() {
        return userService.list().stream().map(UserController::toView).toList();
    }

    @GetMapping("/{id}")
    public UserView get(@PathVariable @Positive Long id) {
        return toView(userService.getOrThrow(id));
    }

    @PatchMapping("/{id}")
    public UserView patch(@PathVariable @Positive Long id, @Valid @RequestBody UserPatchRequest req) {
        User updated = userService.update(id, new UserUpdateCommand(req.name(), req.email(), req.role()));
        return toView(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private static UserView toView(User u) {
        return new UserView(u.getId(), u.getName(), u.getEmail(), u.getRole());
    }
}
