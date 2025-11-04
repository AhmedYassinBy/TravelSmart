package com.ahmedyassin.TravelSmart.controllers;

import org.springframework.security.crypto.bcrypt.BCrypt;

import java.security.SecureRandom;
import java.util.Base64;

public class Password {
    public static void main(String[] args) {
        String password = "12345678";
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
        System.out.println(hashed);
    }

//        public static void main(String[] args) {
//            SecureRandom secureRandom = new SecureRandom();
//            byte[] key = new byte[32]; // 256 bits
//            secureRandom.nextBytes(key);
//            String encodedKey = Base64.getEncoder().encodeToString(key);
//            System.out.println("Generated Base64 Secret Key: " + encodedKey);
//        }

}



