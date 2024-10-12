package com.marketshop.marketshop.service;

import ch.qos.logback.core.util.StringUtil;
import com.marketshop.marketshop.entity.ItemImg;
import com.marketshop.marketshop.repository.ItemImgRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

import java.io.File;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemImgService {

    // @Value 어노테이션을 통해 application.properties 파일에 등록한 itemImgLocation 값을 불러와서
    // ItemImgLocation 변수에 넣어줌
    @Value("${itemImgLocation}")
    private String itemImgLocation;

    private final ItemImgRepository itemImgRepository;

    private final FileService fileService;

    public ItemImg saveItemImg(ItemImg itemImg, MultipartFile itemImgFile) throws Exception {
        String oriImgName = itemImgFile.getOriginalFilename();
        String imgName = "";
        String imgUrl = "";

        // 파일 업로드
        if (!StringUtils.isEmpty(oriImgName)) {
            //실제 파일 저장 경로 생성
            String uploadDir = itemImgLocation+ File.separator + "items";
            File dir = new File(uploadDir);

            //폴더 없으면 생성
            if(!dir.exists()){
                dir.mkdirs();
            }

            imgName = fileService.uploadFile(uploadDir, oriImgName, itemImgFile.getBytes());
            imgUrl = "/images/item/" + imgName;
        }

        // 상품 이미지 정보 저장
        itemImg.updateItemImg(oriImgName, imgName, imgUrl);
        return itemImgRepository.save(itemImg);
    }

    public void updateItemImg(Long itemImgId, MultipartFile itemImgFile) throws Exception {
        if (!itemImgFile.isEmpty()) {
            ItemImg saveItemImg = itemImgRepository.findById(itemImgId)
                    .orElseThrow(EntityNotFoundException::new);

            // 기존 이미지 파일 삭제
            if (!StringUtils.isEmpty(saveItemImg.getImgName())) {
                fileService.deleteFile(itemImgLocation + "/item/" + saveItemImg.getImgName());
            }

            // 파일 저장 경로 설정
            String uploadDir = itemImgLocation + File.separator + "item";
            File dir = new File(uploadDir);

            String oriImgName = itemImgFile.getOriginalFilename();
            String imgName = fileService.uploadFile(uploadDir, oriImgName, itemImgFile.getBytes());
            String imgUrl = "/images/item/" + imgName;

            saveItemImg.updateItemImg(oriImgName, imgName, imgUrl); // 변경된 상품 이미지 정보 세팅
        }
    }

    // 상품 삭제
    public void deleteItemImg(Long itemImgId) throws Exception {
        ItemImg itemImg = itemImgRepository.findById(itemImgId)
                .orElseThrow(EntityNotFoundException::new);

        // 기존 이미지 파일 삭제
        if (!StringUtils.isEmpty(itemImg.getImgName())) {
            fileService.deleteFile(itemImgLocation + "/items" + itemImg.getImgName());
        }

        itemImgRepository.delete(itemImg);
    }
}
