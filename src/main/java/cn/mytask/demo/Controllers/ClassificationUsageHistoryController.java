package cn.mytask.demo.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.mytask.demo.Models.ClassificationUsageHistoryView;

@RestController
public class ClassificationUsageHistoryController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/classificationUsageHistory")
    public List<ClassificationUsageHistoryView> getClassificationUsageHistory() {
        String sql = """
            SELECT 
                rt.typeName AS name,
                'Type' AS kind,
                r.title AS relatedRecordName,
                DATE_FORMAT(r.updatedAt, '%Y-%m-%d %H:%i') AS dateOfUse
            FROM resource r
            INNER JOIN resourceType rt ON r.resourceTypeId = rt.resourceTypeId

            UNION ALL

            SELECT 
                c.categoryTopic AS name,
                'Topic' AS kind,
                r.title AS relatedRecordName,
                DATE_FORMAT(r.updatedAt, '%Y-%m-%d %H:%i') AS dateOfUse
            FROM resource r
            INNER JOIN category c ON r.categoryId = c.categoryId

            ORDER BY dateOfUse DESC
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ClassificationUsageHistoryView item = new ClassificationUsageHistoryView();
            item.setName(rs.getString("name"));
            item.setKind(rs.getString("kind"));
            item.setRelatedRecordName(rs.getString("relatedRecordName"));
            item.setDateOfUse(rs.getString("dateOfUse"));
            return item;
        });
    }
}