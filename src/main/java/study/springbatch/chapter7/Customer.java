package study.springbatch.chapter7;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
//@XmlRootElement
@Entity
@Table(name = "CUSTOMER")
public class Customer {

    @Id
    private Long id;

    @Column
    private String firstName;

    @Column
    private String middleInitial;

    @Column
    private String lastName;

    private String city;
    private String state;
    private String zipCode;
    private String address;

//    private String addressNumber;
//    private String street;
//
//    private List<Transaction> transactions;

//    @XmlElementWrapper(name = "transactions")
//    @XmlElement(name = "transaction")
//    public void setTransactions(List<Transaction> transactions) {
//        this.transactions = transactions;
//    }
}
