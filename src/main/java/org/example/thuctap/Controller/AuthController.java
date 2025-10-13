package org.example.thuctap.Controller;

import org.example.thuctap.Model.User;
import org.example.thuctap.Repository.UserRepository;
import org.example.thuctap.Security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        try {
            System.out.println("==> Login request: username=" + loginRequest.getUsername() + ", password=" + loginRequest.getPassword());

            User user = userRepository.findByUsername(loginRequest.getUsername());
            if (user == null) {
                System.out.println("==> Không tìm thấy user trong DB!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Sai tên đăng nhập hoặc mật khẩu!"));
            }

            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                System.out.println("==> Sai mật khẩu!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Sai tên đăng nhập hoặc mật khẩu!"));
            }

            String token = jwtUtil.generateToken(user.getUsername());
            System.out.println("==> Đăng nhập thành công! Username: " + user.getUsername());
            System.out.println("==> Token sinh ra: " + token);

            Map<String, Object> res = new HashMap<>();
            res.put("message", "Đăng nhập thành công!");
            res.put("token", token);
            res.put("username", user.getUsername());
            res.put("role", user.getRole());
            return ResponseEntity.ok(res);

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi server"));
        }
    }
}
