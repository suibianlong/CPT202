package cn.mytask.demo.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cn.mytask.demo.Models.CategoryTopic;

@Repository
public interface CategoryTopicRepo extends JpaRepository<CategoryTopic, Long> {

    List<CategoryTopic> findByCategoryTopic(String categoryTopic);

    List<CategoryTopic> findByStatus(String status);
}