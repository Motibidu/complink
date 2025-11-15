package com.pcgear.complink.pcgear.Item;

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
import lombok.extern.slf4j.Slf4j;

@Tag(name = "품목 API", description = "품목 정보를 관리하는 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
@Slf4j
public class ItemController {

        private final ItemService itemService;

        @Operation(summary = "품목 목록 조회")
        @GetMapping
        public ResponseEntity<ItemPageDto> getAllItems(
                        @RequestParam(name = "search", required = false) String search,
                        @PageableDefault(size = 10, sort = "itemId", direction = Sort.Direction.DESC) Pageable pageable) {

                // search가 빈문자열, 공백, null이면 모두 null로 통일 시켜 같은 캐싱공간을 공유하도록 합니다.
                String effectiveSearchTerm = (search != null && !search.isBlank()) ? search : null;
                ItemPageDto itemPageDto = itemService.getAllItems(effectiveSearchTerm, pageable);
                return ResponseEntity.ok(itemPageDto);
        }

        @GetMapping("/temps")
        public ResponseEntity<List<Item>> readTempItems() {
                List<Item> tmpItems = itemService.findFirstThreeItems();

                return ResponseEntity.ok(tmpItems);
        }

        @Operation(summary = "신규 품목 등록")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "품목 생성 성공"),
                        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 / 입력값 검증 실패"),
        })
        @PostMapping
        public ResponseEntity<Item> createItem(@Valid @RequestBody Item item) {
                Item createdItem = itemService.createItem(item);
                return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
        }

        @Operation(summary = "품목 정보 수정")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "품목 정보 수정 성공"),
                        @ApiResponse(responseCode = "404", description = "해당 ID의 품목을 찾을 수 없음")
        })
        @PutMapping("/{itemId}")
        public ResponseEntity<String> updateItem(@PathVariable(name = "itemId") Integer itemId,
                        @Valid @RequestBody Item item) {
                itemService.updateItem(itemId, item);
                return ResponseEntity.status(HttpStatus.OK).body("품목 정보 수정이 성공적으로 완료되었습니다.");
        }

        @Operation(summary = "품목 삭제")
        @ApiResponse(responseCode = "204", description = "품목 삭제 성공")
        @DeleteMapping
        public ResponseEntity<Void> deleteItems(@RequestParam(name = "ids") List<Integer> ids) {
                itemService.deleteItems(ids);
                return ResponseEntity.noContent().build();
        }

}
