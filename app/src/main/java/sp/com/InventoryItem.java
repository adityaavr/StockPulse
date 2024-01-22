package sp.com;

public class InventoryItem {
    private String itemName;
    private int itemQuantity;
    private int totalItemQuantity;
    private String imageUrl;
    private String itemId;

    // Default constructor is required for Firestore's automatic data mapping.
    public InventoryItem() {
    }

    public InventoryItem(String itemName, int itemQuantity, int totalItemQuantity, String imageUrl) {
        this.itemName = itemName;
        this.itemQuantity = itemQuantity;
        this.totalItemQuantity = totalItemQuantity;
        this.imageUrl = imageUrl;
    }

    // Getters
    public String getItemName() {
        return itemName;
    }

    public int getItemQuantity() {
        return itemQuantity;
    }

    public int getTotalItemQuantity() {
        return totalItemQuantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    public String getItemId() {
        return itemId;
    }

    // Setters
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setItemQuantity(int itemQuantity) {
        this.itemQuantity = itemQuantity;
    }

    public void setTotalItemQuantity(int totalItemQuantity) {
        this.totalItemQuantity = totalItemQuantity;
    }
    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
