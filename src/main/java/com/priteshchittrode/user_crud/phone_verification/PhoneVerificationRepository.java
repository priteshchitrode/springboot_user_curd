package com.priteshchittrode.user_crud.phone_verification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PhoneVerificationRepository extends JpaRepository<PhoneVerificationEntity, Long> {
    Optional<PhoneVerificationEntity> findByUserId(Long userId);
}
