package cn.mytask.demo.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.mytask.demo.Models.TagUsageHistoryView;

@RestController
public class TagUsageHistoryController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/tagUsageHistory")
    public List<TagUsageHistoryView> getTagUsageHistory() {
        String sql = """
            SELECT
                t.tagName AS tagName,
                r.title AS relatedRecordName,
                DATE_FORMAT(r.updatedAt, '%Y-%m-%d %H:%i') AS dateOfUse
            FROM resourceTag rt
            INNER JOIN tag t ON rt.tagId = t.tagId
            INNER JOIN resource r ON rt.resourceId = r.id
            ORDER BY r.updatedAt DESC
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            TagUsageHistoryView item = new TagUsageHistoryView();
            item.setTagName(rs.getString("tagName"));
            item.setRelatedRecordName(rs.getString("relatedRecordName"));
            item.setDateOfUse(rs.getString("dateOfUse"));
            return item;
        });
    }
}