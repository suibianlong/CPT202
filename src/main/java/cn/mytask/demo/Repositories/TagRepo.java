package cn.mytask.demo.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cn.mytask.demo.Models.Tag;

@Repository
public interface TagRepo extends JpaRepository<Tag, Long> {

    List<Tag> findByTagName(String tagName);

    List<Tag> findByStatus(String status);
}