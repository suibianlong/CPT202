package org.example.cpt202music.service;

import org.example.cpt202music.model.dto.AttachedFileDTO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface FileService {

    AttachedFileDTO uploadFile(Long resourceId, MultipartFile file);

    List<AttachedFileDTO> getFilesByResourceId(Long resourceId);

    void deleteFile(Long fileId, Long resourceId);
}
