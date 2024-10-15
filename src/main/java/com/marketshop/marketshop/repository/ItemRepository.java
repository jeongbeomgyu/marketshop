package com.marketshop.marketshop.repository;

import com.marketshop.marketshop.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long>, QuerydslPredicateExecutor<Item>, ItemRepositoryCustom {
    List<Item> findByItemNm(String itemNm);
    List<Item> findByItemNmOrItemCategory(String itemNm, String itemCategory);
    List<Item> findByItemNmOrItemDetail(String itemNm, String itemDetail);  // 상품을 상품명과 상품 상세 설명을 OR 조건을 이용하여 조회하는 쿼리 메소드
    List<Item> findByPriceLessThan(Integer price);
    List<Item> findByPriceLessThanOrderByPriceDesc(Integer price);

    // 특정 사용자가 등록한 아이템 목록 조회
    List<Item> findByMemberId(Long memberId);

    // 고정된 Sql 이 아닌 동적쿼리 생성해야함 -> Querydsl 사용
    // Query 를 이용해 검색 처리 -> Querydsl 사용
    // @Query 어노테이션 안에 JPQL 로 작성한 쿼리문을 넣어줌
    @Query(value = "select i form Item i where i.itemDetail like %:itemDetail% order by i.price desc", nativeQuery = true)
    // 파라미터 @Param 어노테이션을 이용해 파라미터로 넘어온 값을 JPQL 에 들어갈 변수로 지정해줬음
    List<Item> findByItemDetail(@Param("itemDetail") String itemDetail);
}
