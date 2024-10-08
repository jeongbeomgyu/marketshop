package com.marketshop.marketshop.controller;

import com.marketshop.marketshop.dto.ItemFormDto;
import com.marketshop.marketshop.dto.ItemSearchDto;
import com.marketshop.marketshop.entity.Item;
import com.marketshop.marketshop.service.ItemService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    // 상품 등록 페이지 데이터
    @GetMapping("/admin/item/new")
    public ResponseEntity<ItemFormDto> getItemForm() {
        return ResponseEntity.ok(new ItemFormDto());
    }

    // 상품 등록
    @PostMapping("/admin/item/new")
    public ResponseEntity<?> createItem(
            @Valid @ModelAttribute ItemFormDto itemFormDto,
            BindingResult bindingResult,
            @RequestParam("itemImgFile") List<MultipartFile> itemImgFileList) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        if (itemImgFileList.isEmpty() || (itemImgFileList.get(0).isEmpty() && itemFormDto.getId() == null)) {
            return ResponseEntity.badRequest().body("첫번째 상품 이미지는 필수 입력 값입니다.");
        }

        try {
            itemService.saveItem(itemFormDto, itemImgFileList);
            return ResponseEntity.ok("상품이 성공적으로 등록되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("상품 등록 중 에러가 발생하였습니다.");
        }
    }

    // 상품 상세 조회
    @GetMapping("/admin/item/{itemId}")
    public ResponseEntity<?> getItemDetails(@PathVariable("itemId") Long itemId) {
        try {
            ItemFormDto itemFormDto = itemService.getItemDtl(itemId);
            return ResponseEntity.ok(itemFormDto);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("존재하지 않는 상품입니다.");
        }
    }

    // 상품 업데이트
    @PostMapping("/admin/item/{itemId}")
    public ResponseEntity<?> updateItem(
            @PathVariable("itemId") Long itemId,
            @Valid @ModelAttribute ItemFormDto itemFormDto,
            BindingResult bindingResult,
            @RequestParam(value = "itemImgFile", required = false) List<MultipartFile> itemImgFileList) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }


        if (itemId == null) {
            return ResponseEntity.badRequest().body("업데이트할 상품의 ID가 제공되지 않았습니다.");
        }

        itemFormDto.setId(itemId);

        // itemFormDto의 ID 값도 로그로 출력
        System.out.println("itemFormDto.getId() 값: " + itemFormDto.getId());

        // itemFormDto 값 확인
        System.out.println("ItemFormDto 값: " + itemFormDto);

        System.out.println("ItemImgId size : " + itemFormDto.getItemImgIds());

        // 이미지 파일 리스트가 null이거나 비어있는지 확인
        if (itemImgFileList == null || itemImgFileList.isEmpty()) {
            return ResponseEntity.badRequest().body("이미지 파일이 제공되지 않았습니다.");
        }

        // 첫 번째 이미지가 비어 있는지 확인
        if (itemImgFileList.get(0).isEmpty() && itemFormDto.getId() == null) {
            return ResponseEntity.badRequest().body("첫번째 상품 이미지는 필수 입력 값입니다.");
        }

        try {
            itemService.updateItem(itemFormDto, itemImgFileList);
            return ResponseEntity.ok("상품이 성공적으로 업데이트되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("상품 수정 중 에러가 발생하였습니다.");
        }
    }


    // 상품 관리
    @GetMapping({"/admin/items", "/admin/items/{page}"})
    public ResponseEntity<?> getItems(
            ItemSearchDto itemSearchDto,
            @PathVariable(value = "page", required = false) Optional<Integer> page) {

        Pageable pageable = PageRequest.of(page.orElse(0), 3);
        Page<Item> items = itemService.getAdminItemPage(itemSearchDto, pageable);

        return ResponseEntity.ok(Map.of(
                "items", items.getContent(),
                "totalPages", items.getTotalPages(),
                "totalElements", items.getTotalElements(),
                "currentPage", items.getNumber()
        ));
    }

    // 상품 상세 조회 페이지
    @GetMapping("/item/{itemId}")
    public ResponseEntity<?> getItemDetailsPage(@PathVariable("itemId") Long itemId) {
        try {
            ItemFormDto itemFormDto = itemService.getItemDtl(itemId);
            return ResponseEntity.ok(itemFormDto);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("존재하지 않는 상품입니다.");
        }
    }
}
