package com.marketshop.marketshop.controller;

import com.marketshop.marketshop.entity.Item;
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
    WishlistService wishlistService;

    @PostMapping("/add")
    public ResponseEntity<WishlistResponse<String>> addwishlist(@RequestParam Long memberId, @RequestParam Long itemId) {
        String message = wishlistService.toggleWishlist(memberId, itemId);
        return new ResponseEntity<>(new WishlistResponse<>(message, null), HttpStatus.OK);
    }
    @GetMapping("/user/{memberId}")
    public ResponseEntity<WishlistResponse<List<Item>>> getuserwishlist(@PathVariable Long memberId){
        List<Item> wishlist = wishlistService.getWishllist(memberId);
        System.out.println(wishlistService.getWishllist(4L));
        return new ResponseEntity<>(new WishlistResponse<>( "찜 목록", wishlist), HttpStatus.OK);
    }

}
