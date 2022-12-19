package com.agency.paypal.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PayPalTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column
    private long id;
    @Column
    private String paymentId;
    @Column
    private String payerName;
    @Column
    private String payerLastname;
    @Column
    private String payerEmail;
    @Column
    private double amount;
    @Column
    private String currency;
    @Column
    private String payeeEmail;
    @Column
    private String payeeName;
    @Column
    private LocalDateTime dateAndTime;

    public PayPalTransaction(String paymentId, String payerName, String payerLastname, String payerEmail, double amount, String currency, String payeeEmail, String payeeName, LocalDateTime dateAndTime) {
        this.paymentId = paymentId;
        this.payerName = payerName;
        this.payerLastname = payerLastname;
        this.payerEmail = payerEmail;
        this.amount = amount;
        this.currency = currency;
        this.payeeEmail = payeeEmail;
        this.payeeName = payeeName;
        this.dateAndTime = dateAndTime;
    }
}
