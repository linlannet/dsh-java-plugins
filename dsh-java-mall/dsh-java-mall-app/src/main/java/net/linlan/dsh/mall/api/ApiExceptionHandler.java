package net.linlan.dsh.mall.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public void handleAsyncNotUsable(AsyncRequestNotUsableException exception) {
        log.debug("SSE 响应已关闭: {}", exception.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusiness(BusinessException exception) {
        log.warn("业务异常: {}", exception.getMessage());
        return ResponseEntity.status(exception.getStatus())
                .body(Map.of("code", exception.getStatus().value(), "message", exception.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception exception,
                                                                HttpServletRequest request,
                                                                HttpServletResponse response) {
        log.error("系统异常", exception);
        String requestUri = request.getRequestURI();
        if (requestUri.startsWith("/api/assistant/stream")
                || "text/event-stream".equals(response.getContentType())) {
            return null;
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("code", 500, "message", "商城服务开小差了，请稍后再试"));
    }
}