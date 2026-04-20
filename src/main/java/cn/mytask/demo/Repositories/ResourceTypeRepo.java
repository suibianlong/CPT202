package cn.mytask.demo.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cn.mytask.demo.Models.ResourceType;

@Repository
public interface ResourceTypeRepo extends JpaRepository<ResourceType, Long> {

    List<ResourceType> findByTypeName(String typeName);

    List<ResourceType> findByStatus(String status);
}