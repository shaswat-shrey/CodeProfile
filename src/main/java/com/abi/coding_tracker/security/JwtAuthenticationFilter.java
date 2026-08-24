package com.abi.coding_tracker.security;

import java.io.IOException;
// REMOVE THESE TWO INCORRECT IMPORTS:
// import java.nio.charset.MalformedInputException;
// import java.security.SignatureException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.context.annotation.Lazy;

import io.jsonwebtoken.ExpiredJwtException;
// ADD THESE TWO CORRECT JJWT IMPORTS:
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailService userDetailsService;

    // Use @Lazy here to prevent early initialization of JPA (which breaks Flyway)
    public JwtAuthenticationFilter(JwtService jwtService, @Lazy CustomUserDetailService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Look for the "Authorization" header in the HTTP Request
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 2. If there is no header, OR it doesn't start with "Bearer ", reject it!
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.trace("No Bearer token found in request to: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extract the Token
        jwt = authHeader.substring(7);

        try {
            // 4. Extract the Email from the Token
            userEmail = jwtService.extractUsername(jwt);
            log.debug("Extracted username '{}' from JWT", userEmail);

            // 5. If we found an email, AND the user isn't already authenticated...
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 6. Fetch the user details
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // 7. Validate the Token mathematically
                if (jwtService.validateToken(jwt, userDetails.getUsername())) {
                    log.debug("JWT is valid for user: {}", userEmail);
                    
                    // 8. Create the "VIP Stamp"
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 9. Place the "VIP Stamp" onto the SecurityContextHolder
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    log.warn("Invalid JWT presented for user: {}", userEmail);
                }
            }
        }catch(ExpiredJwtException e){
            log.warn("JWT has expired : {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,"JWT has expired. Please log in again.");
            return;
        }catch(SignatureException e){
            log.warn("Invalid JWT signature: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT signature");
            return;
        }catch(MalformedJwtException e){ // <-- UPDATE THIS CLASS NAME TOO!
            log.warn("Malformed JWT token: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Malformed JWT token");
            return;
        } catch (Exception e) {
            log.error("Could not set user authentication in security context", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected authentication error occurred.");
            return; // Stop the filter chain!
        }

        // 10. Pass the request to the next filter
        filterChain.doFilter(request, response);
    }

    // Helper method to write a JSON error directly to the HTTP response
    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        
        // Manually format the JSON to match your ErrorResponse DTO structure!
        String json = String.format(
            "{\"timestamp\": \"%s\", \"status\": %d, \"message\": \"%s\"}",
            java.time.LocalDateTime.now().toString(),
            status,
            message
        );
        response.getWriter().write(json);
    }
}