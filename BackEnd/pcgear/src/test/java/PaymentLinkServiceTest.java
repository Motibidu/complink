import com.fasterxml.jackson.databind.ObjectMapper;
import com.pcgear.complink.pcgear.PcgearApplication;
import com.pcgear.complink.pcgear.Payment.PaymentLinkService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat; // assertion 라이브러리 (선택적)

@SpringBootTest(classes = PcgearApplication.class)
class PaymentLinkServiceTest {

        // 실제 WebClient.Builder와 ObjectMapper를 주입받음
        @Autowired
        private WebClient.Builder webClientBuilder;
        @Autowired
        private ObjectMapper objectMapper;

        private PaymentLinkService paymentLinkService;

        @Value("${portone.webhook-url}")
        String webhookUrl;

        @BeforeEach
        void setUp() {
                // 테스트 전 PaymentLinkService 인스턴스를 생성하고 주입받은 의존성들을 전달
                // 이 방법은 WebClient를 직접 주입받을 수 없을 때 유용합니다.
                this.paymentLinkService = new PaymentLinkService(webClientBuilder, objectMapper, webhookUrl);
        }

        @Test
        void createPaymentLink_shouldReturnShortenedUrl() {
                String merchantUid = "test_order_" + System.currentTimeMillis();
                int amount = 1000;
                String productName = "테스트 상품";
                String buyerTel = "01012345678";
                String shortenedUrl = paymentLinkService.createPaymentLink(merchantUid, amount, productName, buyerTel);

                System.out.println("생성된 결제 URL: " + shortenedUrl);

                // URL이 null이 아니고 특정 형식(예: https://impay.link/ 로 시작)을 따르는지 확인
                assertThat(shortenedUrl).isNotNull();
                assertThat(shortenedUrl).startsWith("https://impay.link/");
                // 필요하다면 URL의 길이, 특정 문자열 포함 여부 등 추가적인 검증도 가능합니다.
        }

        // 에러 상황을 테스트하고 싶다면 추가적인 테스트 메서드를 작성합니다.
        @Test
        void createPaymentLink_shouldHandleError() {
                // 예를 들어, userCode를 잘못 설정하거나, 존재하지 않는 PG사를 사용하는 경우 등
                // 이 테스트는 실제로 실패할 수 있습니다.
                // PaymentLinkService badService = new PaymentLinkService(webClientBuilder,
                // objectMapper, "wrong_user_code");
                // Mono<String> errorMono = badService.createPaymentLink(..., ..., ..., ...);

                // StepVerifier.create(errorMono)
                // .expectError(RuntimeException.class)
                // .verify();
        }
}
