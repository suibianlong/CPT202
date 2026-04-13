package com.cpt202.repository;

import com.cpt202.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByStatus(Category.CategoryStatus status);
    List<Category> findByCategoryType(String categoryType);
}
