package study.springbatch.chapter12;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EnrichmentController {

    private int count = 0;

    @GetMapping("/enrich")
    public String enrich() {
        count++;

        return String.format("Enriched %d", count);
    }
}
