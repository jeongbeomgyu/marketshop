package com.marketshop.marketshop.service;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WishlistResponse<T> {
    private String message;
    private T data;

    public WishlistResponse(String message, T data){
        this.message = message;
        this.data = data;
    }
}
