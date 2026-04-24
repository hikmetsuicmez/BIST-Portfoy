package com.portfolio.integration;

import com.portfolio.dto.request.LoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Auth endpoint'lerinin uçtan uca testi.
 * Gerçek Supabase DB'ye bağlanır — sadece okuma işlemleri yapılır, veri kirletilemez.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        // HttpURLConnection, POST+401 durumunda streaming mode'da hata fırlatır.
        // Apache HttpClient 5 bu sorunu yaşamaz.
        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    // ---------------------------------------------------------------
    // Login Testleri
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Login: Doğru kimlik bilgileriyle giriş başarılı — 200 ve token dönmeli")
    void login_dogruKimlikBilgisi_basarili() {
        LoginRequest request = new LoginRequest("investor@portfolio.local", "portfolio123");

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl() + "/api/auth/login", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("success")).isEqualTo(true);

        Map data = (Map) body.get("data");
        assertThat(data).isNotNull();
        assertThat(data.get("token")).isNotNull();
        assertThat((String) data.get("token")).isNotBlank();
        assertThat(data.get("tokenType")).isEqualTo("Bearer");

        Map kullanici = (Map) data.get("kullanici");
        assertThat(kullanici.get("email")).isEqualTo("investor@portfolio.local");
    }

    @Test
    @DisplayName("Login: Yanlış şifre ile giriş — 401 Unauthorized dönmeli")
    void login_yanlisSifre_401() {
        LoginRequest request = new LoginRequest("investor@portfolio.local", "yanlis_sifre_123");

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl() + "/api/auth/login", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Login: Var olmayan kullanıcı ile giriş — 401 Unauthorized dönmeli")
    void login_olmayalKullanici_401() {
        LoginRequest request = new LoginRequest("yok@portfolio.local", "portfolio123");

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl() + "/api/auth/login", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ---------------------------------------------------------------
    // Token Koruması Testleri
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Korumalı endpoint: Token olmadan — 401 Unauthorized dönmeli")
    void korumaliEndpoint_tokensiz_401() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl() + "/api/portfoy/ozet", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Korumalı endpoint: Geçersiz token — 401 Unauthorized dönmeli")
    void korumaliEndpoint_gecersizToken_401() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("bu.gecersiz.bir.token.degil");

        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/portfoy/ozet", HttpMethod.GET, entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ---------------------------------------------------------------
    // /me Endpoint Testleri
    // ---------------------------------------------------------------

    @Test
    @DisplayName("/me: Geçerli token ile kullanıcı bilgisi dönmeli")
    void me_gecerliToken_kullaniciBilgisi() {
        String token = loginVeTokenAl();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/auth/me", HttpMethod.GET, entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("success")).isEqualTo(true);

        Map data = (Map) body.get("data");
        assertThat(data).isNotNull();
        assertThat(data.get("email")).isEqualTo("investor@portfolio.local");
        assertThat(data.get("ad")).isNotNull();
    }

    @Test
    @DisplayName("/me: Token olmadan — 401 Unauthorized dönmeli")
    void me_tokensiz_401() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl() + "/api/auth/me", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ---------------------------------------------------------------
    // Hisse Listesi Erişim Testi (Auth ile)
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Hisseler: Auth token ile 20 hisse dönmeli")
    void hisseler_authToken_20HisseGelir() {
        String token = loginVeTokenAl();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/api/hisseler", HttpMethod.GET, entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("success")).isEqualTo(true);

        // Seed data: 20 hisse
        java.util.List<?> data = (java.util.List<?>) body.get("data");
        assertThat(data).isNotNull().hasSizeGreaterThanOrEqualTo(20);
    }

    // ---------------------------------------------------------------
    // Yardımcı Metotlar
    // ---------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private String loginVeTokenAl() {
        LoginRequest request = new LoginRequest("investor@portfolio.local", "portfolio123");
        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl() + "/api/auth/login", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map body = response.getBody();
        Map data = (Map) body.get("data");
        return (String) data.get("token");
    }
}
