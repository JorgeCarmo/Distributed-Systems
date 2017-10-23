package org.komparator.mediator.ws;

import java.util.List;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Date;

import java.text.SimpleDateFormat;
import java.text.ParseException;

import javax.jws.WebService;
import javax.jws.HandlerChain;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDIRecord;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;

import java.io.IOException;

import org.komparator.mediator.domain.Mediator;
import org.komparator.mediator.domain.Item;
import org.komparator.mediator.domain.ItemId;
import org.komparator.mediator.domain.Cart;
import org.komparator.mediator.domain.CartItem;
import org.komparator.mediator.domain.ShoppingResult;
import org.komparator.mediator.domain.ItemsQuantityException;
import org.komparator.mediator.ws.MediatorEndpointManager.State;


import org.komparator.supplier.ws.BadProductId;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadText_Exception;
import org.komparator.supplier.ws.BadQuantity_Exception;
import org.komparator.supplier.ws.InsufficientQuantity_Exception;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.cli.SupplierClient;
import org.komparator.supplier.ws.cli.SupplierClientException;

import pt.ulisboa.tecnico.sdis.ws.cli.CreditCardClient;
import pt.ulisboa.tecnico.sdis.ws.cli.CreditCardClientException;


@WebService(
		endpointInterface = "org.komparator.mediator.ws.MediatorPortType",
		wsdlLocation = "mediator.2_0.wsdl",
		name = "MediatorWebService",
		portName = "MediatorPort",
		targetNamespace = "http://ws.mediator.komparator.org/",
		serviceName = "MediatorService"
)
@HandlerChain(file = "/mediator-ws_handler-chain.xml")
public class MediatorPortImpl
	implements MediatorPortType	{

	// end point manager
	private MediatorEndpointManager endpointManager;

    private List<Date> timestamps;

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

	public MediatorPortImpl(MediatorEndpointManager endpointManager) {
		this.endpointManager = endpointManager;
        this.timestamps = Collections.synchronizedList(new ArrayList());
	}

    // P4

    @Override
    public void imAlive(){
        if (endpointManager.getState() == State.SECONDARY){
            endpointManager.setAlive(new Date());
            System.out.println("Primary server is alive!");
            System.out.println(endpointManager.getAlive());
        }
    }

    @Override
    public void updateShopHistory(ShoppingResultView shopResults, String cartId, String time){
        List<CartItem> purchasedItems = new ArrayList();

        List<CartItem> droppedItems = new ArrayList();

        try {
            Date timestamp = dateFormatter.parse(time);

            timestamps.add(timestamp);

            for(CartItemView purchasedItem : shopResults.getPurchasedItems()){
                ItemId itemId = new ItemId(purchasedItem.getItem().getItemId().getProductId(), purchasedItem.getItem().getItemId().getSupplierId());
                Item item = new Item(itemId, purchasedItem.getItem().getDesc(), purchasedItem.getItem().getPrice());
                CartItem purchased = new CartItem(item, purchasedItem.getQuantity());
                purchasedItems.add(purchased);
            }

            for (CartItemView droppedItem : shopResults.getDroppedItems()){
                ItemId itemId = new ItemId(droppedItem.getItem().getItemId().getProductId(), droppedItem.getItem().getItemId().getSupplierId());
                Item item = new Item(itemId, droppedItem.getItem().getDesc(), droppedItem.getItem().getPrice());
                CartItem dropped = new CartItem(item, droppedItem.getQuantity());
                droppedItems.add(dropped);
            }

            ShoppingResult shopingResult = new ShoppingResult(shopResults.getId(), shopResults.getResult(), purchasedItems, droppedItems, shopResults.getTotalPrice(), timestamp);
            Mediator mediator = Mediator.getInstance();
            mediator.addShoppingResult(shopingResult);
            mediator.clearCart(cartId);
            System.out.println("Updated by primary server");
        } catch(ParseException e){
            System.out.println("Problem with date");
        }
    }

    @Override
    public void updateCart(String cartId, ItemIdView itemId, int itemQty, int supplierquantity, String description, int price, String time){
        Mediator mediator = Mediator.getInstance();
        try{
            Date timestamp = dateFormatter.parse(time);

            timestamps.add(timestamp);

            mediator.addToCart(cartId, itemId.getProductId(), itemId.getSupplierId(), itemQty, supplierquantity, description, price);

            System.out.println("Updated by primary server");
        } catch (Exception e) {
            System.out.println("Correct me");
        }
    }


	// Main operations -------------------------------------------------------

    @Override
	public void addToCart(String cartId, ItemIdView itemId, int itemQty, String time) throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        // check cart id
        if (cartId == null){
			throwInvalidCartId("Cart identifier cannot be null!");
        }
		cartId = cartId.trim();
		if (cartId.length() == 0){
			throwInvalidCartId("Cart identifier cannot be empty or whitespace!");
        }

        if (itemId == null){
			throwInvalidItemId("Item cannot be null!");
        }

		// check product id
        String productId = itemId.getProductId();
		if (productId == null){
			throwInvalidItemId("Product identifier cannot be null!");
        }
		productId = productId.trim();
		if (productId.length() == 0){
			throwInvalidItemId("Product identifier cannot be empty or whitespace!");
        }

        // check supplier id basic check
        String supplierId = itemId.getSupplierId();
        if (supplierId == null){
            throwInvalidItemId("Supplier identifier cannot be null!");
        }
        supplierId = supplierId.trim();
        if (supplierId.length() == 0){
            throwInvalidItemId("Supplier identifier cannot be empty or whitespace!");
        }

		// check negative quantity
		if (itemQty <= 0){
			throwInvalidQuantity("Item quantity must be greater than 0!");
        }

		try {

            Date timestamp = dateFormatter.parse(time);

    		if(timestamps.contains(timestamp)){
    			return;
    		}
            else{
                timestamps.add(timestamp);
            }

            Mediator mediator = Mediator.getInstance();
            UDDINaming uddiNaming = this.endpointManager.getUddiNaming();

            String uddiUrl = uddiNaming.getUDDIUrl();
			SupplierClient supplier = new SupplierClient(uddiUrl, supplierId);

            if (supplier == null){
                System.out.println(supplierId);
                throwInvalidItemId("Supplier does not exist!");
            }
			ProductView newproduct = supplier.getProduct(productId);
            if (newproduct == null){
                throwInvalidItemId("Product does not exist!");
            }
			int supplierquantity = newproduct.getQuantity();
			// check if supplier has enough item quantity
			if (supplierquantity < itemQty){
				throwNotEnoughItems("Supplier doesn't have enough amount of that product!");
            }
            mediator.addToCart(cartId, productId, supplierId, itemQty, supplierquantity, newproduct.getDesc(), newproduct.getPrice());

            if(endpointManager.getState() == State.MAIN){
                MediatorPortType port = endpointManager.getSecondPort();
                System.out.println("Updating secondary server");
                port.updateCart(cartId, itemId, itemQty, supplierquantity, newproduct.getDesc(), newproduct.getPrice(), time);
            }

		} catch (ParseException e){
            System.out.printf("Bad Date", e);
        } catch (BadProductId_Exception e) {
			System.out.printf("Caught exception when starting: %s%n", e);
		} catch (NotEnoughItems_Exception e) {
			System.out.printf("Caught exception when starting: %s%n", e);
            System.out.println("Rethrowing");
            throw e;
        } catch (ItemsQuantityException e) {
    		System.out.printf("Caught exception when starting: %s%n", e);
            throwNotEnoughItems("Supplier doesn't have enough amount of that product!");
		} catch (SupplierClientException e) {
			System.out.printf("Caught exception when starting: %s%n", e);
            throwInvalidItemId("Item does not exist");
		} catch (Exception e) {
			System.out.printf("Caught exception when starting: %s%n", e);
            throwInvalidItemId("Item does not exist");
		}
	}

    @Override
	public List<ItemView> searchItems(String descText) throws InvalidText_Exception {
		if (descText == null || descText.trim().length() == 0){
			throwInvalidText("Invalid item desciption!");
        }

		UDDINaming uddiNaming = this.endpointManager.getUddiNaming();
		List<ItemView> items = new ArrayList<ItemView>();

		if (uddiNaming != null){
			try {
				Collection<UDDIRecord> uddiRecords = new ArrayList();
				uddiRecords.addAll(uddiNaming.listRecords("A61_Supplier%"));

				for (UDDIRecord uddiRecord : uddiRecords){
					SupplierClient supplier = new SupplierClient(uddiNaming.getUDDIUrl(), uddiRecord.getOrgName());
					List<ProductView> products = new ArrayList<ProductView>();
					products = supplier.searchProducts(descText);
					for (ProductView product : products){
						Item item = new Item(new ItemId(product.getId(), uddiRecord.getOrgName()), product.getDesc(), product.getPrice());
						ItemView itemView = newItemView(item);
						items.add(itemView);
                    }
				}
			} catch (UDDINamingException e) {
				System.out.printf("Caught exception when starting: %s%n", e);
			} catch (BadText_Exception e) {
				System.out.printf("Caught exception when starting: %s%n", e);
			} catch (SupplierClientException e) {
				System.out.printf("Caught exception when starting: %s%n", e);
			}
		    Collections.sort(items, new Comparator<ItemView>() {
		    	@Override
		        public int compare(ItemView o1, ItemView o2) {

		            String x1 = ((ItemView) o1).getItemId().getProductId();
		            String x2 = ((ItemView) o2).getItemId().getProductId();
		            int sComp = x1.compareTo(x2);

		            if (sComp != 0) {
		               return sComp;
		            } else {
		               Integer i1 = ((ItemView) o1).getPrice();
		               Integer i2 = ((ItemView) o2).getPrice();
		               return i1.compareTo(i2);
		            }
		    }
				});
			return items;
		}
		return items;
	}


	@Override
    public List<ItemView> getItems(String productId) throws InvalidItemId_Exception {

		if (productId == null || productId.trim().length() == 0){
			throwInvalidItemId("Invalid product ID!");
        }

		UDDINaming uddiNaming = this.endpointManager.getUddiNaming();

		List<ItemView> items = new ArrayList();

		if (uddiNaming != null){
			try {
				Collection<UDDIRecord> uddiRecords = new ArrayList();
				uddiRecords.addAll(uddiNaming.listRecords("A61_Supplier%"));

				for (UDDIRecord uddiRecord : uddiRecords){
					SupplierClient supplier = new SupplierClient(uddiNaming.getUDDIUrl(), uddiRecord.getOrgName());
					ProductView product = supplier.getProduct(productId);
                    if(product != null){
					    Item item = new Item(new ItemId(productId, uddiRecord.getOrgName()), product.getDesc(), product.getPrice());
					    ItemView itemView = newItemView(item);
					    items.add(itemView);
                    }
				}
			} catch (UDDINamingException e) {
				System.out.printf("Caught exception when starting: %s%n", e);
			} catch (BadProductId_Exception e) {
				System.out.printf("Caught exception when starting: %s%n", e);
                throwInvalidItemId("Item does not exist");
			} catch (SupplierClientException e) {
				System.out.printf("Caught exception when starting: %s%n", e);
			}
			Collections.sort(items, new Comparator<ItemView>(){
				   public int compare(ItemView iv1, ItemView iv2){
				      return iv1.getPrice() - iv2.getPrice();
				   }
				});
			return items;
		}
		return items;
	}

	@Override
	public ShoppingResultView buyCart(String cartId, String creditCardNr, String time) throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {

		// check cart id
		if (cartId == null){
			throwInvalidCartId("Cart identifier cannot be null!");
        }
		cartId = cartId.trim();
		if (cartId.length() == 0){
			throwInvalidCartId("Cart identifier cannot be empty or whitespace!");
        }

        // check card number
    	if (creditCardNr == null){
    	    throwInvalidCreditCard("Credit Card number cannot be null!");
        }
    	creditCardNr = creditCardNr.trim();
    	if (creditCardNr.length() == 0){
    	   throwInvalidCreditCard("Credit Card number cannot be empty or whitespace!");
       }

		// check if cart is empty
		Mediator mediator = Mediator.getInstance();
		Cart cart = mediator.getCart(cartId);
		if (cart == null){
			throwInvalidCartId("Cart does not exist!");
        }
        else if (cart.getCartItems().size() == 0){
            throwEmptyCart("Cart is empty!");
        }
		String wsName = this.endpointManager.getWsName();
		UDDINaming uddiNaming = this.endpointManager.getUddiNaming();

		if (uddiNaming != null){
			try {

                Date timestamp = dateFormatter.parse(time);

        		if(timestamps.contains(timestamp)){
        			List<ShoppingResult> shoppingResults = mediator.getShoppingResult();

                    for(ShoppingResult shoppingResult : shoppingResults){
                        if(timestamp.equals(shoppingResult.getTimestamp())){
                            ShoppingResultView shoppingResultView  = newShoppingResultView(shoppingResult.getId(), shoppingResult.getResult(), shoppingResult.getPurchasedItems(), shoppingResult.getDroppedItems(), shoppingResult.getTotalPrice());
                            return shoppingResultView;
                        }
                    }

        		}
                else{
                    timestamps.add(timestamp);
                }

				String wsURL = uddiNaming.lookup("CreditCard");

				CreditCardClient creditCardClient = new CreditCardClient(wsURL);


                if (!creditCardClient.validateNumber(creditCardNr)) {
                    throwInvalidCreditCard("Credit Card number not valid!");
                }


                String purchaseId = mediator.generatePurchaseId();

                List<CartItem> purchasedItems = new ArrayList();

                List<CartItem> droppedItems = new ArrayList();

                int totalPrice = 0;

                int itemQuantity;
                int supplierQuantity;
                SupplierClient supplier;
                ProductView productView;

                for(CartItem cartItem : cart.getCartItems()){
                    String uddiUrl = uddiNaming.getUDDIUrl();
                    supplier = new SupplierClient(uddiUrl, cartItem.getItem().getItemId().getSupplierId());
                    productView = supplier.getProduct(cartItem.getItem().getItemId().getProductId());
                    supplierQuantity = productView.getQuantity();
                    itemQuantity = cartItem.getQuantity();
                    if (supplierQuantity < itemQuantity){
                        droppedItems.add(cartItem);
                    }
                    else if(supplierQuantity >= itemQuantity){
                        purchasedItems.add(cartItem);
                        supplier.buyProduct(cartItem.getItem().getItemId().getProductId(), itemQuantity);
                        totalPrice += cartItem.getItem().getPrice() * itemQuantity;
                    }
                }

                Result result;

                if(droppedItems.size() == 0){
                    result = Result.fromValue("COMPLETE");
                }
                else if(purchasedItems.size() == 0){
                    result = Result.fromValue("EMPTY");
                }
                else{
                    result = Result.fromValue("PARTIAL");
                }

                ShoppingResult shoppingResult = new ShoppingResult(purchaseId, result, purchasedItems, droppedItems, totalPrice, timestamp);

                ShoppingResultView shoppingResultView  = newShoppingResultView(purchaseId, result, purchasedItems, droppedItems, totalPrice);

                mediator.addShoppingResult(shoppingResult);
                mediator.clearCart(cartId);

                if(endpointManager.getState() == State.MAIN){
                    MediatorPortType port = endpointManager.getSecondPort();
                    System.out.println("Updating secondary server");
                    port.updateShopHistory(shoppingResultView, cartId, time);
                }

                return shoppingResultView;

            } catch (ParseException e){
                System.out.printf("Bad Date", e);
			} catch (UDDINamingException e) {
				System.out.printf("Caught exception when starting: %s%n", e);
			} catch (CreditCardClientException e) {
				System.out.printf("Caught exception when starting: %s%n", e);
			} catch (SupplierClientException e) {
				System.out.printf("Caught exception when starting: %s%n", e);
			} catch (BadProductId_Exception e) {
				System.out.printf("Caught exception when starting: %s%n", e);
			} catch (BadQuantity_Exception e) {
				System.out.printf("Caught exception when starting: %s%n", e);
			} catch (InsufficientQuantity_Exception e) {
				System.out.printf("Caught exception when starting: %s%n", e);
			}
		}
        return null;
	}


    // TODO


	// Auxiliary operations --------------------------------------------------

  @Override
  public void kill(String client){
    System.out.println(client + " killed this server");
    System.exit(0);
  }

  @Override
  public String ping(String name){

		if (name == null || name.trim().length() == 0)
			name = "friend";

		String wsName = this.endpointManager.getWsName();
		UDDINaming uddiNaming = this.endpointManager.getUddiNaming();

		StringBuilder builder = new StringBuilder();
		builder.append("Hello ").append(name);
		builder.append(" from ").append(wsName).append("\n");

		if (uddiNaming != null){
			try {
				Collection<UDDIRecord> uddiRecords = new ArrayList();
				uddiRecords.addAll(uddiNaming.listRecords("A61_Supplier%"));

				for (UDDIRecord uddiRecord : uddiRecords){
					SupplierClient supplier = new SupplierClient(uddiNaming.getUDDIUrl(), uddiRecord.getOrgName());
					builder.append(supplier.ping(name)).append("\n");
				}
			} catch (UDDINamingException e) {
				System.out.printf("Caught exception when starting: %s%n", e);
			} catch (SupplierClientException e) {
				System.out.printf("Caught exception when starting: %s%n", e);
			}
		}


		return builder.toString();
	}

    @Override
    public List<ShoppingResultView> shopHistory() {
    	Mediator mediator = Mediator.getInstance();
    	List<ShoppingResultView> shoppingResultViewList = new ArrayList<ShoppingResultView>();
    	for (ShoppingResult shoppingResults : mediator.getShoppingResult()) {
    		shoppingResultViewList.add(newShoppingResultView(shoppingResults.getId(), shoppingResults.getResult(), shoppingResults.getPurchasedItems(), shoppingResults.getDroppedItems(), shoppingResults.getTotalPrice()));
    	}
    	return shoppingResultViewList;
    }

    @Override
    public List<CartView> listCarts() {
		Mediator mediator = Mediator.getInstance();
		List<CartView> cartViewList = new ArrayList<CartView>();
		for (Cart cart : mediator.getCarts()) {
			CartView cartView = newCartView(cart);
			cartViewList.add(cartView);
		}
		return cartViewList;
    }

    @Override
    public void clear() {
			UDDINaming uddiNaming = this.endpointManager.getUddiNaming();
			if (uddiNaming != null){
				try {
					Collection<UDDIRecord> uddiRecords = new ArrayList();
					uddiRecords.addAll(uddiNaming.listRecords("A61_Supplier%"));

					for (UDDIRecord uddiRecord : uddiRecords){
						SupplierClient supplier = new SupplierClient(uddiNaming.getUDDIUrl(), uddiRecord.getOrgName());
						supplier.clear();
					}

                    if(endpointManager.getState() == State.MAIN){
                        MediatorPortType port = endpointManager.getSecondPort();
                        System.out.println("Updating secondary server");
                        port.clear();
                    }

				} catch (UDDINamingException e) {
					System.out.printf("Caught exception when starting: %s%n", e);
				} catch (SupplierClientException e) {
					System.out.printf("Caught exception when starting: %s%n", e);
				}
			}
            Mediator.getInstance().reset();
    }

    // TODO


	// View helpers -----------------------------------------------------
	public void createItem(){

	}

	private ItemIdView newItemIdView(Item item) {
		ItemIdView view = new ItemIdView();
		view.setProductId(item.getItemId().getProductId());
		view.setSupplierId(item.getItemId().getSupplierId());
		return view;
	}

	private ItemView newItemView(Item item) {
		ItemView view = new ItemView();
		view.setItemId(newItemIdView(item));
		view.setDesc(item.getDescription());
		view.setPrice(item.getPrice());
		return view;
	}

	private CartItemView newCartItemView(CartItem cartItem) {
		CartItemView view = new CartItemView();
		view.setItem(newItemView(cartItem.getItem()));
		view.setQuantity(cartItem.getQuantity());
		return view;
	}

	private CartView newCartView(Cart cart) {
		CartView view = new CartView();
		view.setCartId(cart.getCartId());
		for(CartItem ci : cart.getCartItems()){
			view.getItems().add(newCartItemView(ci));
		}
		return view;
	}


	private ShoppingResultView newShoppingResultView(String value, Result result, List<CartItem> purchasedcart, List<CartItem> droppedcart, int totalPrice) {
		ShoppingResultView view = new ShoppingResultView();
		view.setId(value);
        view.setResult(result);
        for(CartItem cartItem : purchasedcart){
			view.getPurchasedItems().add(newCartItemView(cartItem));
		}
		for(CartItem cartItem : droppedcart){
			view.getDroppedItems().add(newCartItemView(cartItem));
		}
		view.setTotalPrice(totalPrice);
		return view;
	}
    // TODO


	// Exception helpers -----------------------------------------------------


    /** Helper method to throw new EmptyCart exception */
  	private void throwEmptyCart(final String message) throws EmptyCart_Exception {
  		EmptyCart faultInfo = new EmptyCart();
  		faultInfo.message = message;
  		throw new EmptyCart_Exception(message, faultInfo);
  	}

    /** Helper method to throw new InvalidCartId exception */
  	private void throwInvalidCartId(final String message) throws InvalidCartId_Exception {
  		InvalidCartId faultInfo = new InvalidCartId();
  		faultInfo.message = message;
  		throw new InvalidCartId_Exception(message, faultInfo);
  	}

    /** Helper method to throw new InvalidCreditCard exception */
  	private void throwInvalidCreditCard(final String message) throws InvalidCreditCard_Exception {
  		InvalidCreditCard faultInfo = new InvalidCreditCard();
  		faultInfo.message = message;
  		throw new InvalidCreditCard_Exception(message, faultInfo);
  	}

    /** Helper method to throw new InvalidItemId exception */
  	private void throwInvalidItemId(final String message) throws InvalidItemId_Exception {
  		InvalidItemId faultInfo = new InvalidItemId();
  		faultInfo.message = message;
  		throw new InvalidItemId_Exception(message, faultInfo);
  	}

    /** Helper method to throw new InvalidQuantity exception */
  	private void throwInvalidQuantity(final String message) throws InvalidQuantity_Exception {
  		InvalidQuantity faultInfo = new InvalidQuantity();
  		faultInfo.message = message;
  		throw new InvalidQuantity_Exception(message, faultInfo);
  	}

    /** Helper method to throw new InvalidText exception */
  	private void throwInvalidText(final String message) throws InvalidText_Exception {
  		InvalidText faultInfo = new InvalidText();
  		faultInfo.message = message;
  		throw new InvalidText_Exception(message, faultInfo);
  	}

    /** Helper method to throw new NotEnoughItems exception */
  	private void throwNotEnoughItems(final String message) throws NotEnoughItems_Exception {
  		NotEnoughItems faultInfo = new NotEnoughItems();
  		faultInfo.message = message;
  		throw new NotEnoughItems_Exception(message, faultInfo);
  	}

}
