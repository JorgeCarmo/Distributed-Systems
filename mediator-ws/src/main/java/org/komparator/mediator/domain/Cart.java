package org.komparator.mediator.domain;

import java.util.ArrayList;
import java.util.List;

public class Cart {
   /**Cart identifier*/
   private String cartId;
   /**List of items in this cart*/
   private List<CartItem> cartItems = new ArrayList();

   /** Create a new cart*/
   public Cart(String cId, CartItem cartItem){
       this.cartId =  cId;
       this.cartItems.add(cartItem);
   }

   public String getCartId(){
       return cartId;
   }

   public synchronized List<CartItem> getCartItems(){
       return cartItems;
   }

   public synchronized void addItem(CartItem cartItem) {
	   this.cartItems.add(cartItem);
   }

   public void addQuantityCartItem(Item item, int quantity){
       for (CartItem tempCartItem : cartItems){
           if (tempCartItem.getItem().getItemId().getProductId().equals(item.getItemId().getProductId()) && tempCartItem.getItem().getItemId().getSupplierId().equals(item.getItemId().getSupplierId())) {
               tempCartItem.addQuantity(quantity);
           }
       }
   }

   public synchronized void clear(){
       cartItems.clear();
   }


   //TODO: toString, removeItem, might need more things
}
