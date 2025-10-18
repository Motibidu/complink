package com.pcgear.complink.pcgear.SMS;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/sms")
public class SmsController {

        private final SmsService smsService;

        @PostMapping("/send-one")
        public ResponseEntity<String> sendOne(@RequestBody SendOneRequestDto sendOneRequest) {
                smsService.sendPaymentLinkAndUpdateToReady(sendOneRequest);

                return ResponseEntity.ok("메시지가 성공적으로 전송되었습니다.");
        }
}
