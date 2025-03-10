package com.example.saferoutine.api.controller;

import com.example.saferoutine.api.service.UserService;
import com.example.saferoutine.common.util.ConfigUtils;
import com.example.saferoutine.db.entity.User;
import com.example.saferoutine.dto.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;

@CrossOrigin(origins = "*")
@Controller
@RequestMapping(value = "/api/google")
public class GoogleController {

    @Autowired
    private UserService userService;
    private final ConfigUtils configUtils;

    GoogleController(ConfigUtils configUtils) {
        this.configUtils = configUtils;
    }

    @GetMapping(value = "/login")
    public ResponseEntity<Object> moveGoogleInitUrl() {
        String authUrl = configUtils.googleInitUrl();
        URI redirectUri = null;
        try {
            redirectUri = new URI(authUrl);
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setLocation(redirectUri);
            return new ResponseEntity<>(httpHeaders, HttpStatus.SEE_OTHER);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return ResponseEntity.badRequest().build();
    }

    @GetMapping(value = "/login/redirect")
    public ResponseEntity<LoginResponseDTO> redirectGoogleLogin(
            @RequestParam(value = "code") String authCode
    ) {
        // HTTP 통신을 위해 RestTemplate 활용
        RestTemplate restTemplate = new RestTemplate();
        GoogleLoginRequest requestParams = GoogleLoginRequest.builder()
                .clientId(configUtils.getGoogleClientId())
                .clientSecret(configUtils.getGoogleSecret())
                .code(authCode)
                .redirectUri(configUtils.getGoogleRedirectUri())
                .grantType("authorization_code")
                .build();

        try {
            // Http Header 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<GoogleLoginRequest> httpRequestEntity = new HttpEntity<>(requestParams, headers);
            ResponseEntity<String> apiResponseJson = restTemplate.postForEntity(configUtils.getGoogleAuthUrl() + "/token", httpRequestEntity, String.class);

            // ObjectMapper를 통해 String to Object로 변환
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // NULL이 아닌 값만 응답받기(NULL인 경우는 생략)
            GoogleLoginResponse googleLoginResponse = objectMapper.readValue(apiResponseJson.getBody(), new TypeReference<GoogleLoginResponse>() {});

            // 사용자의 정보는 JWT Token으로 저장되어 있고, Id_Token에 값을 저장한다.
            String jwtToken = googleLoginResponse.getIdToken();

            // JWT Token을 전달해 JWT 저장된 사용자 정보 확인
            String requestUrl = UriComponentsBuilder.fromHttpUrl(configUtils.getGoogleAuthUrl() + "/tokeninfo").queryParam("id_token", jwtToken).toUriString();

            String resultJson = restTemplate.getForObject(requestUrl, String.class);

            if(resultJson != null) {
                GoogleLoginDto userInfoDto = objectMapper.readValue(resultJson, new TypeReference<GoogleLoginDto>() {});
                LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
                User user = userService.getUserByEmail(userInfoDto.getEmail());
                if (user == null) {
                    RegisterDTO registerDTO = new RegisterDTO();
                    registerDTO.setEmail(userInfoDto.getEmail());
                    registerDTO.setName(userInfoDto.getName());
                    registerDTO.setPhoneNumber("010-0000-0000");
                    registerDTO.setPassword(userInfoDto.getAud());
                    registerDTO.setProfileImage(userInfoDto.getPicture());
                    userService.register(registerDTO);
                    loginResponseDTO.setResult("REGISTER_SUCCESS");
                    System.out.println("#################구글 가입 성공");
                }
                else{
                    EmailPwDTO emailPwDTO = new EmailPwDTO();
                    emailPwDTO.setEmail(userInfoDto.getEmail());
                    emailPwDTO.setPassword(userInfoDto.getAud());
                    loginResponseDTO = userService.logIn2(emailPwDTO);
                    loginResponseDTO.setSotialType("GOOGLE");
                    System.out.println("#################구글 로그인 성공");
                }


                System.out.println(userInfoDto);

                return ResponseEntity.ok().body(loginResponseDTO);
            }
            else {
                throw new Exception("Google OAuth failed!");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.badRequest().body(null);
    }
}