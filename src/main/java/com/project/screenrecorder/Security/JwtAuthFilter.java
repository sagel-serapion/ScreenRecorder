package com.project.screenrecorder.Security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;


@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {


        // Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9. -> Header
        //eyJzdWIiOiJzYWdlbCIsInJvbGUiOiJVU0VSIiwiZXhwIjoxNzUwMDAwMDAwfQ. -> Payload
        //abc123xyz456 -> Signature
        String authHeader = request.getHeader("Authorization");

        String accesstoken = null;
        String username = null ;

        if (authHeader != null && authHeader.startsWith("Bearer ") ){
            accesstoken = authHeader.substring(7);

            try{

                username =jwtUtils.extractName(accesstoken);

            }
            catch (JwtException e){

                filterChain.doFilter(request,response);

                return;
            }
        }

        if( username != null && SecurityContextHolder.getContext().getAuthentication() == null){

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtUtils.validateToken(accesstoken,username,userDetails)){

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,null,userDetails.getAuthorities()
                        );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        }
        // if missed returned a 200 but no response body
        // very important

        filterChain.doFilter(request, response);

    }
}
