package com.smartcampushelpdesk.helpdesk_api.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CampusHelpdeskController {

    @GetMapping("/test")
    public String getHelloWorld() {
        return "Hello World!";
    }

    @GetMapping("/dipender")
    public String getdivaPender() {
        return "it's me dipender";
    }

    @GetMapping("/aarti")
    public String getAarti() {
        return "it's me Aartiiiiiii";
    }
}