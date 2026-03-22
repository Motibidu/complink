package com.pcgear.complink.pcgear.Dashboard;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

        private final DashboardService dashboardSerivce;
        private final TopItemsCacheService topItemsCacheService;

        @GetMapping("/today-summary")
        public ResponseEntity<TodaySummary> todaySummary() {
                TodaySummary todaySummary = dashboardSerivce.getTodaySummary();

                return ResponseEntity.ok(todaySummary);
        }

        @GetMapping("/refresh-cache")
        public ResponseEntity<String> refreshCache() {
                topItemsCacheService.manualRefresh();
                return ResponseEntity.ok("TOP 10 items cache refreshed successfully");
        }

}
