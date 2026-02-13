package com.pcgear.complink.pcgear.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
public class SseEmitterManager {

    private static final long TIMEOUT = 60 * 60 * 1000L; // 1시간
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(TIMEOUT);

        emitters.add(emitter);
        emitter.onCompletion(() -> {
            emitters.remove(emitter);
            log.debug("SSE connection completed and removed");
        });
        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            log.debug("SSE connection timed out and removed");
        });
        emitter.onError(e -> {
            emitters.remove(emitter);
            log.debug("SSE connection error and removed: {}", e.getMessage());
        });

        // 연결 직후 더미 이벤트 전송 (프록시/브라우저 연결 유지용)
        try {
            emitter.send(SseEmitter.event().name("connect").data("connected"));
        } catch (IOException e) {
            emitters.remove(emitter);
            log.debug("Failed to send initial SSE event: {}", e.getMessage());
        }

        return emitter;
    }

    public void broadcast(String message) {
        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("notification").data(message));
            } catch (IOException e) {
                // Broken pipe나 연결 끊김은 정상적인 상황 (클라이언트가 연결을 닫음)
                log.debug("Client disconnected during broadcast: {}", e.getMessage());
                deadEmitters.add(emitter);
            } catch (Exception e) {
                // 기타 예상치 못한 에러만 경고로 로깅
                log.warn("Unexpected error during SSE broadcast: {}", e.getMessage());
                deadEmitters.add(emitter);
            }
        }

        // 끊어진 연결들을 일괄 제거
        emitters.removeAll(deadEmitters);

        if (!deadEmitters.isEmpty()) {
            log.debug("Removed {} disconnected SSE clients", deadEmitters.size());
        }
    }
}
