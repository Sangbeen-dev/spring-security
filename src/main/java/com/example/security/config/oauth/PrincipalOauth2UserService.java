package com.example.security.config.oauth;

import com.example.security.config.auth.PrincipalDetails;
import com.example.security.config.oauth.provider.GoogleUserInfo;
import com.example.security.config.oauth.provider.NaverUserInfo;
import com.example.security.config.oauth.provider.OAuth2UserInfo;
import com.example.security.model.User;
import com.example.security.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    public PrincipalOauth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 구글로 받은 userRequest 데이터에 대한 후처리 함수
    // 함수 종료시 @AuthenticationPrincipal 어노테이션이 만들어짐
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        System.out.println("getClientRegistration: " + userRequest.getClientRegistration()); // 어떤 Oauth로 로그인 했는지 알 수 있음.
        System.out.println("getAccessToken: " + userRequest.getAccessToken().getTokenValue());

        OAuth2User oAuth2User = super.loadUser(userRequest);
        // 구글로그인 -> code리턴 -> AccesToken 요청
        // userRequest 정보 -> loadUser함수 -> 회원정보
        System.out.println("getAttributes: " + oAuth2User.getAttributes());

        OAuth2UserInfo oAuth2UserInfo = null;
        if (userRequest.getClientRegistration().getRegistrationId().equals("google")) {
            System.out.println("google 로그인 요청");
            oAuth2UserInfo = new GoogleUserInfo(oAuth2User.getAttributes());
        } else if (userRequest.getClientRegistration().getRegistrationId().equals("naver")) {
            System.out.println("naver 로그인 요청");
            oAuth2UserInfo = new NaverUserInfo((Map) oAuth2User.getAttributes().get("response"));
        }

        User entity = userRepository.findByUsername(oAuth2UserInfo.getName());
        if (entity == null) {
            entity = User.builder()
                    .username(oAuth2UserInfo.getName())
                    .password(new BCryptPasswordEncoder().encode("security"))
                    .email(oAuth2UserInfo.getEmail())
                    .role("ROLE_USER")
                    .provider(oAuth2UserInfo.getProvider())
                    .providerId(oAuth2UserInfo.getProviderId())
                    .build();
            userRepository.save(entity);
        }

        return new PrincipalDetails(entity, oAuth2User.getAttributes()); // Authentication 객체
    }
}
