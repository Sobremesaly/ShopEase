package com.shopease.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopease.result.Result;
import com.shopease.utils.JwtUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * @author hspcadmin
 */
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    // 不需要拦截的接口（公开接口）
    private static final List<String> WHITE_LIST = Arrays.asList(
            // 登录接口
            "/sys/user/login",
            // 后续的注册接口，也放这里
            "/sys/user/register"
    );

    private final ObjectMapper objectMapper;

    public JwtAuthFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // 1. 白名单接口直接放行
        if (WHITE_LIST.stream().anyMatch(path::contains)) {
            return chain.filter(exchange);
        }

        // 2. 非白名单接口，获取请求头中的 Token
        String token = request.getHeaders().getFirst("Authorization");
        if (token == null || token.isEmpty()) {
            // 无 Token，返回 401 未登录
            return handleUnAuth(exchange, "请先登录");
        }

        // 3. 验证 Token 有效性
        boolean valid = JwtUtils.validateToken(token);
        if (!valid) {
            // Token 无效或过期，返回 401
            return handleUnAuth(exchange, "登录已过期，请重新登录");
        }

        // 4. Token 有效，放行到对应的微服务
        return chain.filter(exchange);
    }

    // 处理未登录响应
    private Mono<Void> handleUnAuth(ServerWebExchange exchange, String msg) {
        ServerHttpResponse response = exchange.getResponse();
        // 401的状态码
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");

        // 封装响应结果
        Result<Void> result = Result.error(msg);
        try {
            byte[] bytes = objectMapper.writeValueAsString(result).getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            return response.setComplete();
        }
    }

    // 过滤器执行顺序（数字越小，越先执行）
    @Override
    public int getOrder() {
        return -100;
    }
}
