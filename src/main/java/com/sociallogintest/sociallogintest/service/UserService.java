package com.sociallogintest.sociallogintest.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.sociallogintest.sociallogintest.dto.UserDto;
import com.sociallogintest.sociallogintest.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Slf4j
public class UserService {

    private final Environment env;
    private final RestTemplate restTemplate = new RestTemplate();
    private final UserMapper userMapper;

    @Autowired
    public UserService(Environment env, UserMapper userMapper) {
        this.env = env;
        this.userMapper = userMapper;
    }

    public Model findAllUser(Model model) {
        List<UserDto> users = userMapper.findAllUser();
        model.addAttribute("users", users);
        return model;
    }

    public void createUser(UserDto userDto) {
        emailDuplicateCheck(userDto);
        userMapper.createUser(userDto);
    }

    private void emailDuplicateCheck(UserDto userDto) {
        userMapper.findByEmail(userDto.getEmail())
                .ifPresent(m -> {
                    throw new IllegalStateException("이미 존재하는 이메일");
                });
    }

    public void deleteUser(int id) {
        userMapper.deleteUser(id);
    }

    public void socialLogin(String code, String registrationId) {
        log.info("======================================================");

        String accessToken = getAccessToken(code, registrationId);
        JsonNode userResourceNode = getUserResource(accessToken, registrationId);

        UserDto userDto = new UserDto();
        log.info("userDto = {}", userDto);

        switch (registrationId) {
            case "google": {
                userDto.setEmail(userResourceNode.get("email").asText());
                userDto.setProviderId(userResourceNode.get("id").asText());
                userDto.setProvider(registrationId);
                break;
            } case "kakao": {
                userDto.setEmail(userResourceNode.get("kakao_account").get("email").asText());
                userDto.setProviderId(userResourceNode.get("id").asText());
                userDto.setProvider(registrationId);
                break;
            } case "naver": {
                userDto.setEmail(userResourceNode.get("response").get("email").asText());
                userDto.setProviderId(userResourceNode.get("response").get("id").asText());
                userDto.setProvider(registrationId);
                break;
            } default: {
                throw new RuntimeException("UNSUPPORTED SOCIAL TYPE");
            }
        }

        log.info("email = {}", userDto.getEmail());
        log.info("provider id = {}", userDto.getProviderId());
        log.info("provider = {}", userDto.getProvider());
        log.info("======================================================");

        emailDuplicateCheck(userDto);
        userMapper.createUser(userDto);
    }

    private String getAccessToken(String authorizationCode, String registrationId) {
        String clientId = env.getProperty("oauth2." + registrationId + ".client-id");
        String clientSecret = env.getProperty("oauth2." + registrationId + ".client-secret");
        String redirectUri = env.getProperty("oauth2." + registrationId + ".redirect-uri");
        String tokenUri = env.getProperty("oauth2." + registrationId + ".token-uri");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", authorizationCode);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity entity = new HttpEntity(params, headers);

        ResponseEntity<JsonNode> responseNode = restTemplate.exchange(tokenUri, HttpMethod.POST, entity, JsonNode.class);
        JsonNode accessTokenNode = responseNode.getBody();
        return accessTokenNode.get("access_token").asText();
    }

    private JsonNode getUserResource(String accessToken, String registrationId) {
        String resourceUri = env.getProperty("oauth2."+registrationId+".resource-uri");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity entity = new HttpEntity(headers);
        return restTemplate.exchange(resourceUri, HttpMethod.GET, entity, JsonNode.class).getBody();
    }
}
