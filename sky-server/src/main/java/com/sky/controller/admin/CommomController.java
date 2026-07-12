package com.sky.controller.admin;


import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@RestController
@RequestMapping("/admin/common")
@Slf4j
@Api(tags ="通用接口")
public class CommomController {
    private static String FILE_UPLOAD_PATH = "D:\\upload\\";

    @PostMapping("/upload")
    @ApiOperation("文件上传")
    @ResponseBody
    public Result upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        File dir = new File(FILE_UPLOAD_PATH);
        if (!dir.exists() || !dir.isDirectory()) {
            boolean create = dir.mkdir();
            if (create) {
                log.info("创建文件成功{}", FILE_UPLOAD_PATH);
            } else {
                log.warn("创建文件失败或已存在{}", FILE_UPLOAD_PATH);
            }
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            return Result.error("文件名无效");
        }

        Path targetLocation = Paths.get(FILE_UPLOAD_PATH).resolve(originalFilename).normalize();
        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("文件上传成功: {}", originalFilename);

        } catch (Exception e) {
           log.error("文件上传失败: {}", originalFilename, e);
           return Result.error("文件上传失败");
        }
        String fileurl = "http://localhost:8080/static/" + originalFilename;
        return Result.success(fileurl);

    }
}
