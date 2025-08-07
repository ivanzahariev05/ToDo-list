package demo.todolist.service;

import demo.todolist.entity.User;
import demo.todolist.entity.UserRole;
import demo.todolist.exception.DuplicateFieldException;
import demo.todolist.repository.UserRepository;
import demo.todolist.utils.DtoMapper;
import demo.todolist.web.dto.AdminUserResponse;
import demo.todolist.web.dto.RegisterRequest;
import demo.todolist.web.dto.RegisterResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;


    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
    }

    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public RegisterResponse registerUser(RegisterRequest registerRequest){

        if (userRepository.findUserByEmail(registerRequest.getEmail()).isPresent()){
            throw new DuplicateFieldException("email", "This email is already in use!");
        }

        if (userRepository.findUserByUsername(registerRequest.getUsername()).isPresent()){
            throw new DuplicateFieldException("username", "This username is already in use!");
        }
        User user = DtoMapper.toUserEntity(registerRequest);

        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        if (userRepository.count() == 0){
            user.setRole(UserRole.ADMIN);
        }
        User savedUser = userRepository.save(user);
        return DtoMapper.toUserResponse(savedUser);
    }

    public RegisterResponse promoteUserToAdmin(UUID id){
       User user =  userRepository.findUserById(id)
               .orElseThrow(() -> new EntityNotFoundException("User with id:" + id + "does not exist"));

       if (user.getRole() == UserRole.ADMIN){
           throw new IllegalArgumentException("User is already a admin");
       }
       user.setRole(UserRole.ADMIN);
       userRepository.save(user);
       return DtoMapper.toUserResponse(user);
    }

    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findUserById(id)
                .orElseThrow(() -> new EntityNotFoundException("User " + id + " not found"));

        if (user.getRole() == UserRole.ADMIN) {
            throw new IllegalArgumentException("You cannot delete an admin!");
        }
        refreshTokenService.revokeAllForUser(id);
        userRepository.delete(user);
    }


    public User findUser(String username) {
        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User with username: " + username + "does not exist!"));
    }

    public List<AdminUserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(u -> new AdminUserResponse(
                        u.getId(),
                        u.getUsername(),
                        u.getEmail(),
                        u.getRole().name(),
                        u.getTasksDone()
                ))
                .collect(Collectors.toList());
    }

    public void saveUser(User user){
        userRepository.save(user);
    }
}


