package sn.edu.ept.api_gateway.filter;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class RoleAuthorizationFilter
        extends AbstractGatewayFilterFactory<RoleAuthorizationFilter.Config> {

    public RoleAuthorizationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String userRole = exchange.getRequest().getHeaders().getFirst("X-User-Role");
            String required = config.getRequiredRole();

            if (userRole == null || !userRole.equals(required)) {
                log.warn("[Gateway] Accès refusé — rôle={} requis={} path={}",
                        userRole, required, exchange.getRequest().getURI().getPath());
                return forbiddenResponse(exchange.getResponse(),
                        "Accès réservé aux utilisateurs ayant le rôle " + required + ".");
            }

            return chain.filter(exchange);
        };
    }

    private Mono<Void> forbiddenResponse(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = String.format(
                "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"%s\"}", message);
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(bytes)));
    }

    @Getter
    @Setter
    public static class Config {
        private String requiredRole;
    }
}
