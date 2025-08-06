package com.pcgear.complink.pcgear.PJH.IM.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/home")
public class HomeController {

        @GetMapping("init")
        public ResponseEntity<String> home() {
                return ResponseEntity.ok("불러옴");

        }
}
