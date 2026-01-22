package com.example.MyFirstTgBot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SecurityService {
    private final Set<Long> approvedUsers;

    public SecurityService(@Value("${Id}") String idApprovedUsers) {
        this.approvedUsers = Arrays.stream(idApprovedUsers.split(","))
                .map(String::trim)
                .map(Long::parseLong)
                .collect(Collectors.toSet());
    }

    public boolean isApproved(Long userId){
        return approvedUsers.contains(userId);
    }
}
