package com.cpt202.service;

import com.cpt202.dto.AttachedFileDTO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface FileService {

    AttachedFileDTO uploadFile(Long resourceId, MultipartFile file);

    List<AttachedFileDTO> getFilesByResourceId(Long resourceId);

    void deleteFile(Long fileId, Long resourceId);
}
