package com.pcgear.complink.pcgear.SMS;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.pcgear.complink.pcgear.Order.model.OrderStatus;
import com.pcgear.complink.pcgear.Order.service.OrderService;

import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;

@Slf4j
@Service
public class SmsService {

        private final DefaultMessageService messageService;
        private final OrderService orderService;

        public SmsService(DefaultMessageService messageService, OrderService orderService) {
                this.messageService = messageService;
                this.orderService = orderService;
        }

        public void sendPaymentLinkAndUpdateToReady(SendOneRequestDto sendOneRequest) {
                log.info("sendOneRequest: {}", sendOneRequest);

                String text = buildPaymentSmsText(sendOneRequest);
                String toPhoneNumber = "01062301825";

                SingleMessageSentResponse response = sendSms(toPhoneNumber, text);

                log.info("SMS 발송 완료. Response: {}", response);
                orderService.updateOrderStatus(sendOneRequest.getOrderId(), OrderStatus.PAYMENT_PENDING);

        }

        private SingleMessageSentResponse sendSms(String to, String text) {
                // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.
                Message message = new Message();
                message.setFrom("01062301825"); // 설정에서 주입받은 발신 번호 사용
                message.setTo(to);
                message.setText(text);

                return messageService.sendOne(new SingleMessageSendingRequest(message));
        }

        private String buildPaymentSmsText(SendOneRequestDto request) {
                return String.format(
                                "[PCGear] 결제를 요청\n" + "결제 요청 금액: %s원\n" + "결제링크: %s\n",
                                request.getGrandAmount(),
                                request.getPaymentLink());
        }

}
