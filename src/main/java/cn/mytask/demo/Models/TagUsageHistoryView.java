package cn.mytask.demo.Models;

public class TagUsageHistoryView {

    private String tagName;
    private String relatedRecordName;
    private String dateOfUse;

    public TagUsageHistoryView() {
    }

    public TagUsageHistoryView(String tagName, String relatedRecordName, String dateOfUse) {
        this.tagName = tagName;
        this.relatedRecordName = relatedRecordName;
        this.dateOfUse = dateOfUse;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getRelatedRecordName() {
        return relatedRecordName;
    }

    public void setRelatedRecordName(String relatedRecordName) {
        this.relatedRecordName = relatedRecordName;
    }

    public String getDateOfUse() {
        return dateOfUse;
    }

    public void setDateOfUse(String dateOfUse) {
        this.dateOfUse = dateOfUse;
    }
}