package org.example.thuctap;

import org.example.thuctap.Model.User;
import org.example.thuctap.Repository.UserRepository;
import org.example.thuctap.Service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addUser_encodesPassword_andSaves() {
        User u = User.builder().username("abc").password("raw").build();
        User saved = User.builder().id(1L).username("abc").password("encoded").build();
        when(userRepository.save(any(User.class))).thenReturn(saved);

        User r = userService.addUser(u);
        assertThat(r.getId()).isEqualTo(1L);
        verify(userRepository).save(any(User.class));
        // password should be changed (encoded) before save (not equal raw)
        assertThat(r.getPassword()).isNotEqualTo("raw");
    }

    @Test
    void updateUser_changesFields_ifFound() {
        User existing = User.builder().id(2L).username("u").password("p").fullName("old").email("e@x").role("USER").build();
        User details = User.builder().username("newu").fullName("New").email("n@x").role("ADMIN").build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User updated = userService.updateUser(2L, details);
        assertThat(updated).isNotNull();
        assertThat(updated.getUsername()).isEqualTo("newu");
        assertThat(updated.getFullName()).isEqualTo("New");
        assertThat(updated.getRole()).isEqualTo("ADMIN");
    }
}
