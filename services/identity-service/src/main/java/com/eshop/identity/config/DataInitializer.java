/**
 * Converted from: src/Identity.API/UsersSeed.cs
 * .NET Class: eShop.Identity.API.UsersSeed
 *
 * Initial data seeder for users.
 */
package com.eshop.identity.config;

import com.eshop.identity.model.ApplicationUser;
import com.eshop.identity.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Seeding default users...");

            // Alice - regular user
            ApplicationUser alice = new ApplicationUser();
            alice.setUsername("alice");
            alice.setEmail("AliceSmith@email.com");
            alice.setPassword(passwordEncoder.encode("Pass123$"));
            alice.setFirstName("Alice");
            alice.setLastName("Smith");
            alice.setCardNumber("4012888888881881");
            alice.setSecurityNumber("123");
            alice.setExpiration("12/25");
            alice.setCardHolderName("Alice Smith");
            alice.setCardType(1);
            alice.setStreet("15703 NE 61st Ct");
            alice.setCity("Redmond");
            alice.setState("WA");
            alice.setCountry("U.S.");
            alice.setZipCode("98052");
            alice.setRoles(Set.of("ROLE_USER"));
            userRepository.save(alice);

            // Bob - regular user
            ApplicationUser bob = new ApplicationUser();
            bob.setUsername("bob");
            bob.setEmail("BobSmith@email.com");
            bob.setPassword(passwordEncoder.encode("Pass123$"));
            bob.setFirstName("Bob");
            bob.setLastName("Smith");
            bob.setCardNumber("4012888888881881");
            bob.setSecurityNumber("456");
            bob.setExpiration("12/25");
            bob.setCardHolderName("Bob Smith");
            bob.setCardType(1);
            bob.setStreet("One Microsoft Way");
            bob.setCity("Redmond");
            bob.setState("WA");
            bob.setCountry("U.S.");
            bob.setZipCode("98052");
            bob.setRoles(Set.of("ROLE_USER"));
            userRepository.save(bob);

            log.info("Default users seeded: alice, bob (password: Pass123$)");
        }
    }
}
