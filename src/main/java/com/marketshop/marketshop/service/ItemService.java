package com.marketshop.marketshop.service;

import com.marketshop.marketshop.constant.ItemSellStatus;
import com.marketshop.marketshop.dto.ItemFormDto;
import com.marketshop.marketshop.dto.ItemImgDto;
import com.marketshop.marketshop.dto.ItemSearchDto;
import com.marketshop.marketshop.dto.MainItemDto;
import com.marketshop.marketshop.entity.Item;
import com.marketshop.marketshop.entity.ItemImg;
import com.marketshop.marketshop.entity.Member;
import com.marketshop.marketshop.repository.ItemImgRepository;
import com.marketshop.marketshop.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    private final ItemImgService itemImgService;

    private final ItemImgRepository itemImgRepository;

    private final MemberService memberService;



    public Long saveItem(ItemFormDto itemFormDto, List<MultipartFile> itemImgFileList, String memberEmail) throws Exception {
        // 이메일로 인증된 회원을 가져옴
        Member member = memberService.findByEmail(memberEmail); // 회원을 직접 반환

        // 아이템을 생성하고 회원과 연관시킴
        Item item = itemFormDto.createItem();
        item.setMember(member); // 아이템에 해당 회원을 설정

        itemRepository.save(item);

        for (int i = 0; i < itemImgFileList.size(); i++) {
            MultipartFile itemImgFile = itemImgFileList.get(i);
            ItemImg itemImg = new ItemImg();
            itemImg.setItem(item);

            if(i == 0) {
                itemImg.setRepimgYn("Y");
            } else {
                itemImg.setRepimgYn("N");
            }

            // 아이템 이미지 저장 후 ID를 itemImgIds에 추가
            ItemImg savedItemImg = itemImgService.saveItemImg(itemImg, itemImgFile);
            itemFormDto.getItemImgIds().add(savedItemImg.getId());  // saveItemImg에서 반환된 ID 추가
        }

        if (item.getStockNumber() == 0) {
            item.setItemSellStatus(ItemSellStatus.SOLD_OUT);
        }
        itemRepository.save(item);

        return item.getId();
    }

    @Transactional(readOnly = true)
    public ItemFormDto getItemDtl(Long itemId) {
        List<ItemImg> itemImgList = itemImgRepository.findByItemIdOrderByIdAsc(itemId);
        List<ItemImgDto> itemImgDtoList = new ArrayList<>();
        List<Long> itemImgIds = new ArrayList<>();

        for (ItemImg itemImg : itemImgList) {
            ItemImgDto itemImgDto = ItemImgDto.of(itemImg);
            itemImgDtoList.add(itemImgDto);
            itemImgIds.add(itemImg.getId());
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(EntityNotFoundException::new);
        ItemFormDto itemFormDto = ItemFormDto.of(item);
        itemFormDto.setItemImgDtoList(itemImgDtoList);
        itemFormDto.setItemImgIds(itemImgIds);  // 설정

        return itemFormDto;
    }

    public Long updateItem(ItemFormDto itemFormDto, List<MultipartFile> itemImgFileList) throws Exception {

        if (itemFormDto.getId() == null) {
            throw new IllegalArgumentException("Item ID는 null일 수 없습니다.");
        }

        // itemImgIds를 강제로 설정
        List<Long> itemImgIds = itemFormDto.getItemImgIds();
        if (itemImgIds.isEmpty()) {
            List<ItemImg> itemImgList = itemImgRepository.findByItemIdOrderByIdAsc(itemFormDto.getId());
            for (ItemImg itemImg : itemImgList) {
                itemImgIds.add(itemImg.getId());
            }
            System.out.println("itemImgIds 설정 후 크기: " + itemImgIds.size());
        }

        System.out.println("itemFormDto 값: " + itemFormDto);
        System.out.println("itemImgFileList의 크기: " + itemImgFileList.size());
        System.out.println("itemImgIds의 크기: " + itemImgIds.size());

        if (itemImgIds.isEmpty() || itemImgIds.size() != itemImgFileList.size()) {
            throw new IllegalStateException("업데이트할 이미지 ID 리스트가 비어 있거나 이미지 파일 리스트와 일치하지 않습니다.");
        }

        Item item = itemRepository.findById(itemFormDto.getId())
                .orElseThrow(EntityNotFoundException::new);
        item.updateItem(itemFormDto);

        for (int i = 0; i < itemImgFileList.size(); i++) {
            itemImgService.updateItemImg(itemImgIds.get(i), itemImgFileList.get(i));
        }

        if (item.getStockNumber() == 0) {
            item.setItemSellStatus(ItemSellStatus.SOLD_OUT);
        }
        itemRepository.save(item);

        return item.getId();
    }

    @Transactional(readOnly = true)
    public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        return itemRepository.getAdminItemPage(itemSearchDto, pageable);
    }

    @Transactional(readOnly = true)
    public Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable){
        return itemRepository.getMainItemPage(itemSearchDto, pageable);
    }

    // 상품 삭제
    public void deleteitem(Long itemId) throws Exception {
        Item item = itemRepository.findById(itemId).orElseThrow(EntityNotFoundException::new);
        List<ItemImg> itemImgList = itemImgRepository.findByItemIdOrderByIdAsc(itemId);

        for (ItemImg itemImg : itemImgList) {
            itemImgService.deleteItemImg(itemImg.getId());
        }
        itemRepository.delete(item);
    }

    @Transactional(readOnly = true)
    public List<ItemFormDto> getItemsByUser(Long userId) {
        // 사용자의 아이템 목록을 조회
        List<Item> items = itemRepository.findByMemberId(userId);

        // ItemFormDto 목록으로 변환
        List<ItemFormDto> itemFormDtoList = new ArrayList<>();

        for (Item item : items) {
            // 아이템의 이미지 목록 조회
            List<ItemImg> itemImgList = itemImgRepository.findByItemIdOrderByIdAsc(item.getId());
            List<ItemImgDto> itemImgDtoList = new ArrayList<>();

            for (ItemImg itemImg : itemImgList) {
                // 이미지 정보 DTO로 변환
                itemImgDtoList.add(ItemImgDto.of(itemImg));
            }

            // 아이템 정보를 DTO로 변환
            ItemFormDto itemFormDto = ItemFormDto.of(item);
            itemFormDto.setItemImgDtoList(itemImgDtoList); // 이미지 정보 설정

            itemFormDtoList.add(itemFormDto);
        }

        return itemFormDtoList;
    }
    public List<ItemFormDto> findItemsByMemberId(Long memberId) {
        // memberId로 아이템을 조회
        List<Item> items = itemRepository.findByMemberId(memberId);
        // 조회한 아이템을 DTO로 변환
        return items.stream()
                .map(item -> {
                    ItemFormDto itemFormDto = ItemFormDto.of(item);
                    // 각 아이템의 이미지 정보를 DTO로 변환하여 리스트에 추가
                    List<ItemImgDto> itemImgDtos = item.getProductThumbnails()
                            .stream()
                            .map(ItemImgDto::of)
                            .collect(Collectors.toList());
                    itemFormDto.setItemImgDtoList(itemImgDtos);
                    return itemFormDto;
                })
                .collect(Collectors.toList());
    }

    public List<ItemFormDto> findSoldOutItemsByMemberId(Long memberId) {
        List<Item> soldOutItems = itemRepository.findByMemberIdAndItemSellStatus(memberId, ItemSellStatus.SOLD_OUT);

        return soldOutItems.stream()
                .map(item -> {
                    ItemFormDto itemFormDto = ItemFormDto.of(item); // 기본 정보를 매핑
                    // Item의 이미지 정보를 매핑
                    List<ItemImgDto> itemImgDtoList = item.getProductThumbnails().stream()
                            .map(ItemImgDto::of)
                            .collect(Collectors.toList());
                    itemFormDto.setItemImgDtoList(itemImgDtoList); // 이미지 리스트 설정
                    return itemFormDto;
                })
                .collect(Collectors.toList());
    }


}
