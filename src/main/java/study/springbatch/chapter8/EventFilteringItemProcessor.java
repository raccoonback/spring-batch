package study.springbatch.chapter8;

import org.springframework.batch.item.ItemProcessor;

public class EventFilteringItemProcessor implements ItemProcessor<Customer, Customer> {
    @Override
    public Customer process(Customer customer) throws Exception {
        return Integer.parseInt(customer.getZip()) % 2 == 0 ? null : customer;
    }
}
