package com.devunion.backend.user;

import com.devunion.backend.jwt.JwtTokenProvider;
import com.devunion.backend.user.dto.LoginRequestDto;
import com.devunion.backend.user.dto.UserProfileResponseDto;
import com.devunion.backend.user.dto.UserRegistrationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

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

        //2. DTO를 User 엔티티로 변환
        User user = new User();
        user.setEmail(registrationDto.getEmail());
        user.setUsername(registrationDto.getUsername());
        user.setMajor(registrationDto.getMajor());
        user.setGrade(registrationDto.getGrade());
        user.setRole(registrationDto.getRole() != null ? registrationDto.getRole() : "후배");

        //3. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(registrationDto.getPassword());
        user.setPassword(encodedPassword);

        //4. DB에 저장
        return userRepository.save(user);

    }

    // 프로필 조회 메서드
    public UserProfileResponseDto getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
        return UserProfileResponseDto.from(user);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

        // Spring Security의 UserDetails 객체로 변환하여 변환
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), new ArrayList<>());
    }
}
