package org.komparator.mediator.domain;


public class ItemId {
    /**Item product identifier*/
    private String productId;
    /**Item supplier identifier*/
    private String supplierId;

    /** Create a new Item Id*/
    public ItemId(String newProductId, String newSupplierId){
        this.productId = newProductId;
        this.supplierId = newSupplierId;
    }

    public String getProductId(){
        return productId;
    }

    public String getSupplierId(){
        return supplierId;
    }

    @Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ItemId [productId=").append(productId);
		builder.append(", supplierId=").append(supplierId);
		builder.append("]");
		return builder.toString();
	}
}
