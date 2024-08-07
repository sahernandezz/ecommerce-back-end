package com.challenge.ecommercebackend.modules.user.service;

import com.challenge.ecommercebackend.email.IEmailSenderService;
import com.challenge.ecommercebackend.jwt.JwtUtils;
import com.challenge.ecommercebackend.modules.user.persisten.entity.Token;
import com.challenge.ecommercebackend.modules.user.persisten.entity.TokenType;
import com.challenge.ecommercebackend.modules.user.persisten.entity.User;
import com.challenge.ecommercebackend.modules.user.persisten.repository.ITokenRepository;
import com.challenge.ecommercebackend.modules.user.persisten.repository.IUserRepository;
import com.challenge.ecommercebackend.modules.user.web.dto.request.AuthRequest;
import com.challenge.ecommercebackend.modules.user.web.dto.response.AuthenticationResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Transactional
public class AuthenticationServiceImpl implements IAuthenticationService {

    private final IUserRepository userRepository;

    private final ITokenRepository tokenRepository;

    private final JwtUtils jwtUtils;

    private final IEmailSenderService mailService;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    @Value("${auth.user.code_length}")
    private Integer codeLength;

    @Value("${auth.user.time_recovery_code}")
    private Integer time;

    public AuthenticationServiceImpl(IUserRepository userRepository, ITokenRepository tokenRepository, JwtUtils jwtUtils, IEmailSenderService mailService, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.jwtUtils = jwtUtils;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Optional<AuthenticationResponse> authenticate(AuthRequest request) {
        Optional<AuthenticationResponse> response;
        try {
            Optional<User> user = userRepository.findByEmail(request.getEmail());
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getEmail()));
            if (!authentication.isAuthenticated()) {
                response = Optional.empty();
            } else {
                response = Optional.of(AuthenticationResponse.builder()
                        .message("Autenticación exitosa")
                        .token(jwtUtils.generateToken(user.get()))
                        .user(user.get())
                        .build());
                revokeAllUserTokens(user.get());
                saveUserToken(user.get(), response.get().getToken());
            }
        } catch (Exception e) {
            response = Optional.empty();
        }
        return response;
    }

    public String recoverPassword(String correo, HttpServletRequest request) {
        Optional<User> user = userRepository.findByEmail(correo);
        if (user.isPresent() && user.get().isEnabled()) {
            sendRecoveryCode(user.get(), request);
        }
        return "Se ha enviado un código de recuperación a su correo electrónico";
    }

    public Optional<User> verificateRecoveryCode(String recoveryCode, Long id) {
        Optional<User> result = Optional.empty();
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent() && user.get().isEnabled() && user.get().getRecoveryDate() != null
                && user.get().getRecoveryCode() != null) {//verificar que el usuario exista y que tenga un código de recuperación
            if (passwordEncoder.matches(recoveryCode, user.get().getRecoveryCode())
                    && !isDateExpired(user.get().getRecoveryDate())) {//verificar que el código de recuperación sea correcto y que no haya expirado
                result = user;
            }
            //verificar si el código de recuperación ha expirado
            if (isDateExpired(user.get().getRecoveryDate())) {
                removeCodeRecovery(user.get());//eliminar código de recuperación
                result = Optional.empty();
            }

        }
        return result;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public Optional<User> changePassword(
            String recoveryCode,
            Long id, String clave
    ) {
        Optional<User> user = verificateRecoveryCode(recoveryCode, id);
        if (user.isPresent()) {
            user.get().setPassword(passwordEncoder.encode(clave));//cambiar contraseña
            user.get().setRecoveryCode(null);
            user.get().setRecoveryDate(null);
            user = Optional.of(userRepository.save(user.get()));
            user.ifPresent(this::revokeAllUserTokens);//revocar tokens
        }

        user.ifPresent(usuario -> mailService.sendPasswordRecoveryConfirmation(usuario.getEmail()));
        return user;
    }

    private void sendRecoveryCode(User user, HttpServletRequest request) {
        if (time != null && time > 0) {
            String code = getRandomString(codeLength);
            user.setRecoveryCode(passwordEncoder.encode(code));
            user.setRecoveryDate(addMinutesCurrentDate(time));
            userRepository.save(user);
            mailService.sendRecoveryCode(user.getEmail(), user.getId(), code, request, time);
        }
    }

    private void saveUserToken(User user, String jwtToken) {
        tokenRepository.save(Token.builder().user(user).token(jwtToken).tokenType(TokenType.BEARER).expired(false).revoked(false).build());
    }

    private void removeCodeRecovery(User user) {
        user.setRecoveryCode(null);
        user.setRecoveryDate(null);
        userRepository.save(user);
    }

    private void revokeAllUserTokens(User user) {
        List<Token> validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty()) {
            return;
        }
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    private Date addMinutesCurrentDate(Integer minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, minutes);
        return calendar.getTime();
    }

    private Boolean isDateExpired(Date date) {
        return date == null || date.before(new Date());
    }

    private String getRandomString(Integer n) {
        String result = null;
        if (n != null && n > 0) {
            String uuid = UUID.randomUUID().toString();
            result = IntStream.range(0, n).map(i ->
                    ThreadLocalRandom.current().nextInt(0, uuid.getBytes(StandardCharsets.UTF_8).length)).mapToObj
                    (index -> String.valueOf(uuid.charAt(index))).collect(Collectors.joining());
        }
        return result;
    }
}