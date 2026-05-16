package com.cmdwrapper.util;

import com.ulisesbocchio.jasyptspringboot.annotation.EncryptablePropertySource;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Password encryption utility.
 * This is a development tool to help generate encrypted passwords.
 * 
 * Usage:
 * 1. Add this @Profile("encrypt") to the main class or run with --spring.profiles.active=encrypt
 * 2. Start the application with: java -jar app.jar --jasypt.encryptor.password=myPassword --spring.profiles.active=encrypt
 * 3. Enter plaintext passwords to get encrypted values
 * 
 * Or use the CLI directly:
 * java -cp command-wrapper-service-1.0.0.jar com.cmdwrapper.util.PasswordEncryptorCLI myPassword
 */
@Configuration
@Profile("encrypt")
public class PasswordEncryptor {

    @Bean
    public CommandLineRunner runner(StringEncryptor encryptor) {
        return new PasswordEncryptorRunner(encryptor);
    }

    private static class PasswordEncryptorRunner implements CommandLineRunner {
        private final StringEncryptor encryptor;

        public PasswordEncryptorRunner(StringEncryptor encryptor) {
            this.encryptor = encryptor;
        }

        @Override
        public void run(String... args) {
            System.out.println("\n===========================================");
            System.out.println("Password Encryption Utility");
            System.out.println("===========================================");
            System.out.println("Enter passwords to encrypt (Ctrl+C to exit):");
            System.out.println("-------------------------------------------\n");

            java.util.Scanner scanner = new java.util.Scanner(System.in);
            while (true) {
                try {
                    System.out.print("Plaintext password: ");
                    String plaintext = scanner.nextLine();
                    if (plaintext == null || plaintext.isEmpty()) {
                        continue;
                    }
                    String encrypted = encryptor.encrypt(plaintext);
                    System.out.println("Encrypted: " + encrypted);
                    System.out.println("Config format: ENC(" + encrypted + ")");
                    System.out.println();
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage() + "\n");
                }
            }
        }
    }
}
