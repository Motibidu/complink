package com.pcgear.complink.pcgear.PJH;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/home")
public class HomeController {

        @GetMapping("init")
        public ResponseEntity<String> home() {
                // Map<String, String> responseData = new HashMap<>();
                // responseData.put("message", "불러옴");
                return ResponseEntity.ok("불러옴sss");

        }
}
