package org.komparator.mediator.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** Domain Root. */
public class Mediator {
    //TODO:ADD DOC and add missing methods arraylist in shopHistory might create problems
	private Map<String, Cart> carts = new ConcurrentHashMap<>();

    private List<ItemId> items = new ArrayList();

    private AtomicInteger purchaseIdCounter = new AtomicInteger(0);

    private List<ShoppingResult> shopHistory = new ArrayList();
    // Singleton -------------------------------------------------------------

 	/* Private constructor prevents instantiation from other classes */
   private Mediator() {
   }

	/**
	 * SingletonHolder is loaded on the first execution of
	 * Singleton.getInstance() or the first access to SingletonHolder.INSTANCE,
	 * not before.
	 */
   private static class SingletonHolder {
       private static final Mediator INSTANCE = new Mediator();
   }

   public static synchronized Mediator getInstance() {
       return SingletonHolder.INSTANCE;
   }

   public synchronized Cart getCart(String cartId) {
		return carts.get(cartId);
	}

	public List<Cart> getCarts() {
		List<Cart> list = new ArrayList();
		list.addAll(carts.values());
		return list;
	}

	public List<ShoppingResult> getShoppingResult() {
		return shopHistory;
	}

    public void addShoppingResult(ShoppingResult shoppingResult){
        shopHistory.add(shoppingResult);
    }

    public String generatePurchaseId() {
		// relying on AtomicInteger to make sure assigned number is unique
		int purchaseId = purchaseIdCounter.incrementAndGet();
		return Integer.toString(purchaseId);
	}

    public void addToCart(String cartId, String productId, String supplierId, int itemQty, int supplierquantity, String description, int price) throws ItemsQuantityException {
        ItemId realItemId = new ItemId(productId, supplierId);
        Item item = new Item(realItemId, description, price);
		Cart cart = this.getCart(cartId);
        CartItem cartItem = new CartItem(item, itemQty);
		if (cart == null) {
			cart = new Cart(cartId, cartItem);
			this.registerCart(cart);
		} else {
            boolean itemNotFound = true;
            for (CartItem tempCartItem : cart.getCartItems()){
		        if (tempCartItem.getItem().getItemId().getProductId().equals(productId) && tempCartItem.getItem().getItemId().getSupplierId().equals(supplierId)) {
                    if((cartItem.getQuantity() + tempCartItem.getQuantity()) > supplierquantity){
                        throw new ItemsQuantityException("Supplier doesn't have enough amount of that product!");
                    }
				cart.addQuantityCartItem(cartItem.getItem(), cartItem.getQuantity());
                itemNotFound = false;
				}
            }
			if (itemNotFound){
				cart.addItem(cartItem);
            }
		}
    }


	public synchronized void registerCart(Cart cart) {
		carts.put(cart.getCartId(), cart);
	}


    public synchronized void clearCart(String cartId){
        carts.get(cartId).clear();
    }


   public synchronized void reset() {
	   carts.clear();
	   items.clear();
	   shopHistory.clear();
       purchaseIdCounter.set(0);
	}


}
