package com.marketshop.marketshop.controller;

import com.marketshop.marketshop.dto.ItemSearchDto;
import com.marketshop.marketshop.dto.MainItemDto;
import com.marketshop.marketshop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final ItemService itemService;

//    @GetMapping(value="/")
//    public String main(ItemSearchDto itemSearchDto, Optional<Integer> page, Model model) {
//        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 6);
//        Page<MainItemDto> items = itemService.getMainItemPage(itemSearchDto, pageable);
//        model.addAttribute("items", items);
//        model.addAttribute("itemSearchDto", itemSearchDto);
//        model.addAttribute("maxPage", 5);
//        return "main";
//    }
//
//    @GetMapping(value = "/search")
//    public String SearchItem(ItemSearchDto itemSearchDto, Optional<Integer> page, Model model) {
//        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 6);
//        Page<MainItemDto> items = itemService.getMainItemPage(itemSearchDto, pageable);
//        model.addAttribute("items", items);
//        model.addAttribute("itemSearchDto", itemSearchDto);
//        model.addAttribute("maxPage", 5);
//        return "item/itemSearchForm";
//    }

    // 메인 페이지 아이템 리스트를 반환하는 API
    @GetMapping(value="/") // 기존 "/" 경로 유지
    public ResponseEntity<?> getItems(ItemSearchDto itemSearchDto, @RequestParam(value = "page", required = false, defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, 6);
        Page<MainItemDto> items = itemService.getMainItemPage(itemSearchDto, pageable);

        // JSON 형식으로 items와 추가 정보를 반환
        return ResponseEntity.ok(Map.of(
                "items", items.getContent(),  // 페이지의 아이템 목록
                "currentPage", items.getNumber(), // 현재 페이지 번호
                "totalPages", items.getTotalPages(), // 전체 페이지 수
                "totalItems", items.getTotalElements() // 전체 아이템 수
        ));
    }

    // 검색 결과를 반환하는 API
    @GetMapping(value = "/search") // 기존 "/search" 경로 유지
    public ResponseEntity<?> searchItems(ItemSearchDto itemSearchDto, @RequestParam(value = "page", required = false, defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, 6);
        Page<MainItemDto> items = itemService.getMainItemPage(itemSearchDto, pageable);

        // JSON 형식으로 검색된 items와 추가 정보를 반환
        return ResponseEntity.ok(Map.of(
                "items", items.getContent(),  // 페이지의 아이템 목록
                "currentPage", items.getNumber(), // 현재 페이지 번호
                "totalPages", items.getTotalPages(), // 전체 페이지 수
                "totalItems", items.getTotalElements() // 전체 아이템 수
        ));
    }
}
