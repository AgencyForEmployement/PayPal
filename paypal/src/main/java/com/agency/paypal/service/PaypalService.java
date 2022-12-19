package com.agency.paypal.service;

import com.agency.paypal.model.PayPalTransaction;
import com.agency.paypal.repository.PayPalRepository;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaypalService {

    @Autowired
    private APIContext apiContext;
    @Autowired
    private PayPalRepository repository;
    @Autowired
    private RestTemplate restTemplate;

    public Payment createPayment(
            Double total,
            String description,
            String cancelUrl,
            String successUrl) throws PayPalRESTException{
        Amount amount = new Amount();
        amount.setCurrency("EUR");
        total = new BigDecimal(total).setScale(2, RoundingMode.HALF_UP).doubleValue();
        System.out.println(total);
        amount.setTotal(total.toString());

        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(amount);

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        Payer payer = new Payer();
        payer.setPaymentMethod("PAYPAL");

        Payment payment = new Payment();
        payment.setIntent("SALE");
        payment.setPayer(payer);
        payment.setTransactions(transactions);
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(cancelUrl);
        redirectUrls.setReturnUrl(successUrl);
        payment.setRedirectUrls(redirectUrls);

        return payment.create(apiContext);
    }

    public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException{
        Payment payment = new Payment();
        payment.setId(paymentId);
        PaymentExecution paymentExecute = new PaymentExecution();
        paymentExecute.setPayerId(payerId);
        return payment.execute(apiContext, paymentExecute);
    }

    public void saveTransaction(PayPalTransaction t) {
        repository.save(t);
        restTemplate.postForObject("http://localhost:8081/paypal/save", t, String.class);
    }


}