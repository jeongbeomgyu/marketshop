package com.marketshop.marketshop.repository;

import com.marketshop.marketshop.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findByMemberId(Long memberId);
    Wishlist findByMemberIdAndItemId(Long memberId, Long itemId);
    Wishlist findByItemId(Long itemId);

}
