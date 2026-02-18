package com.example.todo.config;

import com.example.todo.entity.AppUser;
import com.example.todo.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class PasswordResetEnforcementFilter extends OncePerRequestFilter {
    private final UserRepository userRepository;

    public PasswordResetEnforcementFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestPath = request.getRequestURI();
        if (isExcludedPath(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        String username = authentication.getName();
        Optional<AppUser> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent() && userOpt.get().isPasswordResetRequired()) {
            response.sendRedirect(request.getContextPath() + "/password/reset");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isExcludedPath(String path) {
        return path.startsWith("/login")
                || path.startsWith("/logout")
                || path.startsWith("/password/reissue")
                || path.startsWith("/password/reset")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.startsWith("/h2-console");
    }
}
