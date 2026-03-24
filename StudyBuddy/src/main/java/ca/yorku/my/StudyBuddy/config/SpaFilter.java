package ca.yorku.my.StudyBuddy.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SpaFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String uri = req.getRequestURI();

        // Only forward GET requests that don't start with /api and don't contain dots (static resources)
        // Exclude /index.html to avoid conflicts with ReactRoutingController
        if (req.getMethod().equals("GET") && !uri.startsWith("/api") && !uri.contains(".") && !uri.equals("/index.html")) {
            req.getRequestDispatcher("/index.html").forward(req, res);
            return;
        }
        chain.doFilter(request, response);
    }
}