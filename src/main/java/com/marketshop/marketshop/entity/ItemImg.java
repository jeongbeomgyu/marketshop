package com.marketshop.marketshop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

// 아이템 이미지 구현 엔티티
@Entity
@Table(name = "item_img")
@Setter
@Getter
public class ItemImg {

    @Id
    @Column(name = "item_img_id")
    @GeneratedValue
    private Long id;

    private String imgName;     // 이미지 파일명

    private String oriImgName;  // 원본 이미지 파일명

    private String imgUrl;      // 이미지 조회 경로

    private String repimgYn;    // 대표 이미지 여부

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    @JsonIgnore
    private Item item;

    public void updateItemImg(String oriImgName, String imgName, String imgUrl) {
        this.oriImgName = oriImgName;
        this.imgName = imgName;
        this.imgUrl = imgUrl;
    }
    
    
    
}
