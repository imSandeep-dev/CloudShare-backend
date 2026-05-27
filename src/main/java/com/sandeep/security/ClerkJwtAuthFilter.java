package com.sandeep.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class ClerkJwtAuthFilter extends OncePerRequestFilter {

    @Value("${clerk.issuer}")
    private String clerkIssuer;

    private final ClerkJwksProvider clerkJwksProvider;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(request.getRequestURI().contains("/webhooks") || request.getRequestURI().contains("/public")
        ){
            filterChain.doFilter(request,response);
            return;
        }
        String authHeader=request.getHeader("Authorization");
        if(authHeader==null || !authHeader.startsWith("Bearer ")){
            response.sendError(HttpServletResponse.SC_FORBIDDEN,"Authorization header is invalid");
            return;
        }
        try {
            String token=authHeader.substring(7);
            String[] chunks=token.split("\\.");
            if(chunks.length<3){
                response.sendError(HttpServletResponse.SC_FORBIDDEN,"token is invalid or expired");
                return;
            }
            String headerJson=new String(Base64.getUrlDecoder().decode(chunks[0]));
            ObjectMapper objectMapper=new ObjectMapper();
            JsonNode headerNode=objectMapper.readTree(headerJson);
            if(!headerNode.has("kid")){
                response.sendError(HttpServletResponse.SC_FORBIDDEN,"Missing token header");
                return;
            }
            String kid=headerNode.get("kid").asText();
            PublicKey publicKey= clerkJwksProvider.getPublicKey(kid);
//            Claims claims= Jwts
//                    .parser()
//                    .setSigningKey(publicKey)
//                    .setAllowedClockSkewSeconds(60)
//                    .requireIssuer(clerkIssuer)
//                    .build()
//                    .parseClaimsJws(token)
//                    .getBody();
            Claims claims=Jwts.parser()
                    .verifyWith((RSAPublicKey)publicKey)
                    .clockSkewSeconds(60)
                    .requireIssuer(clerkIssuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String clerkId=claims.getSubject();
            UsernamePasswordAuthenticationToken authToken=new UsernamePasswordAuthenticationToken(clerkId,null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            filterChain.doFilter(request,response);
        }catch (Exception e){
//            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_FORBIDDEN,"Invalid jwt token");
        }
    }
}
