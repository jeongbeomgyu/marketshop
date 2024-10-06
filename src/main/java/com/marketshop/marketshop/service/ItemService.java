package com.marketshop.marketshop.service;

import com.marketshop.marketshop.dto.ItemFormDto;
import com.marketshop.marketshop.dto.ItemImgDto;
import com.marketshop.marketshop.dto.ItemSearchDto;
import com.marketshop.marketshop.dto.MainItemDto;
import com.marketshop.marketshop.entity.Item;
import com.marketshop.marketshop.entity.ItemImg;
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

@Service
@Transactional
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    private final ItemImgService itemImgService;

    private final ItemImgRepository itemImgRepository;

    public Long saveItem(ItemFormDto itemFormDto, List<MultipartFile> itemImgFileList) throws Exception {
        Item item = itemFormDto.createItem();
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

}
