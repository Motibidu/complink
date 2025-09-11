package com.pcgear.complink.pcgear.PJH.Register.controller;

import org.springframework.web.bind.annotation.RestController;

import com.pcgear.complink.pcgear.PJH.Order.model.Customer;
import com.pcgear.complink.pcgear.PJH.Order.model.Manager;
import com.pcgear.complink.pcgear.PJH.Register.model.Item;
import com.pcgear.complink.pcgear.PJH.Register.service.RegisterService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "기초정보 등록 API", description = "거래처, 품목, 담당자 등 시스템 기초 정보를 관리하는 API")
@RestController
@RequestMapping("/registers")
@Slf4j
@RequiredArgsConstructor
public class RegisterController {

        private final RegisterService registerService;

        @Operation(summary = "신규 거래처 등록", description = "새로운 거래처 정보를 시스템에 등록합니다.")
        @ApiResponse(responseCode = "201", description = "거래처 등록 성공")
        @PostMapping("/customer")
        public ResponseEntity<String> registerCustomer(@RequestBody Customer customer) {
                registerService.registerCustomer(customer);
                return ResponseEntity.status(HttpStatus.CREATED).body("거래처 등록이 성공적으로 완료되었습니다.");
        }

        @ApiResponse(responseCode = "201", description = "품목 등록 성공")
        @PostMapping("/item")
        public ResponseEntity<String> registerItem(@Valid @RequestBody Item item) {
                registerService.registerItem(item);
                return ResponseEntity.status(HttpStatus.CREATED).body("품목 등록이 성공적으로 완료되었습니다.");
        }

        @ApiResponse(responseCode = "201", description = "담당자 등록 성공")
        @PostMapping("/manager")
        public ResponseEntity<String> registerManager(@RequestBody Manager manager) {
                registerService.registerManager(manager);
                return ResponseEntity.status(HttpStatus.CREATED).body("담당자 등록이 성공적으로 완료되었습니다.");
        }

        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "담당자 정보 수정 성공"),
                        @ApiResponse(responseCode = "404", description = "해당 ID의 담당자를 찾을 수 없음")
        })
        @PutMapping("/manager/{managerId}")
        public ResponseEntity<String> editManager(@PathVariable(name = "managerId") String managerId,
                        @Valid @RequestBody Manager manager) {
                registerService.editManager(managerId, manager);
                return ResponseEntity.status(HttpStatus.OK).body("담당자 정보 수정이 성공적으로 완료되었습니다.");
        }

        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "품목 정보 수정 성공"),
                        @ApiResponse(responseCode = "404", description = "해당 ID의 품목을 찾을 수 없음")
        })
        @PutMapping("/item/{itemId}")
        public ResponseEntity<String> editItem(@PathVariable(name = "itemId") Integer itemId,
                        @Valid @RequestBody Item item) {
                registerService.editItem(itemId, item);
                return ResponseEntity.status(HttpStatus.OK).body("품목 정보 수정이 성공적으로 완료되었습니다.");
        }

        @ApiResponse(responseCode = "204", description = "담당자 삭제 성공")
        @DeleteMapping("/managers")
        public ResponseEntity deleteManagers(@RequestBody Map<String, List<String>> managers) {
                log.info("managers: {}", managers);
                registerService.deleteManagers(managers.get("ids"));
                return ResponseEntity.noContent().build();
        }

        @ApiResponse(responseCode = "204", description = "품목 삭제 성공")
        @DeleteMapping("/items")
        public ResponseEntity deleteItems(@RequestBody Map<String, List<Integer>> items) {
                log.info("items: {}", items);
                registerService.deleteItems(items.get("ids"));
                return ResponseEntity.noContent().build();
        }

        @ApiResponse(responseCode = "204", description = "거래처 삭제 성공")
        @DeleteMapping("/customers")
        public ResponseEntity deleteCustomers(@RequestBody Map<String, List<String>> customers) {
                log.info("customers: {}", customers);
                registerService.deleteCustomers(customers.get("ids"));
                return ResponseEntity.noContent().build();
        }

}
