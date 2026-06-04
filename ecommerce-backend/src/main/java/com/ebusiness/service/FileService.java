package com.ebusiness.service;

import com.ebusiness.common.BusinessException;
import com.ebusiness.common.ErrorCode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

  @Value("${app.upload.dir}")
  private String uploadDir;

  public String storeImage(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new BusinessException(ErrorCode.INVALID_PARAM, "File is empty");
    }
    String original = file.getOriginalFilename();
    String ext = getExtension(original);
    if (!isAllowedImage(ext)) {
      throw new BusinessException(ErrorCode.INVALID_PARAM, "Unsupported file type");
    }

    String datePath = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    String name = UUID.randomUUID().toString().replace("-", "") + "." + ext;
    Path dir = Paths.get(uploadDir, datePath).toAbsolutePath().normalize();
    Path target = dir.resolve(name);

    try {
      Files.createDirectories(dir);
      Files.copy(file.getInputStream(), target);
    } catch (IOException ex) {
      throw new BusinessException(ErrorCode.SERVER_ERROR, "Upload failed");
    }

    return "/uploads/" + datePath + "/" + name;
  }

  private String getExtension(String filename) {
    if (filename == null || !filename.contains(".")) {
      return "";
    }
    return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
  }

  private boolean isAllowedImage(String ext) {
    return "jpg".equals(ext) || "jpeg".equals(ext) || "png".equals(ext)
        || "gif".equals(ext) || "webp".equals(ext)
        || "mp4".equals(ext) || "webm".equals(ext) || "mov".equals(ext);
  }
}
