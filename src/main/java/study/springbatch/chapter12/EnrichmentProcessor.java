package study.springbatch.chapter12;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.CircuitBreaker;
import org.springframework.retry.annotation.Recover;
import org.springframework.web.client.RestTemplate;

public class EnrichmentProcessor implements ItemProcessor<Foo, Foo> {

    @Autowired
    private RestTemplate restTemplate;

    @Recover
    public Foo fallback(Foo foo) {
        foo.setMessage("error");
        return foo;
    }

    @CircuitBreaker(maxAttempts = 1)
    @Override
    public Foo process(Foo foo) {
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "http://localhost:8080/enrich",
                HttpMethod.GET,
                null,
                String.class
        );

        foo.setMessage(responseEntity.getBody());
        return foo;
    }

}
