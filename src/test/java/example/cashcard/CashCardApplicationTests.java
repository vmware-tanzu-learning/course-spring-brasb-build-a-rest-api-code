package example.cashcard;

import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.annotation.DirtiesContext.ClassMode;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class CashCardApplicationTests {
    @Autowired
    TestRestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        restTemplate.withBasicAuth("sarah1", "abc123");
    }

    @Test
    @DirtiesContext
    void shouldReturnACashCardWhenDataIsSaved() {
        ResponseEntity<String> response = restTemplate.withBasicAuth("sarah1", "abc123")
                .getForEntity("/cashcards/99", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        Number id = documentContext.read("$.id");
        assertThat(id).isEqualTo(99);

        Double amount = documentContext.read("$.amount");
        assertThat(amount).isEqualTo(123.45);
    }

    @Test
    void shouldNotReturnACashCardWithAnUnknownId() {
        ResponseEntity<String> response = restTemplate.withBasicAuth("sarah1", "abc123")
                .getForEntity("/cashcards/1000", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isBlank();
    }

    @Test
    @DirtiesContext
    void shouldCreateANewCashCard() throws URISyntaxException {
        HttpHeaders headers = createClientCsrfHeaders();
        CashCard newCashCard = new CashCard(null, 250.00, "sarah1");
        var req = new RequestEntity<>(newCashCard, headers, HttpMethod.POST, new URI("/cashcards"));

        ResponseEntity<Void> createResponse = restTemplate.withBasicAuth("sarah1", "abc123")
                .postForEntity("/cashcards", req, Void.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI locationOfNewCashCard = createResponse.getHeaders().getLocation();
        ResponseEntity<String> getResponse = restTemplate.withBasicAuth("sarah1", "abc123")
                .getForEntity(locationOfNewCashCard, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
        Number id = documentContext.read("$.id");
        Double amount = documentContext.read("$.amount");

        assertThat(id).isNotNull();
        assertThat(amount).isEqualTo(250.00);
    }

    /**
     * Create the most basic CSRF headers (Cookie + X-XSRF-TOKEN) that spring-security would accept. An SPA
     * _can_ do this.
     */
    private HttpHeaders createClientCsrfHeaders() {
        var cookie = new HttpCookie("XSRF-TOKEN", "some-value");
        var headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, cookie.toString());
        headers.add("X-XSRF-TOKEN", cookie.getValue());
        return headers;
    }

    /**
     * This method creates the CSRF headers like angular would, by reading the cookie and then sending the
     * value back in both the cookie and the header.
     */
    private HttpHeaders createRealCsrfHeader() {
        ResponseEntity<Void> response = restTemplate.withBasicAuth("sarah1", "abc123")
                .exchange("/cashcards", HttpMethod.HEAD, null, Void.class);
        var cookie = HttpCookie.parse(
                        response.getHeaders().getFirst(HttpHeaders.SET_COOKIE)
                )
                .stream()
                .filter(c -> c.getName().equals("XSRF-TOKEN"))
                .findFirst()
                .get();

        var headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, cookie.toString());
        headers.add("X-XSRF-TOKEN", cookie.getValue());
        return headers;
    }

    @Test
    void shouldReturnAllCashCardsWhenListIsRequested() {
        ResponseEntity<String> response = restTemplate.withBasicAuth("sarah1", "abc123")
                .getForEntity("/cashcards", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        int cashCardCount = documentContext.read("$.length()");
        assertThat(cashCardCount).isEqualTo(3);

        JSONArray ids = documentContext.read("$..id");
        assertThat(ids).containsExactlyInAnyOrder(99, 100, 101);

        JSONArray amounts = documentContext.read("$..amount");
        assertThat(amounts).containsExactlyInAnyOrder(123.45, 1.00, 150.00);
    }

    @Test
    void shouldReturnAPageOfCashCards() {
        // With page size of 2, get the 2nd page. Sort by amount descending.
        ResponseEntity<String> response = restTemplate.withBasicAuth("sarah1", "abc123").
                getForEntity("/cashcards?page=1&size=2&sort=amount,desc", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray read = documentContext.read("$[*]");
        assertThat(read.size()).isEqualTo(1); // it's the last page so has fewer than page size

        int id = documentContext.read("$[0].id");
        assertThat(id).isEqualTo(100);

        double amount = documentContext.read("$[0].amount");
        assertThat(amount).isEqualTo(1.00);
    }
}
