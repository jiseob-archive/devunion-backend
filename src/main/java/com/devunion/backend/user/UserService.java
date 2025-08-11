package com.devunion.backend.user;

import com.devunion.backend.jwt.JwtTokenProvider;
import com.devunion.backend.user.dto.LoginRequestDto;
import com.devunion.backend.user.dto.UserProfileResponseDto;
import com.devunion.backend.user.dto.UserPublicProfileDto;
import com.devunion.backend.user.dto.UserRegistrationDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor //final 필드들을 이용하여 생성자를 자동으로 생성
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 새로운 사용자 등록
     * @param registrationDto 사용자의 회원가입 정보
     * @return 등록된 User 엔티티
     */
    public User registerNewUser(UserRegistrationDto registrationDto) {
        //1. 이메일 중복 확인
        if (userRepository.findByEmail(registrationDto.getEmail()).isPresent()) {
            throw new IllegalStateException("이미 존재하는 이메일입니다.");
        }
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new IllegalStateException("이미 존재하는 닉네임입니다.");
        }

        //2. DTO를 User 엔티티로 변환
        User user = new User();
        user.setEmail(registrationDto.getEmail());
        user.setUsername(registrationDto.getUsername());
        user.setMajor(registrationDto.getMajor());
        user.setGrade(registrationDto.getGrade());

        // 기본값 STUDENT
        if (registrationDto.getRole() == null) {
            user.setRole(Role.STUDENT);
        } else {
            user.setRole(registrationDto.getRole());
        }

        //3. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(registrationDto.getPassword());
        user.setPassword(encodedPassword);

        //4. DB에 저장
        return userRepository.save(user);

    }

    // 내 프로필 (email기반) - /api/users/me 용
    public UserProfileResponseDto getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
        return UserProfileResponseDto.from(user);
    }

    // 공개 프로필 : id로 조회 (게시판 링크 용)
    public UserPublicProfileDto getPublicProfileById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + id));
        return UserPublicProfileDto.from(user);
    }

    // 공개 프로필 : username으로 조회 (게시판 링크용)
    public UserPublicProfileDto getPublicProfileByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
        return UserPublicProfileDto.from(user);
    }

    public Optional<UserPublicProfileDto> getPublicProfileByUsernameOpt(String username) {
        return userRepository.findByUsername(username)
                .map(UserPublicProfileDto::from);
    }


    // Security: 권한 부여 (ADMIN 이면 STUDENT 권한도 가지고 있음)
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())); // 기본 롤
        if (user.getRole() == Role.ADMIN) {
            authorities.add(new SimpleGrantedAuthority("ROLE_STUDENT"));
        }
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), authorities);
    }

    @Transactional
    public void updateRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        user.setRole(newRole);
    }
}
