package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.error.exception.NotFoundException;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<UserDto> users(List<Integer> ids, Integer from, Integer size) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return userRepository.findAllByIdIn(ids, PageRequest.of(from / size, size)).stream()
                                                                                        .map(UserMapper::toUserDto)
                                                                                        .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest newUserRequest) {
        return UserMapper.toUserDto(userRepository.save(UserMapper.toUser(newUserRequest)));
    }

    @Override
    @Transactional
    public void deleteUserId(Integer userId) {
        userRepository.findById(userId).orElseThrow(() -> {
            throw new NotFoundException(String.format("Не найден пользователь с id = %d", userId));
        });
        userRepository.deleteById(userId);
    }
}
