package com.hstk.iot_warehouse.controller;

import com.hstk.iot_warehouse.common.api.Result;
import com.hstk.iot_warehouse.component.OssComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/upload")
public class UploadController {

    @Autowired
    private OssComponent ossComponent;

    /**
     * 上传图片接口
     * @param file 文件对象
     * @return 文件的URL
     */
    @PostMapping("/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        log.info("Receiving image upload request: {}", file.getOriginalFilename());
        String url = ossComponent.uploadFile(file);
        return Result.success(url);
    }
}
