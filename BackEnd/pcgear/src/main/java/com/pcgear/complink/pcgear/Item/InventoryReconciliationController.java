package com.pcgear.complink.pcgear.Item;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 재고 정합성 검증 API
 */
@RestController
@RequestMapping("/inventory/reconciliation")
@Tag(name = "Inventory Reconciliation", description = "재고 정합성 검증 API")
@RequiredArgsConstructor
@Slf4j
public class InventoryReconciliationController {

    private final InventoryReconciliationService reconciliationService;

    /**
     * 전체 재고 정합성 검증 (수동 실행)
     */
    @PostMapping("/all")
    @Operation(summary = "전체 재고 정합성 검증", description = "모든 품목의 재고 정합성을 검증하고 자동 수정합니다.")
    public ResponseEntity<String> reconcileAllInventory() {
        log.info("수동 전체 재고 정합성 검증 요청");
        reconciliationService.reconcileAllInventory();
        return ResponseEntity.ok("재고 정합성 검증이 완료되었습니다.");
    }

    /**
     * 특정 품목 재고 정합성 검증 (수동 실행)
     */
    @PostMapping("/{itemId}")
    @Operation(summary = "특정 품목 재고 정합성 검증", description = "특정 품목의 재고 정합성을 검증하고 자동 수정합니다.")
    public ResponseEntity<String> reconcileSingleItem(@PathVariable Integer itemId) {
        log.info("수동 단일 품목 재고 정합성 검증 요청. ItemId: {}", itemId);

        boolean fixed = reconciliationService.reconcileSingleItem(itemId);

        if (fixed) {
            return ResponseEntity.ok("재고 불일치가 발견되어 수정되었습니다.");
        } else {
            return ResponseEntity.ok("재고 정합성이 정상입니다.");
        }
    }

    /**
     * 특정 품목의 감사 로그 조회
     */
    @GetMapping("/audit/{itemId}")
    @Operation(summary = "품목별 감사 로그 조회", description = "특정 품목의 재고 정합성 검증 이력을 조회합니다.")
    public ResponseEntity<List<InventoryAudit>> getAuditLogs(@PathVariable Integer itemId) {
        List<InventoryAudit> auditLogs = reconciliationService.getAuditLogs(itemId);
        return ResponseEntity.ok(auditLogs);
    }
}
