package com.pcgear.complink.pcgear.Customer;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "거래처 API", description = "거래처 정보를 관리하는 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/customers")
public class CustomerController {

        private final CustomerService customerService;

        @Operation(summary = "거래처 목록 조회")
        @GetMapping
        public ResponseEntity<Page<Customer>> getAllCustomers(
                        @RequestParam(name = "search", required = false) String search,
                        @PageableDefault(size = 15, sort = "customerId", direction = Sort.Direction.DESC) Pageable pageable) {
                Page<Customer> customerPage = customerService.getAllCustomers(search, pageable);
                return ResponseEntity.ok(customerPage);
        }

        @Operation(summary = "신규 거래처 등록")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "거래처 생성 성공"),
                        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 / 입력값 검증 실패"),
        })
        @PostMapping
        public ResponseEntity<Customer> createCustomer(@Valid @RequestBody Customer Customer) {
                Customer createdCustomer = customerService.createCustomer(Customer);
                return ResponseEntity.status(HttpStatus.CREATED).body(createdCustomer);
        }

        @Operation(summary = "거래처 정보 수정")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "거래처 정보 수정 성공"),
                        @ApiResponse(responseCode = "404", description = "해당 ID의 거래처를 찾을 수 없음")
        })
        @PutMapping("/{customerId}")
        public ResponseEntity<String> updateCustomer(@PathVariable(name = "customerId") String customerId,
                        @Valid @RequestBody Customer Customer) {
                customerService.updateCustomer(customerId, Customer);
                return ResponseEntity.status(HttpStatus.OK).body("거래처 정보 수정이 성공적으로 완료되었습니다.");
        }

        @Operation(summary = "거래처 삭제")
        @ApiResponses({
                        @ApiResponse(responseCode = "204", description = "거래처 삭제 성공"),
                        @ApiResponse(responseCode = "404", description = "삭제할 거래처를 찾을 수 없음"),
                        @ApiResponse(responseCode = "409", description = "주문서에 할당된 이력이 있어 삭제 불가")
        })
        @DeleteMapping
        public ResponseEntity<Void> deleteCustomers(@RequestParam(name = "ids") List<String> ids) {
                customerService.deleteCustomers(ids);
                return ResponseEntity.noContent().build();
        }

}
