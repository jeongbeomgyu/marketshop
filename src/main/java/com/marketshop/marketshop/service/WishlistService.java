package com.marketshop.marketshop.service;

import com.marketshop.marketshop.entity.Item;
import com.marketshop.marketshop.entity.ItemImg;
import com.marketshop.marketshop.entity.Member;
import com.marketshop.marketshop.entity.Wishlist;
import com.marketshop.marketshop.repository.ItemImgRepository;
import com.marketshop.marketshop.repository.ItemRepository;
import com.marketshop.marketshop.repository.MemberRepository;
import com.marketshop.marketshop.repository.WishlistRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WishlistService {
    @Autowired
    private WishlistRepository wishlistRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private ItemImgRepository itemImgRepository;  // ItemImgRepository 추가

    @Transactional
    public String toggleWishlist(Long memberId, Long itemId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new RuntimeException());
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new RuntimeException("해당 상품을 찾을 수 없습니다"));

        // ItemImg 조회 (예를 들어, 첫 번째 대표 이미지를 조회)
        ItemImg itemImg = itemImgRepository.findByItemIdAndRepimgYn(itemId, "Y");

        Wishlist wishlist = wishlistRepository.findByMemberIdAndItemId(memberId, itemId);

        if (wishlist != null) {
            wishlistRepository.delete(wishlist);
            item.deleteToWishlist();
            return "지워짐";
        } else {
            Wishlist newWishlist = new Wishlist();
            newWishlist.setMember(member);
            newWishlist.setItem(item);
            newWishlist.setItemImg(itemImg);  // ItemImg 설정
            wishlistRepository.save(newWishlist);
            item.addToWishlist();
            return "저장됨";
        }
    }

    @Transactional
    public List<WishlistItemResponse> getWishlist(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new RuntimeException());
        List<Wishlist> wishlists = wishlistRepository.findByMemberId(memberId);

        return wishlists.stream()
                .map(wishlist -> {
                    Item item = wishlist.getItem();
                    ItemImg itemImg = wishlist.getItemImg();
                    return new WishlistItemResponse(item, itemImg);  // DTO로 변환
                })
                .collect(Collectors.toList());
    }
}