package com.gestionremodelacion.gestion.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gestionremodelacion.gestion.model.RefreshToken;
import com.gestionremodelacion.gestion.model.User;

import jakarta.persistence.QueryHint;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUser_Username(String username);

    Optional<RefreshToken> findByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.used = true WHERE rt.token = :token")
    @QueryHints(
            @QueryHint(name = "org.hibernate.comment", value = "Marcando refresh token como usado"))
    int markAsUsed(@Param("token") String token);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.used = true WHERE rt.user.id = :userId")
    int markAllAsUsedByUserId(@Param("userId") Long userId);

    boolean existsByToken(String token);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user = :user")
    void deleteByUser(@Param("user") User user);

}
