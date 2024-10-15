package com.marketshop.marketshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.marketshop.marketshop.constant.ItemSellStatus;
import com.marketshop.marketshop.dto.ItemFormDto;
import com.marketshop.marketshop.exception.OutOfStockException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "item")
@Setter
@Getter
@ToString
public class Item extends BaseEntity{

    @Id
    @Column(name = "item_id")
    @GeneratedValue
    private Long id;        // 상품 코드

    @Column(nullable = false, length = 50)
    private String itemNm;  // 상품명

    @Column(name = "price", nullable = false)
    private int price;      // 상품 가격

    @Column(nullable = false)
    private int stockNumber;    // 상품 재고 수량

    @Lob
    @Column(nullable = false)
    private String itemDetail;  // 상품 상세 설명

    @Enumerated(EnumType.STRING)
    private ItemSellStatus itemSellStatus;  // 상품 판매 상태

    @Column(nullable = false)
    private String itemCategory;    // 상품 카테고리

    private LocalDateTime regTime;

    @ManyToOne(fetch = FetchType.LAZY) // 어떤 멤버가 만들었는지 (판매자)
    @JoinColumn(name = "member_id")
    @JsonIgnore
    private Member member;

    @Column(name = "wishlist_count", nullable = false)
    private int wishlistCount = 0;


    public void updateItem(ItemFormDto itemFormDto){
        this.itemNm = itemFormDto.getItemNm();
        this.price = itemFormDto.getPrice();
        this.stockNumber = itemFormDto.getStockNumber();
        this.itemDetail = itemFormDto.getItemDetail();
        this.itemSellStatus = itemFormDto.getItemSellStatus();
    }

    public void removeStock(int stockNumber) {
        int restStock = this.stockNumber - stockNumber;
        if(restStock < 0) {
            throw new OutOfStockException("상품의 재고가 부족합니다." + "(현재 재고 수량: " + this.stockNumber + ")");
        }
        this.stockNumber = restStock;
    }

    public void addToWishlist() {
        this.wishlistCount++;
    }

    public void deleteToWishlist() {
        this.wishlistCount--;
    }


    public void addStock(int stockNumber) {
        this.stockNumber += stockNumber;
    }

    @JsonIgnore
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Wishlist> wishlistItems = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemImg> productThumbnails = new ArrayList<>();
}
