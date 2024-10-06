package com.marketshop.marketshop.controller;

import com.marketshop.marketshop.dto.OrderDto;
import com.marketshop.marketshop.dto.OrderHistDto;
import com.marketshop.marketshop.service.OrderService;
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
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 구매 PostMapping
    @PostMapping(value = "/order")
    public ResponseEntity<?> order(@RequestBody @Valid OrderDto orderDto, BindingResult bindingResult, Principal principal) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();

            for (FieldError fieldError : fieldErrors) {
                sb.append(fieldError.getDefaultMessage());
            }

            return new ResponseEntity<>(sb.toString(), HttpStatus.BAD_REQUEST);
        }

        String email = principal.getName();
        Long orderId;

        try {
            orderId = orderService.order(orderDto, email);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(orderId, HttpStatus.OK); // 성공시 주문 ID를 JSON으로 반환
    }

    // 구매 이력
    @GetMapping(value = {"/orders", "/orders/{page}"})
    public ResponseEntity<?> orderHist(@PathVariable("page") Optional<Integer> page, Principal principal) {

        Pageable pageable = PageRequest.of(page.orElse(0), 4); // 기본 페이지는 0, 4개씩 가져옴
        Page<OrderHistDto> orderHistDtoList = orderService.getOrderList(principal.getName(), pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("orders", orderHistDtoList.getContent()); // 주문 리스트
        response.put("page", pageable.getPageNumber()); // 현재 페이지
        response.put("totalPages", orderHistDtoList.getTotalPages()); // 총 페이지 수

        return new ResponseEntity<>(response, HttpStatus.OK); // 페이징 정보와 주문 데이터를 JSON으로 반환
    }

    // 구매 이력에서 주문 취소
    @PostMapping("/order/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable("orderId") Long orderId, Principal principal){

        if(!orderService.validateOrder(orderId, principal.getName())){
            return new ResponseEntity<>("주문 취소 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        orderService.cancelOrder(orderId);
        return new ResponseEntity<>(orderId, HttpStatus.OK); // 취소된 주문 ID를 JSON으로 반환
    }

}
