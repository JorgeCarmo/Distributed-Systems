package org.komparator.mediator.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;


import org.komparator.mediator.ws.Result;

public class ShoppingResult {
    private String identifier;

    private Result result;

    private List<CartItem> purchasedItems = new ArrayList();

    private List<CartItem> droppedItems = new ArrayList();

    private int totalPrice;

    private Date timestamp;

    /** Create a new CartItem */
    public ShoppingResult(String id, Result res, List<CartItem> purchItems, List<CartItem> dropItems, int total, Date timestamp){
        this.identifier = id;
        this.result = res;
        this.purchasedItems.addAll(purchItems);
        this.droppedItems.addAll(dropItems);
        this.totalPrice = total;
        this.timestamp = timestamp;
    }

    public String getId() {
    	return this.identifier;
    }

    public Result getResult() {
    	return this.result;
    }

    public synchronized List<CartItem> getPurchasedItems() {
    	return this.purchasedItems;
    }

    public synchronized List<CartItem> getDroppedItems() {
    	return this.droppedItems;
    }

    public int getTotalPrice(){
        return this.totalPrice;
    }

    public Date getTimestamp(){
        return this.timestamp;
    }
}
