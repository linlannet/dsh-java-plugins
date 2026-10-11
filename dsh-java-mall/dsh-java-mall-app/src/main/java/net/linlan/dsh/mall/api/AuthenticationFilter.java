package net.linlan.dsh.mall.api;

import net.linlan.dsh.mall.domain.Customer;
import net.linlan.dsh.mall.service.AuthService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);
    private final AuthService authService;
    private final String serviceToken;

    public AuthenticationFilter(
            AuthService authService,
            @Value("${mall.security.service-token:mall-internal-demo-token}") String serviceToken
    ) {
        this.authService = authService;
        this.serviceToken = serviceToken;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        if (!request.getRequestURI().startsWith("/api/")) {
            chain.doFilter(request, response);
            return;
        }

        String incomingServiceToken = request.getHeader("X-Service-Token");
        boolean serviceAuthorized = incomingServiceToken != null && incomingServiceToken.equals(serviceToken);
        System.err.println("[AUTH-DEBUG] uri=" + request.getRequestURI() + " method=" + request.getMethod()
                + " token=" + (incomingServiceToken == null ? "<null>" : incomingServiceToken)
                + " authorized=" + serviceAuthorized
                + " customerId=" + request.getParameter("customerId"));
        Customer customer = serviceAuthorized
                ? authService.currentCustomerById(request.getParameter("customerId"))
                : authService.currentCustomer(request);
        System.err.println("[AUTH-DEBUG] resolved customer=" + (customer == null ? "null" : customer.id()));
        boolean publicApi = request.getRequestURI().startsWith("/api/auth/")
                || request.getRequestURI().startsWith("/api/mall/products");
        if (publicApi) {
            chain.doFilter(request, response);
            return;
        }

        if (customer == null) {
            log.warn("auth: customer is null, returning 401");
            unauthorized(response);
            return;
        }

        request.setAttribute("currentCustomer", customer);
        chain.doFilter(request, response);
    }

    private void unauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"请先登录\"}");
    }
}