package com.marketshop.marketshop.controller;

import com.marketshop.marketshop.dto.ItemFormDto;
import com.marketshop.marketshop.dto.ItemSearchDto;
import com.marketshop.marketshop.entity.Item;
import com.marketshop.marketshop.entity.Member;
import com.marketshop.marketshop.service.ChatRoomService;
import com.marketshop.marketshop.service.ItemService;
import com.marketshop.marketshop.service.MemberService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private final MemberService memberService;
    private final ChatRoomService chatRoomService;



    // 상품 등록 페이지 데이터
    @GetMapping("/admin/item/new")
    public ResponseEntity<ItemFormDto> getItemForm() {
        return ResponseEntity.ok(new ItemFormDto());
    }

    // 상품 등록
    @PostMapping("/admin/item/new")
    public ResponseEntity<?> createItem(
            @Valid @ModelAttribute ItemFormDto itemFormDto,
            BindingResult bindingResult,
            @RequestParam("itemImgFile") List<MultipartFile> itemImgFileList,
            Principal principal) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        if (itemImgFileList.isEmpty() || (itemImgFileList.get(0).isEmpty() && itemFormDto.getId() == null)) {
            return ResponseEntity.badRequest().body("첫번째 상품 이미지는 필수 입력 값입니다.");
        }

        try {
            // 인증된 사용자의 이메일을 전달하여 아이템과 연관
            String memberEmail = principal.getName();
            itemService.saveItem(itemFormDto, itemImgFileList, memberEmail);
            return ResponseEntity.ok("상품이 성공적으로 등록되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("상품 등록 중 에러가 발생하였습니다.");
        }
    }

    // 상품 상세 조회
    @GetMapping("/admin/item/{itemId}")
    public ResponseEntity<?> getItemDetails(@PathVariable("itemId") Long itemId) {
        try {
            ItemFormDto itemFormDto = itemService.getItemDtl(itemId);
            Member seller = memberService.findMemberById(itemFormDto.getMemberId()); // 판매자 정보 조회
            itemFormDto.setMemberId(seller.getId()); // 판매자 이름을 추가로 세팅
            return ResponseEntity.ok(itemFormDto);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("존재하지 않는 상품입니다.");
        }
    }

    // 상품 업데이트
    @PostMapping("/admin/item/{itemId}")
    public ResponseEntity<?> updateItem(
            @PathVariable("itemId") Long itemId,
            @Valid @ModelAttribute ItemFormDto itemFormDto,
            BindingResult bindingResult,
            @RequestParam(value = "itemImgFile", required = false) List<MultipartFile> itemImgFileList) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }


        if (itemId == null) {
            return ResponseEntity.badRequest().body("업데이트할 상품의 ID가 제공되지 않았습니다.");
        }

        itemFormDto.setId(itemId);

        // itemFormDto의 ID 값도 로그로 출력
        System.out.println("itemFormDto.getId() 값: " + itemFormDto.getId());

        // itemFormDto 값 확인
        System.out.println("ItemFormDto 값: " + itemFormDto);

        System.out.println("ItemImgId size : " + itemFormDto.getItemImgIds());

        // 이미지 파일 리스트가 null이거나 비어있는지 확인
        if (itemImgFileList == null || itemImgFileList.isEmpty()) {
            return ResponseEntity.badRequest().body("이미지 파일이 제공되지 않았습니다.");
        }

        // 첫 번째 이미지가 비어 있는지 확인
        if (itemImgFileList.get(0).isEmpty() && itemFormDto.getId() == null) {
            return ResponseEntity.badRequest().body("첫번째 상품 이미지는 필수 입력 값입니다.");
        }

        try {
            itemService.updateItem(itemFormDto, itemImgFileList);
            return ResponseEntity.ok("상품이 성공적으로 업데이트되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("상품 수정 중 에러가 발생하였습니다.");
        }
    }


    // 상품 관리
    @GetMapping({"/admin/items", "/admin/items/{page}"})
    public ResponseEntity<?> getItems(
            ItemSearchDto itemSearchDto,
            @PathVariable(value = "page", required = false) Optional<Integer> page) {

        Pageable pageable = PageRequest.of(page.orElse(0), 3);
        Page<Item> items = itemService.getAdminItemPage(itemSearchDto, pageable);

        return ResponseEntity.ok(Map.of(
                "items", items.getContent(),
                "totalPages", items.getTotalPages(),
                "totalElements", items.getTotalElements(),
                "currentPage", items.getNumber()
        ));
    }

    // 상품 상세 조회 페이지
    @GetMapping("/item/{itemId}")
    public ResponseEntity<?> getItemDetailsPage(@PathVariable("itemId") Long itemId) {
        try {
            ItemFormDto itemFormDto = itemService.getItemDtl(itemId);
            return ResponseEntity.ok(itemFormDto);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("존재하지 않는 상품입니다.");
        }
    }

    // 상품 삭제
    @PostMapping("/admin/item/delete/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable("itemId") Long itemId) {
        try {
            itemService.deleteitem(itemId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    // 사용자별로 아이템 목록 조회
    @GetMapping("/user/items")
    public ResponseEntity<?> getUserItems(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        String userEmail = principal.getName(); // 로그인된 사용자 이메일 가져오기
        Member member = memberService.findByEmail(userEmail); // 사용자의 회원 정보 가져오기

        List<ItemFormDto> items = itemService.getItemsByUser(member.getId()); // 사용자 ID로 아이템 필터링
        return ResponseEntity.ok(items); // 사용자 아이템 목록 반환
    }
    @GetMapping("/seller")
    @ResponseBody
    public ResponseEntity<List<ItemFormDto>> getSellerItems(@RequestParam("memberId") Long memberId) {
        List<ItemFormDto> items = itemService.findItemsByMemberId(memberId); // 서비스에서 아이템 리스트 조회
        return ResponseEntity.ok(items); // 조회한 아이템 리스트를 JSON 형태로 반환
    }


}
