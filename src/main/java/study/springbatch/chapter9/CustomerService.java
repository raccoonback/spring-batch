package study.springbatch.chapter9;

import org.springframework.stereotype.Service;

@Service
public class CustomerService {
    public void logCustomer(Customer customer) {
        System.out.println("I just saved " + customer);
    }
}
