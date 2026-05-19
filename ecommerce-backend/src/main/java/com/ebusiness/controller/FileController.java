package com.ebusiness.controller;

import com.ebusiness.common.ApiResponse;
import com.ebusiness.service.FileService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileController {
  private final FileService fileService;

  public FileController(FileService fileService) {
    this.fileService = fileService;
  }

  @PostMapping("/upload")
  public ApiResponse<String> upload(@RequestParam("file") MultipartFile file) {
    String url = fileService.storeImage(file);
    return ApiResponse.success(url);
  }
}
