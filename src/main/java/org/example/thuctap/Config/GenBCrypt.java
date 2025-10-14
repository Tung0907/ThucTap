package org.example.thuctap.Config;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
public class GenBCrypt {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String raw = "123456";
        System.out.println("Tung hash: " + encoder.encode(raw));
    }
}
