package com.pcgear.complink.pcgear.PJH.Manager;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pcgear.complink.pcgear.PJH.Order.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "담당자 API", description = "담당자 정보를 관리하는 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/managers")
public class ManagerController {

        private final ManagerService managerService;

        @Operation(summary = "담당자 목록 조회")
        @GetMapping
        public ResponseEntity<List<Manager>> getAllManagers() {
                List<Manager> managers = managerService.readManagers();
                return ResponseEntity.ok(managers);
        }

        @Operation(summary = "신규 담당자 등록")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "담당자 생성 성공"),
                        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 / 입력값 검증 실패"),
        })
        @PostMapping
        public ResponseEntity<Manager> createManager(@Valid @RequestBody Manager manager) {
                Manager createdManager = managerService.createManager(manager);
                return ResponseEntity.status(HttpStatus.CREATED).body(createdManager);
        }

        @Operation(summary = "담당자 정보 수정")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "담당자 정보 수정 성공"),
                        @ApiResponse(responseCode = "404", description = "해당 ID의 담당자를 찾을 수 없음")
        })
        @PutMapping("/{managerId}")
        public ResponseEntity<String> updateManager(@PathVariable(name = "managerId") String managerId,
                        @Valid @RequestBody Manager manager) {
                managerService.updateManager(managerId, manager);
                return ResponseEntity.status(HttpStatus.OK).body("담당자 정보 수정이 성공적으로 완료되었습니다.");
        }

        @Operation(summary = "담당자 삭제")
        @ApiResponse(responseCode = "204", description = "담당자 삭제 성공")
        @DeleteMapping
        public ResponseEntity<Void> deleteManagers(@RequestParam List<String> ids) {
                managerService.deleteManagers(ids);
                return ResponseEntity.noContent().build();
        }

}
