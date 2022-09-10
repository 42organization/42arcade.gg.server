package io.pp.arcade.domain.user;

import io.pp.arcade.domain.admin.dto.create.UserCreateRequestDto;
import io.pp.arcade.domain.admin.dto.update.UserUpdateRequestDto;
import io.pp.arcade.domain.user.dto.*;
import io.pp.arcade.global.exception.BusinessException;
import io.pp.arcade.global.type.RoleType;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public UserDto findByIntraId(UserFindDto findDto) {
        User user = userRepository.findByIntraId(findDto.getIntraId()).orElseThrow(() -> new BusinessException("E0001"));
        return UserDto.from(user);
    }

    @Transactional
    public UserDto findById(UserFindDto findDto) {
        User user = userRepository.findById(findDto.getUserId()).orElseThrow(() -> new BusinessException("E0001"));
        return UserDto.from(user);
    }

    /* 유저 등록 */
    @Transactional
    public void addUser(UserAddDto addDto) {
        User user = User.builder()
                .intraId(addDto.getIntraId())
                .eMail(addDto.getEMail())
                .roleType(RoleType.USER)
                .statusMessage("")
                .ppp(0)
                .build();
        userRepository.save(user);
    }

    /* 유저 ppp 변경 */
    @Transactional
    public void modifyUserPpp(UserModifyPppDto modifyDto) {
        User user = userRepository.findById(modifyDto.getUserId()).orElseThrow(() -> new BusinessException("E0001"));
        user.setPpp(modifyDto.getPpp());
    }

    /* 유저 정보 업데이트 */
    @Transactional
    public void modifyUserProfile(UserModifyProfileDto modifyDto) {
        User user = userRepository.findById(modifyDto.getUserId()).orElseThrow(() -> new BusinessException("E0001"));
        //user.setEMail(modifyDto.getEmail());
        //user.setImageUri(modifyDto.getUserImageUri());
        user.setRacketType(modifyDto.getRacketType());
        user.setStatusMessage(modifyDto.getStatusMessage());
    }

    /* 문자열을 포함하는 intraId를 가진 유저 찾기 */
    @Transactional
    public List<UserDto> findByPartsOfIntraId(UserSearchRequestDto userSearchDto) {
        List<UserDto> result = new ArrayList<UserDto>();
        if (!userSearchDto.getIntraId().isEmpty()){
            Pageable pageable = PageRequest.of(0, 5, JpaSort.unsafe("locate('" + userSearchDto.getIntraId() + "', intraId)").ascending().and(Sort.by("intraId")));
            Page<User> users = userRepository.findByIntraIdContains(userSearchDto.getIntraId(), pageable);
            result.addAll(users.stream().map(UserDto::from).collect(Collectors.toList()));
        }
        return result;
    }

    @Transactional
    public void createUserByAdmin(UserCreateRequestDto userCreateDto) {
        User user = User.builder()
                .intraId(userCreateDto.getIntraId())
                .eMail(userCreateDto.getEmail())
                .imageUri(userCreateDto.getUserImageUri())
                .racketType(userCreateDto.getRacketType())
                .statusMessage(userCreateDto.getStatusMessage() == null ? "" : userCreateDto.getStatusMessage())
                .ppp(userCreateDto.getPpp() == null ? 0 : userCreateDto.getPpp())
                .roleType(RoleType.USER)
                .build();
        userRepository.save(user);
    }

    @Transactional
    public void updateUserByAdmin(UserUpdateRequestDto updateRequestDto) {
        User user = userRepository.findById(updateRequestDto.getUserId()).orElseThrow();
        user.setImageUri(updateRequestDto.getUserImageUri());
        user.setRacketType(updateRequestDto.getRacketType());
        user.setStatusMessage(updateRequestDto.getStatusMessage());
        user.setPpp(updateRequestDto.getPpp());
    }

    @Transactional
    public List<UserDto> findUserByAdmin(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        List<UserDto> userDtos = users.stream().map(UserDto::from).collect(Collectors.toList());
        return userDtos;
    }

    @Transactional
    public List<UserDto> findAllByRoleType(RoleType roleType) {
        List<User> users = userRepository.findAllByRoleType(roleType);
        List<UserDto> userDtos = users.stream().map(UserDto::from).collect(Collectors.toList());
        return userDtos;
    }

    @Transactional
    public void toggleUserRoleType(UserDto userDto) {
        User user = userRepository.findById(userDto.getId()).orElseThrow(() -> new BusinessException("E0001"));
        if (user.getRoleType() == RoleType.USER) {
            user.setRoleType(RoleType.ADMIN);
        } else {
            user.setRoleType(RoleType.USER);
        }
    }
}
