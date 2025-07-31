package demo.todolist.service;

import demo.todolist.entity.User;
import demo.todolist.entity.UserRole;
import demo.todolist.repository.UserRepository;
import demo.todolist.utils.DtoMapper;
import demo.todolist.web.dto.RegisterRequest;
import demo.todolist.web.dto.RegisterResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public RegisterResponse registerUser(RegisterRequest registerRequest){
        if (userRepository.findUserByUsername(registerRequest.getUsername()).isPresent()){
            throw new IllegalArgumentException("This username is already in use!");
        }
        if (userRepository.findUserByEmail(registerRequest.getEmail()).isPresent()){
            throw new IllegalArgumentException("This email is already in use!");
        }
        User user = DtoMapper.toEntity(registerRequest);

        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        if (userRepository.count() == 0){
            user.setRole(UserRole.ADMIN);
        }
        User savedUser = userRepository.save(user);
        return DtoMapper.toResponse(savedUser);
    }

}
