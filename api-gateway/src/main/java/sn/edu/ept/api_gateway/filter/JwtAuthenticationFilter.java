package sn.edu.ept.api_gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import sn.edu.ept.api_gateway.config.JwtConfig;

import java.nio.charset.StandardCharsets;

/*
    Filtre Gateway appliqué sur toutes les routes protégées.
    Comportement :
        1. Extrait le Bearer token depuis Authorization.
        2. Valide la signature et l'expiration.
        3. Injecte les claims dans les headers de la requête routée :
            X-Auth-Id → id numérique de l'utilisateur (authId)
            X-User-Role → rôle (CLIENT, ADMIN, DRIVER)
            X-User-Email → email (subject du JWT)
        4. En cas d'échec → 401 JSON immédiat, pas de routing.
 */
@Slf4j
@Component
public class JwtAuthenticationFilter
        extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    @Autowired
    private JwtConfig jwtConfig;

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("[Gateway] Requête sans token sur {}", path);
                return unauthorizedResponse(exchange.getResponse(),
                        "Token manquant. Veuillez vous connecter.");
            }

            String token = authHeader.substring(7);

            try {
                if (!jwtConfig.isTokenValid(token)) {
                    log.warn("[Gateway] Token invalide ou expiré sur {}", path);
                    return unauthorizedResponse(exchange.getResponse(),
                            "Token invalide ou expiré. Veuillez vous reconnecter.");
                }

                String email  = jwtConfig.extractUsername(token);
                Long   authId = jwtConfig.extractAuthId(token);
                String role   = jwtConfig.extractRole(token);

                // Injecter les claims dans les headers — les services en aval
                // lisent ces headers directement sans retoucher le JWT.
                ServerHttpRequest mutatedRequest = request.mutate()
                        .header("X-Auth-Id",    authId != null ? authId.toString() : "")
                        .header("X-User-Role",  role   != null ? role   : "")
                        .header("X-User-Email", email  != null ? email  : "")
                        .build();
                log.debug("[Gateway] JWT valide — authId={} role={} path={}", authId, role, path);
                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                log.warn("[Gateway] Token expiré sur {}", path);
                return unauthorizedResponse(exchange.getResponse(),
                        "Token expiré. Veuillez vous reconnecter.");
            } catch (io.jsonwebtoken.JwtException e) {
                log.warn("[Gateway] JwtException sur {} : {}", path, e.getMessage());
                return unauthorizedResponse(exchange.getResponse(),
                        "Token invalide.");
            } catch (Exception e) {
                log.error("[Gateway] Erreur inattendue sur {}", path, e);
                return unauthorizedResponse(exchange.getResponse(),
                        "Erreur d'authentification.");
            }
        };
    }

    private Mono<Void> unauthorizedResponse(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = String.format(
                "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"%s\"}", message);
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(bytes)));
    }

    public static class Config {
        // Pas de configuration supplémentaire pour ce filtre
    }
}
