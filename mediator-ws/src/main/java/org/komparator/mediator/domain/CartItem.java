package org.komparator.mediator.domain;


public class CartItem {
    /**Item in Cart*/
    Item item;
    /**Quantity of this item*/
    int quantity;

    /** Create a new CartItem */
    public CartItem(Item newItem, int quant){
        this.item = newItem;
        this.quantity = quant;
    }

    public Item getItem(){
        return item;
    }

    /** Synchronized locks object before returning quantity */
    public synchronized int getQuantity() {
        return quantity;
    }

    public void addQuantity(int i) {
    	this.quantity+=i;
    }

}
