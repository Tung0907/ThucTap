package org.example.thuctap.Controller;

import org.example.thuctap.Model.User;
import org.example.thuctap.Repository.UserRepository;
import org.example.thuctap.Service.UserService;
import org.example.thuctap.dto.*;
import org.example.thuctap.exception.BadRequestException;
import org.example.thuctap.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getAll() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users.stream().map(dto -> {
            return org.example.thuctap.dto.UserResponse.fromEntity(dto);
        }).collect(Collectors.toList()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody org.example.thuctap.dto.UserRequest req, BindingResult br) {
        logger.info("POST /api/users/register {}", req.getUsername());
        if (br.hasErrors()) throw new BadRequestException("Dữ liệu không hợp lệ");

        if (userRepository.findByUsername(req.getUsername()) != null) {
            throw new BadRequestException("Username đã tồn tại");
        }
        User u = new User();
        u.setUsername(req.getUsername());
        u.setPassword(new BCryptPasswordEncoder().encode(req.getPassword()));
        u.setFullName(req.getFullName());
        u.setEmail(req.getEmail());
        u.setRole(req.getRole() == null ? "USER" : req.getRole().toUpperCase());
        User saved = userService.addUser(u);
        return ResponseEntity.status(HttpStatus.CREATED).body(org.example.thuctap.dto.UserResponse.fromEntity(saved));
    }
}
