package com.cpt202.controller;

import com.cpt202.dto.*;
import com.cpt202.service.FileService;
import com.cpt202.service.ResourceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ResourceController {

    private final ResourceService resourceService;
    private final FileService fileService;

    // ==================== 资源创建和更新接口 ====================
    // POST /api/resources - 创建新资源
    // 数据流：接收 JSON → Service 层处理 → 保存到数据库 → 返回创建的资源信息

    @PostMapping
    public ResponseEntity<ResourceResponseDTO> createResource(
            @Valid @RequestBody ResourceCreateDTO dto,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        ResourceResponseDTO resource = resourceService.createResource(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(resource);
    }

    // PUT /api/resources/{id} - 更新已存在的资源
    // 前提条件：资源状态必须是 DRAFT 或 REJECTED 才能编辑

    @PutMapping("/{id}")
    public ResponseEntity<ResourceResponseDTO> updateResource(
            @PathVariable Long id,
            @Valid @RequestBody ResourceCreateDTO dto,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        ResourceResponseDTO resource = resourceService.updateResource(id, userId, dto);
        return ResponseEntity.ok(resource);
    }

    // POST /api/resources/{id}/draft - 保存草稿（新建或更新）
    // 如果 resourceId 为 null，则创建新资源；否则更新现有资源

    @PostMapping("/{id}/draft")
    public ResponseEntity<ResourceResponseDTO> saveDraft(
            @PathVariable Long id,
            @RequestBody(required = false) ResourceCreateDTO dto,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        ResourceResponseDTO resource = resourceService.saveDraft(id, userId, dto);
        return ResponseEntity.ok(resource);
    }

    // ==================== 资源查询接口 ====================
    // GET /api/resources/my - 获取当前用户的所有资源列表
    // 可选参数 status：按状态筛选资源（DRAFT, PENDING_REVIEW, REJECTED, APPROVED, ARCHIVED）

    @GetMapping("/my")
    public ResponseEntity<List<ResourceResponseDTO>> getMyResources(
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<ResourceResponseDTO> resources = resourceService.getMyResources(userId, status);
        return ResponseEntity.ok(resources);
    }

    // GET /api/resources/{id} - 根据 ID 获取单个资源的详细信息
    // 返回内容包括：分类、标签、附件文件列表

    @GetMapping("/{id}")
    public ResponseEntity<ResourceResponseDTO> getResourceById(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        ResourceResponseDTO resource = resourceService.getResourceById(id, userId);
        return ResponseEntity.ok(resource);
    }

    // ==================== 资源提交审核接口 ====================
    // POST /api/resources/{id}/submit - 提交资源进行审核
    // 前提条件：必须至少上传 1 个附件文件才能提交

    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<ResourceResponseDTO>> submitForReview(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        ResourceResponseDTO resource = resourceService.submitForReview(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Submitted successfully, waiting for review", resource));
    }

    // ==================== 资源删除接口 ====================
    // DELETE /api/resources/{id} - 删除资源
    // 前提条件：只能删除状态为 DRAFT 的资源

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteResource(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        resourceService.deleteResource(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Deleted successfully", null));
    }

    // ==================== 分类和标签查询接口 ====================
    // GET /api/resources/categories - 获取所有可用的分类列表
    // 用于资源创建/编辑时的分类选择

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDTO>> getCategories() {
        List<CategoryDTO> categories = resourceService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    // GET /api/resources/tags - 获取所有可用的标签列表
    // 用于资源创建/编辑时的标签选择

    @GetMapping("/tags")
    public ResponseEntity<List<TagDTO>> getTags() {
        List<TagDTO> tags = resourceService.getAllTags();
        return ResponseEntity.ok(tags);
    }

    // ==================== 文件管理接口 ====================
    // POST /api/resources/{id}/upload - 上传附件文件到指定资源
    // 支持 multipart/form-data 格式，文件大小限制 50MB

    @PostMapping("/{id}/upload")
    public ResponseEntity<ApiResponse<AttachedFileDTO>> uploadFile(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        // 预留：用于后续权限校验，确保只有资源所有者才能上传文件
        @SuppressWarnings("unused")
        Long userId = getCurrentUserId(request);
        AttachedFileDTO attachedFile = fileService.uploadFile(id, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("File uploaded successfully", attachedFile));
    }

    // GET /api/resources/{id}/files - 获取指定资源的所有附件文件列表

    @GetMapping("/{id}/files")
    public ResponseEntity<List<AttachedFileDTO>> getFiles(
            @PathVariable Long id,
            HttpServletRequest request) {
        // 预留：用于后续权限校验
        @SuppressWarnings("unused")
        Long userId = getCurrentUserId(request);
        List<AttachedFileDTO> files = fileService.getFilesByResourceId(id);
        return ResponseEntity.ok(files);
    }

    // DELETE /api/resources/{id}/files/{fileId} - 删除指定资源的附件文件

    @DeleteMapping("/{id}/files/{fileId}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @PathVariable Long id,
            @PathVariable Long fileId,
            HttpServletRequest request) {
        // 预留：用于后续权限校验，确保只有资源所有者才能删除文件
        @SuppressWarnings("unused")
        Long userId = getCurrentUserId(request);
        fileService.deleteFile(fileId, id);
        return ResponseEntity.ok(ApiResponse.success("File deleted successfully", null));
    }

    // ==================== 工具方法 ====================
    // 从请求中获取当前用户 ID
    // 如果请求中没有 userId 属性，默认返回 1L（用于测试环境）

    private Long getCurrentUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        if (userId == null) {
            return 1L;
        }
        return (Long) userId;
    }
}
