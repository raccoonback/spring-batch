package study.springbatch.chapter9;

import org.springframework.stereotype.Service;

@Service
public class CustomerService {
    public void logCustomer(Customer customer) {
        System.out.println("I just saved " + customer);
    }

    public void logCustomerAddress(String address, String city, String state, String zipCode) {
        System.out.println(
                String.format("I just saved the address:\n%s\n%s, %s\n%s", address, city, state, zipCode)
        );
    }
}
