package com.devunion.backend.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일을 기반으로 User 객체를 찾는 사용자 정의 쿼리 메서드
    // Spring Data JPA가 메서드 이름 규칙에 따라 자동으로 SQL 쿼리를 생성
    Optional<User> findByEmail(String email);


}
