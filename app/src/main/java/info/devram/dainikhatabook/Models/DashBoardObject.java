package info.devram.dainikhatabook.Models;

public class DashBoardObject {
    private String idObject;
    private String typeObject;
    private long dateObject;
    private String descObject;
    private int amountObject;
    private boolean isExpense = false;
    private boolean syncStatus;

    public DashBoardObject() {
    }

    public DashBoardObject(String typeObject, long dateObject, String descObject, int amountObject) {
        this.typeObject = typeObject;
        this.dateObject = dateObject;
        this.descObject = descObject;
        this.amountObject = amountObject;
    }

    public String getIdObject() {
        return idObject;
    }

    public void setIdObject(String idObject) {
        this.idObject = idObject;
    }

    public String getTypeObject() {
        return typeObject;
    }

    public void setTypeObject(String typeObject) {
        this.typeObject = typeObject;
    }

    public long getDateObject() {
        return dateObject;
    }

    public void setDateObject(long dateObject) {
        this.dateObject = dateObject;
    }

    public String getDescObject() {
        return descObject;
    }

    public void setDescObject(String descObject) {
        this.descObject = descObject;
    }

    public int getAmountObject() {
        return amountObject;
    }

    public void setAmountObject(int amountObject) {
        this.amountObject = amountObject;
    }

    public boolean getIsExpense() {
        return isExpense;
    }

    public void setIsExpense(boolean expense) {
        isExpense = expense;
    }

    public boolean getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(boolean syncStatus) {
        this.syncStatus = syncStatus;
    }

    @Override
    public String toString() {
        return "DashBoardObject{" +
                "typeObject='" + typeObject + '\'' +
                ", dateObject=" + dateObject +
                ", descObject='" + descObject + '\'' +
                ", amountObject=" + amountObject +
                '}';
    }
}
