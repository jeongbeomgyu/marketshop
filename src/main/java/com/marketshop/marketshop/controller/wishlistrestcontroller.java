package com.marketshop.marketshop.controller;

import com.marketshop.marketshop.service.WishlistItemResponse;
import com.marketshop.marketshop.service.WishlistResponse;
import com.marketshop.marketshop.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wishlist")
public class wishlistrestcontroller {

    @Autowired
    private WishlistService wishlistService;

    // 찜 목록에 아이템 추가/제거
    @PostMapping("/add")
    public ResponseEntity<WishlistResponse<String>> addWishlist(
            @RequestParam("memberId") Long memberId,
            @RequestParam("itemId") Long itemId) {
        String message = wishlistService.toggleWishlist(memberId, itemId);
        return new ResponseEntity<>(new WishlistResponse<>(message, null), HttpStatus.OK);
    }

    // 특정 회원의 찜 목록 조회
    @GetMapping("/user")
    public ResponseEntity<WishlistResponse<List<WishlistItemResponse>>> getUserWishlist(@RequestParam("memberId") Long memberId) {
        List<WishlistItemResponse> wishlist = wishlistService.getWishlist(memberId);
        return new ResponseEntity<>(new WishlistResponse<>("찜 목록", wishlist), HttpStatus.OK);
    }
}
