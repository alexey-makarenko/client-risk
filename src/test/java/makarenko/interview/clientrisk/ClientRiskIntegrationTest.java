package makarenko.interview.clientrisk;

import makarenko.interview.clientrisk.repository.RiskProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = ClientRiskApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ClientRiskIntegrationTest {

    private static final String LOCAL_HOST = "http://localhost:";
    private static final String REST_API_PATH = "/v1/risk/profiles";
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private RiskProfileRepository riskProfileRepository;
    @LocalServerPort
    private int port;

    private static <T> T sure(T val) {
        assertNotNull(val);
        return val;
    }

    @BeforeEach
    public void prepare() {
        riskProfileRepository.clear();
    }

    private String getBaseUrl() {
        return LOCAL_HOST + port + REST_API_PATH;
    }

    private String getClientUrl(Long id) {
        return LOCAL_HOST + port + REST_API_PATH + "/" + id;
    }

    @Test
    public void successCreateClients() {
        for (int i = 1; i <= 10; i++) {
            final Long id = assertCreateSuccess();
            final ResponseEntity<RiskProfile> result = sure(restValue(id));
            assertEquals(RiskProfile.NORMAL, result.getBody());
        }
    }

    @Test
    public void successDeleteClient() {
        final Long id = assertCreateSuccess();
        final ResponseEntity<String> result = restDelete(id);
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    public void successUpdateClient() {
        final Long id = assertCreateSuccess();
        final ResponseEntity<String> result = restUpdate(id, RiskProfile.LOW);
        assertEquals(HttpStatus.OK, result.getStatusCode());

        final ResponseEntity<RiskProfile> response = sure(restValue(id));
        assertEquals(RiskProfile.LOW, response.getBody());
    }

    @Test
    public void failNotExitClientUpdate() {
        final ResponseEntity<String> result = restUpdate(31337L, RiskProfile.LOW);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void failNotExitClientDelete() {
        final ResponseEntity<String> result = restDelete(31337L);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    private Long assertCreateSuccess() {
        ResponseEntity<Long> result = restCreate();
        assertNotNull(result);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        return result.getBody();
    }

    private ResponseEntity<Long> restCreate() {
        final String url = getBaseUrl() + "/create";
        return sure(restTemplate.exchange(
                url, HttpMethod.POST,
                null, Long.class));
    }

    private ResponseEntity<String> restDelete(Long id) {
        final String url = getClientUrl(id);
        return sure(restTemplate.exchange(
                url, HttpMethod.DELETE,
                null, String.class));
    }

    private ResponseEntity<RiskProfile> restValue(Long id) {
        final String url = getClientUrl(id) + "/value";
        return sure(restTemplate.exchange(
                url, HttpMethod.GET,
                null, RiskProfile.class));
    }

    private ResponseEntity<String> restUpdate(Long id, RiskProfile riskProfile) {
        final String url = getClientUrl(id) + "/update";
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<RiskProfile> httpEntity = new HttpEntity<>(riskProfile, headers);
        return sure(restTemplate.exchange(
                url, HttpMethod.POST,
                httpEntity, String.class));
    }
}
