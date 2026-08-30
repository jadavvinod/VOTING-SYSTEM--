package com.college.voting.security;

import com.college.voting.repository.AdminRepository;
import com.college.voting.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final StudentRepository studentRepository;
    private final AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return studentRepository.findByEmail(email)
                .<UserDetails>map(s -> s)
                .or(() -> adminRepository.findByEmail(email).map(a -> a))
                .orElseThrow(() -> new UsernameNotFoundException("No user found with email: " + email));
    }
}
