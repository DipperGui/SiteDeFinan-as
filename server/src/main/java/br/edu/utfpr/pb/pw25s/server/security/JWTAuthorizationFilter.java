package br.edu.utfpr.pb.pw25s.server.security;

import br.edu.utfpr.pb.pw25s.server.model.User;
import br.edu.utfpr.pb.pw25s.server.service.AuthService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JWTAuthorizationFilter  extends BasicAuthenticationFilter {

    private  final AuthService authService;

    public JWTAuthorizationFilter(AuthenticationManager authenticationManager,
                                  AuthService authService) {
        super(authenticationManager);
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {

        String header = request.getHeader(SecutiryConstants.HEADER_STRING);

        if(header == null || !header.startsWith(SecutiryConstants.TOKEN_PREFIX)){
            chain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken authenticationToken = getAuthentication(request);

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        chain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(SecutiryConstants.HEADER_STRING);

        String username = JWT.require(Algorithm.HMAC512(SecutiryConstants.SECRET))
                .build()
                .verify(token.replace(SecutiryConstants.TOKEN_PREFIX, ""))
                .getSubject();
        if (username != null){
            User user = (User) authService.loadUserByUsername(username);
            return new UsernamePasswordAuthenticationToken(
                    user.getUsername(),
                    null,
                    user.getAuthorities());
        }
        return null;
    }
}
