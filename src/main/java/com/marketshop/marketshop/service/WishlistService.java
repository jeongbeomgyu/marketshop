package com.marketshop.marketshop.service;

import com.marketshop.marketshop.entity.Item;
import com.marketshop.marketshop.entity.Member;
import com.marketshop.marketshop.entity.Wishlist;
import com.marketshop.marketshop.repository.ItemRepository;
import com.marketshop.marketshop.repository.MemberRepository;
import com.marketshop.marketshop.repository.WishlistRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
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

    @Transactional
    public String toggleWishlist(Long memberId, Long itemId){
        Member member = memberRepository.findById(memberId).orElseThrow(()-> new RuntimeException());
        Item item = itemRepository.findById(itemId).orElseThrow(()-> new RuntimeException("해당 상품을 찾을 수 없습니다"));

        Wishlist wishlist = wishlistRepository.findByMemberIdAndItemId(memberId, itemId);
        if(wishlist != null){
            wishlistRepository.delete(wishlist);
            item.deleteToWishlist();
            return "지워짐";
        }else {
            Wishlist newwishlist = new Wishlist();
            newwishlist.setMember(member);
            newwishlist.setItem(item);
            wishlistRepository.save(newwishlist);
            item.addToWishlist();
            return "저장됨";
        }

    }
    @Transactional
    public String deletewishlist(Long MemberId, Long itemId){
        Item item = itemRepository.findById(itemId).orElseThrow(()-> new RuntimeException("해당 상품을 찾을 수 없습니다"));
        Wishlist wishlist = wishlistRepository.findByItemId(itemId);
        if (wishlist != null){
            wishlistRepository.delete(wishlist);
            return "지워짐";
        }else {
            return "찜 목록에 없음";
        }
    }



    @Transactional
    public List<Item> getWishllist(Long memberId){
        Member member = memberRepository.findById(memberId).orElseThrow(()-> new RuntimeException());
        return wishlistRepository.findByMemberId(memberId).stream().map(Wishlist::getItem).collect(Collectors.toList());
    }
}
