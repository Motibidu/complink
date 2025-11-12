package com.pcgear.complink.pcgear.Dashboard;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pcgear.complink.pcgear.Order.service.OrderService;
import com.pcgear.complink.pcgear.Sell.SellService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

        private final DashboardSerivce dashboardSerivce;

        @GetMapping("/today-summary")
        public ResponseEntity<TodaySummary> todaySummary() {
                TodaySummary todaySummary = dashboardSerivce.getTodaySummary();

                return ResponseEntity.ok(todaySummary);
        }

}
