CREATE  TABLE IF NOT EXISTS ACCOUNT_SUMMARY (
  id INT NOT NULL AUTO_INCREMENT ,
  account_number VARCHAR(10) NOT NULL ,
  current_balance DECIMAL(10,2) NOT NULL ,
  PRIMARY KEY (id) )
ENGINE = InnoDB;

CREATE  TABLE IF NOT EXISTS TRANSACTION (
  id INT NOT NULL AUTO_INCREMENT ,
  timestamp TIMESTAMP NOT NULL ,
  amount DECIMAL(8,2) NOT NULL ,
  account_summary_id INT NOT NULL ,
  PRIMARY KEY (id) ,
  INDEX fk_Transaction_Account_Summary (account_summary_id ASC) ,
  CONSTRAINT fk_Transaction_Account_Summary
    FOREIGN KEY (account_summary_id )
    REFERENCES ACCOUNT_SUMMARY (id )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


# CREATE TABLE CUSTOMER  (
#                            id BIGINT  NOT NULL PRIMARY KEY ,
#                            firstName VARCHAR(100) NOT NULL ,
#                            middleInitial VARCHAR(100),
#                            lastName VARCHAR(100) NOT NULL,
#                            address VARCHAR(100) NOT NULL,
#                            city VARCHAR(100) NOT NULL,
#                            state CHAR(100) NOT NULL,
#                            zipCode CHAR(100)
# );

# DELIMITER //
#
# CREATE PROCEDURE customer_list(IN cityOption CHAR(16))
# BEGIN
#     SELECT * FROM CUSTOMER
#     WHERE city = cityOption;
# END //
#
# DELIMITER ;