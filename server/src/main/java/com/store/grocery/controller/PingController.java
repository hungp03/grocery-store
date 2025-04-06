package com.store.grocery.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v2")
public class PingController {
    @GetMapping()
    public String ping(){
        return "Pong!";
    }
}
