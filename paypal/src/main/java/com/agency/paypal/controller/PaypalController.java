package com.agency.paypal.controller;

import com.agency.paypal.PaypalApplication;
import com.agency.paypal.model.Order;
import com.agency.paypal.model.PayPalTransaction;
import com.agency.paypal.service.PaypalService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;

import java.time.LocalDateTime;

@Controller
public class PaypalController {

    @Autowired
    PaypalService service;

    public static final String SUCCESS_URL = "pay/success";
    public static final String CANCEL_URL = "pay/cancel";
    final static Logger log = Logger.getLogger(PaypalApplication.class.getName());

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @PostMapping("/pay")
    public ResponseEntity<?> payment(@RequestBody Order order) {
        try {
            Payment payment = service.createPayment(order.getPrice(), order.getDescription(), "http://localhost:9090/" + CANCEL_URL,
                    "http://localhost:9090/" + SUCCESS_URL);
            for(Links link:payment.getLinks()) {
                if(link.getRel().equals("approval_url")) {
                    log.info("Payment done with payment id " + payment.getId());
                    return new ResponseEntity<>(link.getHref(), HttpStatus.OK);
                }
            }

        } catch (PayPalRESTException e) {
            log.error("Payment failed. Something went wrong, try again later." );
            e.printStackTrace();
        }
        return new ResponseEntity<>("redirect:/", HttpStatus.OK);
    }

    @GetMapping(value = CANCEL_URL)
    public String cancelPay() {
        log.warn("Payment canceled.");
        return "redirect:http://localhost:4200/paypal-fail";
    }

    @GetMapping(value = SUCCESS_URL)
    public String successPay(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId) {
        try {
            Payment payment = service.executePayment(paymentId, payerId);
            System.out.println(payment.toJSON());
            if (payment.getState().equals("approved")) {
                log.info("Payment request is approved.");
                service.saveTransaction(
                        new PayPalTransaction(
                                payment.getId(),
                                payment.getPayer().getPayerInfo().getFirstName(),
                                payment.getPayer().getPayerInfo().getLastName(),
                                payment.getPayer().getPayerInfo().getEmail(),
                                Double.parseDouble(payment.getTransactions().get(0).getAmount().getTotal()),
                                payment.getTransactions().get(0).getDescription(),
                                payment.getTransactions().get(0).getAmount().getCurrency(),
                                payment.getTransactions().get(0).getPayee().getEmail(),
                                payment.getTransactions().get(0).getPayee().getFirstName() + " " + payment.getTransactions().get(0).getPayee().getLastName(),
                                LocalDateTime.now()));
                log.info("Transaction id is " + paymentId);
                return "redirect:http://localhost:4200/paypal-success";

            }
        } catch (PayPalRESTException e) {
            System.out.println(e.getMessage());
            log.error("Payment with id " + paymentId + " is failed. Transaction aborted.");
        }
        return "redirect:/";
    }

}