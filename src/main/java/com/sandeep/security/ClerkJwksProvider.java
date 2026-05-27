package com.sandeep.security;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class ClerkJwksProvider {

    @Value("${clerk.jwks-url}")
    private String jwksUrl;

    private final Map<String, PublicKey>keyCache=new HashMap<>();
    private long lastTime=0;
    private static final long CACHE_TTL=3600000;

    public PublicKey getPublicKey(String kid) throws Exception{
        if(keyCache.containsKey(kid) && System.currentTimeMillis()-lastTime<CACHE_TTL){
            return keyCache.get(kid);
        }
        refreshKeys();
        return keyCache.get(kid);
    }

    private void refreshKeys() throws Exception{
         ObjectMapper mapper=new ObjectMapper();
        JsonNode jwks= mapper.readTree(new URL(jwksUrl).openStream());
        JsonNode keys=jwks.get("keys");
        for(JsonNode key:keys){
            String kid=key.get("kid").asText();
            String kty=key.get("kty").asText();
            String alg=key.get("alg").asText();
            if("RSA".equals(kty) && "RS256".equals(alg)){
                String n=key.get("n").asText();
                String e=key.get("e").asText();
                PublicKey publicKey=createPublicKey(n,e);
                keyCache.put(kid,publicKey);
            }
            lastTime=System.currentTimeMillis();
        }
    }

    private PublicKey createPublicKey(String modulus, String exponent) throws Exception{
        byte[] modulusBytes= Base64.getUrlDecoder().decode(modulus);
        byte[] exponentBytes=Base64.getUrlDecoder().decode(exponent);
        BigInteger modulusInt=new BigInteger(1,modulusBytes);
        BigInteger exponentInt=new BigInteger(1,exponentBytes);
        RSAPublicKeySpec spec=new RSAPublicKeySpec(modulusInt,exponentInt);
        KeyFactory keyFactory=KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

}
