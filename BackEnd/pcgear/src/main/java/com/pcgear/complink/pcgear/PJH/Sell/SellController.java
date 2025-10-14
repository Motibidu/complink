package com.pcgear.complink.pcgear.PJH.Sell;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "판매서 API", description = "판매서를 관리하는 API")
@RestController
@RequestMapping("/sells")
@RequiredArgsConstructor
@Slf4j
public class SellController {

    private final SellService sellService;

    // @PostMapping
    // public ResponseEntity<Sell> createSell(@RequestBody Sell sell) {
    // Sell createdSell = sellService.createSell(sell);
    // return ResponseEntity.status(HttpStatus.CREATED).body(createdSell);
    // }

    @GetMapping
    public ResponseEntity<List<Sell>> readSells() {
        List<Sell> sells = sellService.readSells();
        return ResponseEntity.status(HttpStatus.CREATED).body(sells);
    }
}
