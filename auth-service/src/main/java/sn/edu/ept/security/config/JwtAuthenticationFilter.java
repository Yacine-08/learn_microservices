package sn.edu.ept.security.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import sn.edu.ept.security.service.ConnectedUserService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final ConnectedUserService connectedUserService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // check the jwt token
        // si le header est absent ou ne commence pas par Bearer
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // extract the token from the auth header
        jwt = authHeader.substring(7);
        userEmail = jwtService.extractUsername(jwt);

        // after extracting the jwt token, check if we have the user within our database or not
        // if we have the userEmail and user not authenticated
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // we get the userDetails from the database
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            // check if the token is valid
            if(jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // update authentication in the security context
                SecurityContextHolder.getContext().setAuthentication(authToken);
                
                // Enregistrer la connexion de l'utilisateur
                connectedUserService.addUserConnection(userEmail);
            }
        }
        filterChain.doFilter(request, response);
    }
}
