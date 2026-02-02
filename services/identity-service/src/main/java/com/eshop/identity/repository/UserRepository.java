/**
 * Converted from: src/Identity.API/Data/ApplicationDbContext.cs
 * .NET Class: eShop.Identity.API.Data.ApplicationDbContext
 *
 * Repository for ApplicationUser entities.
 */
package com.eshop.identity.repository;

import com.eshop.identity.model.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<ApplicationUser, String> {

    Optional<ApplicationUser> findByUsername(String username);

    Optional<ApplicationUser> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
