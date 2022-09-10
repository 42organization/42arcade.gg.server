package io.pp.arcade.domain.admin.controller;

import io.pp.arcade.domain.admin.dto.create.UserCreateRequestDto;
import io.pp.arcade.domain.admin.dto.update.UserUpdateRequestDto;
import io.pp.arcade.domain.user.dto.UserDto;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface UserAdminController {
    void userCreate(@RequestBody UserCreateRequestDto userCreateDto, HttpServletRequest request);
    void userUpdate(@RequestBody UserUpdateRequestDto userUpdateDto, HttpServletRequest request);
    List<UserDto> userAll(Pageable pageable, HttpServletRequest request);
    void userRoleChange(@PathVariable Integer userId, HttpServletRequest request);
}
