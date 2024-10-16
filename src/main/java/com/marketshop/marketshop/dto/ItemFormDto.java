package com.marketshop.marketshop.dto;

// 상품 데이터 정보를 전달하는 DTO

import com.marketshop.marketshop.constant.ItemSellStatus;
import com.marketshop.marketshop.entity.Item;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ItemFormDto {

    private Long id;

    @NotBlank(message = "상품명은 필수 입력 값입니다.")
    private String itemNm;

    @NotNull(message = "가격은 필수 입력 값입니다.")
    private Integer price;

    @NotBlank(message = "이름은 필수 입력 값입니다.")
    private String itemDetail;

    @NotNull(message = "재고는 필수 입력 값입니다.")
    private Integer stockNumber;

    private ItemSellStatus itemSellStatus;

    private String itemCategory;

    private List<ItemImgDto> itemImgDtoList = new ArrayList<>();        // 상품 저장 후 수정할 때 상품 이미지 정보를 저장하는 리스트

    private List<Long> itemImgIds = new ArrayList<>();      // 상품 이미지 ID 를 저장하는 리스트

    private Long memberId;  // 판매자 ID 필드 추가


    private static ModelMapper modelMapper = new ModelMapper();

    public Item createItem() {
        return modelMapper.map(this, Item.class);   // modelMapper 를 이용하여 엔티티 객체와 DTO 객체 간의 데이터를 복사
    }                                                      // 복사한 객체를 반환해주는 메소드

    public static ItemFormDto of(Item item) {
        return modelMapper.map(item, ItemFormDto.class);
    }

    @Override
    public String toString() {
        return "ItemFormDto{" +
                "id=" + id +
                ", itemNm='" + itemNm + '\'' +
                ", price=" + price +
                ", itemDetail='" + itemDetail + '\'' +
                ", stockNumber=" + stockNumber +
                ", itemSellStatus=" + itemSellStatus +
                ", itemCategory='" + itemCategory + '\'' +
                ", itemImgDtoList=" + itemImgDtoList +
                ", itemImgIds=" + itemImgIds +
                ", memberId=" + memberId +   // memberId 추가
                '}';
    }

}
