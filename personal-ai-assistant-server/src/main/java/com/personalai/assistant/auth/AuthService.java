package com.personalai.assistant.auth;

import com.personalai.assistant.auth.dto.LoginRequest;
import com.personalai.assistant.auth.dto.RegisterRequest;
import com.personalai.assistant.auth.dto.TokenResponse;
import com.personalai.assistant.common.BizException;
import com.personalai.assistant.config.JwtProperties;
import com.personalai.assistant.user.UserMapper;
import com.personalai.assistant.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;

    public void register(RegisterRequest req) {
        if (userMapper.findByUsername(req.username()) != null) {
            throw new BizException("Username already taken");
        }
        User user = new User();
        user.setUsername(req.username());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        userMapper.insert(user);
    }

    public TokenResponse login(LoginRequest req) {
        User user = userMapper.findByUsername(req.username());
        if (user == null || !passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new BizException("Invalid username or password");
        }
        String token = jwtUtil.generate(user.getId());
        return new TokenResponse(token, jwtProperties.getExpirationMs());
    }
}
