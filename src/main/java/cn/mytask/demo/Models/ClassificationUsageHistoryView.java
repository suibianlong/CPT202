package cn.mytask.demo.Models;

public class ClassificationUsageHistoryView {

    private String name;
    private String kind;
    private String relatedRecordName;
    private String dateOfUse;

    public ClassificationUsageHistoryView() {
    }

    public ClassificationUsageHistoryView(String name, String kind, String relatedRecordName, String dateOfUse) {
        this.name = name;
        this.kind = kind;
        this.relatedRecordName = relatedRecordName;
        this.dateOfUse = dateOfUse;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
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