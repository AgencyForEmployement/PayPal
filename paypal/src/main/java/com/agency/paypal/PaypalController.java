package com.agency.paypal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;

@Controller
public class PaypalController {

    @Autowired
    PaypalService service;

    public static final String SUCCESS_URL = "pay/success";
    public static final String CANCEL_URL = "pay/cancel";

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
                    return new ResponseEntity<>(link.getHref(), HttpStatus.OK);
                }
            }

        } catch (PayPalRESTException e) {

            e.printStackTrace();
        }
        return new ResponseEntity<>("redirect:/", HttpStatus.OK);
    }

    @GetMapping(value = CANCEL_URL)
    public String cancelPay() {
        return "redirect:http://localhost:4200/paypal-fail";
    }

    @GetMapping(value = SUCCESS_URL)
    public String successPay(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId) {
        try {
            Payment payment = service.executePayment(paymentId, payerId);
            System.out.println(payment.toJSON());
            if (payment.getState().equals("approved")) {
                return "redirect:http://localhost:4200/paypal-success";
            }
        } catch (PayPalRESTException e) {
            System.out.println(e.getMessage());
        }
        return "redirect:/";
    }

}