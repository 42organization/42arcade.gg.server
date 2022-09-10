package io.pp.arcade.domain.admin.controller;

import io.pp.arcade.domain.admin.dto.create.UserCreateRequestDto;
import io.pp.arcade.domain.admin.dto.update.UserUpdateRequestDto;
import io.pp.arcade.domain.user.UserService;
import io.pp.arcade.domain.user.dto.UserDto;
import io.pp.arcade.domain.user.dto.UserFindDto;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/admin")
public class UserAdminControllerImpl implements UserAdminController {
    private final UserService userService;

    @Override
    @PostMapping(value = "/user")
    public void userCreate(UserCreateRequestDto createRequestDto, HttpServletRequest request) {
        userService.createUserByAdmin(createRequestDto);
        throw new ResponseStatusException(HttpStatus.CREATED);
    }

    @Override
    @PutMapping(value = "/user")
    public void userUpdate(UserUpdateRequestDto updateRequestDto, HttpServletRequest request) {
        userService.updateUserByAdmin(updateRequestDto);
    }

    @Override
    @GetMapping(value = "/user/all")
    public List<UserDto> userAll(Pageable pageable, HttpServletRequest request) {
        List<UserDto> users = userService.findUserByAdmin(pageable);
        return users;
    }

    @Override
    @DeleteMapping(value = "/user/{userId}")
    public void userRoleChange(Integer userId, HttpServletRequest request) {
        UserDto user = userService.findById(UserFindDto.builder().userId(userId).build());
        userService.toggleUserRoleType(user);
    }
}
