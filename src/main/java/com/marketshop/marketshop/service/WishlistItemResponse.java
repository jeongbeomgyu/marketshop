package com.marketshop.marketshop.service;

import com.marketshop.marketshop.entity.Item;
import com.marketshop.marketshop.entity.ItemImg;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WishlistItemResponse {

    private Long itemId;      // 아이템 ID
    private String itemName;  // 아이템 이름
    private String imgUrl;    // 이미지 URL
    private int price; //가격

    public WishlistItemResponse(Item item, ItemImg itemImg) {
        this.itemId = item.getId();                 // 아이템 ID 설정
        this.itemName = item.getItemNm();           // 아이템 이름 설정
        this.price = item.getPrice();
        this.imgUrl = (itemImg != null) ? itemImg.getImgUrl() : null; // 이미지 URL 설정
    }

}
