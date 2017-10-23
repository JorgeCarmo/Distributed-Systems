package org.komparator.mediator.domain;


public class Item {
    /**Item identifier*/
    private ItemId itemId;
    /**Item description*/
    private String description;
    /**Item price*/
    private int price;

    /** Create a new Item */
    public Item(ItemId newItemId, String desc, int newPrice){
        this.itemId = newItemId;
        this.description = desc;
        this.price = newPrice;
    }

    public ItemId getItemId(){
        return itemId;
    }

    public String getDescription(){
        return description;
    }

    public int getPrice(){
        return price;
    }

    @Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Item [itemId=").append(itemId);
		builder.append(", description=").append(description);
		builder.append(", price=").append(price);
		builder.append("]");
		return builder.toString();
	}
}
