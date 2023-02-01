package com.agency.paypal.controller;

import com.agency.paypal.PaypalApplication;
import com.agency.paypal.dto.PayPalPaymentDTO;
import com.agency.paypal.model.Order;
import com.agency.paypal.model.PayPalTransaction;
import com.agency.paypal.service.PaypalService;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Controller
public class PaypalController {

    @Autowired
    PaypalService service;
    @Autowired
    RestTemplate restTemplate;
    public static final String SUCCESS_URL = "pay/success";
    public static final String CANCEL_URL = "pay/cancel";
    final static Logger log = Logger.getLogger(PaypalApplication.class.getName());

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @PostMapping("/pay")
    public ResponseEntity<PayPalPaymentDTO> payment(@RequestBody Order order) {
        try {
            Payment payment = service.createPayment(order.getPrice(), order.getDescription(), "http://localhost:9090/" + CANCEL_URL,
                    "http://localhost:9090/" + SUCCESS_URL);
            System.out.println(payment.toJSON());
            PayPalTransaction t = new PayPalTransaction(
                    payment.getId(),
                    "",
                    "",
                    "",
                    Double.parseDouble(payment.getTransactions().get(0).getAmount().getTotal()),
                    payment.getTransactions().get(0).getDescription(),
                    payment.getTransactions().get(0).getAmount().getCurrency(),
                    "",
                    "",
                    LocalDateTime.now(),
                    payment.getState());
            service.saveTransaction(t);
            for(Links link:payment.getLinks()) {
                if(link.getRel().equals("approval_url")) {
                    log.info("Payment done with payment id " + payment.getId());
                    return new ResponseEntity<>(new PayPalPaymentDTO(payment.getId(), link.getHref(), payment.getState()), HttpStatus.OK);
                    //return new ResponseEntity<>(link.getHref(), HttpStatus.OK);
                }
            }

        } catch (PayPalRESTException e) {
            log.error("Payment failed. Something went wrong, try again later." );
            e.printStackTrace();
        }
        return new ResponseEntity<>(new PayPalPaymentDTO("", "", ""), HttpStatus.OK);
    }

    @GetMapping(value = CANCEL_URL)
    public String cancelPay() {
        log.warn("Payment canceled.");
        return "redirect:http://localhost:4201/paypal-fail";
    }

    @GetMapping(value = SUCCESS_URL)
    public String successPay(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId) {
        try {
            Payment payment = service.executePayment(paymentId, payerId);
            System.out.println(payment.toJSON());
            if (payment.getState().equals("approved")) {
                log.info("Payment request is approved.");
                PayPalTransaction t = service.findByPaymentId(payment.getId());
                t.setPayerName(payment.getPayer().getPayerInfo().getFirstName());
                t.setPayerLastname(payment.getPayer().getPayerInfo().getLastName());
                t.setPayerEmail(payment.getPayer().getPayerInfo().getEmail());
                t.setPayeeEmail(payment.getTransactions().get(0).getPayee().getEmail());
                t.setPayeeName(payment.getTransactions().get(0).getPayee().getFirstName() + " " + payment.getTransactions().get(0).getPayee().getLastName());
                t.setState(payment.getState());
                service.saveTransaction(t);
                log.info("Transaction id is " + paymentId);
                String res = restTemplate.postForObject("http://localhost:8081/paypal/update", t, String.class);
                return "redirect:http://localhost:4201/paypal-success";

            }
        } catch (PayPalRESTException e) {
            System.out.println(e.getMessage());
            log.error("Payment with id " + paymentId + " is failed. Transaction aborted.");
        }
        return "redirect:/";
    }

}