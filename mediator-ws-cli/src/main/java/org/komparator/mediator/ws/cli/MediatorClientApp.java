package org.komparator.mediator.ws.cli;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;

import java.lang.Thread;

import java.text.SimpleDateFormat;

import org.komparator.mediator.ws.CartView;
import org.komparator.mediator.ws.CartItemView;
import org.komparator.mediator.ws.ItemIdView;
import org.komparator.mediator.ws.ItemView;
import org.komparator.mediator.ws.ShoppingResultView;

import org.komparator.security.Manager;

public class MediatorClientApp {

    public static void main(String[] args) throws Exception {
        // Check arguments
        if (args.length == 0) {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: java " + MediatorClientApp.class.getName()
                    + " wsURL OR uddiURL wsName");
            return;
        }
        String uddiURL = null;
        String wsName = null;
        String wsURL = null;
        String option = null;
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        int timeoutConnect = 0;
        int timeoutReceive = 0;
        if (args.length == 1) {
            wsURL = args[0];
        } else if (args.length >= 3) {
            uddiURL = args[0];
            wsName = args[1];
            option = args[2];
            timeoutConnect = Integer.parseInt(args[3]);
            timeoutReceive = Integer.parseInt(args[4]);
        }

        // Create client
        MediatorClient client = null;

        if (wsURL != null) {
            System.out.printf("Creating client for server at %s%n", wsURL);
            client = new MediatorClient(wsURL);
        } else if (uddiURL != null) {
            System.out.printf("Creating client using UDDI at %s for server with name %s%n",
                uddiURL, wsName);
            client = new MediatorClient(uddiURL, wsName, timeoutConnect, timeoutReceive);
        }

        // the following remote invocations are just basic examples
        // the actual tests are made using JUnit

        if(!option.equals("1")){
            System.out.println("Killing main server");
            client.kill("Tester");

            System.out.println("Sleeping untill Secondary server replaces primary");
            Thread.sleep(15 *1000);

            List<ShoppingResultView> shoppingResultViews = new ArrayList();
            shoppingResultViews = client.shopHistory();

            if(shoppingResultViews.size() == 0){
                System.out.println("There were no purchases");
            }
            else{
                System.out.println("Purchase history:");
            }

            for(ShoppingResultView shoppingResultView :shoppingResultViews){
                System.out.println("Purchase Id");
                System.out.println(shoppingResultView.getId());
                System.out.println("Purchase result");
                System.out.println(shoppingResultView.getResult().name());

                if(shoppingResultView.getPurchasedItems().size() == 0){
                    System.out.println("There where no purchases");
                }
                else {
                    System.out.println("Purchased Items");
                    for(CartItemView cartItem : shoppingResultView.getPurchasedItems()){
                        ItemView item = cartItem.getItem();
                        ItemIdView itemId = item.getItemId();
                        System.out.println("Product Id");
                        System.out.println(itemId.getProductId());
                        System.out.println("Supplier Id");
                        System.out.println(itemId.getSupplierId());
                        System.out.println("Product description");
                        System.out.println(item.getDesc());
                        System.out.println("Product price");
                        System.out.println(item.getPrice());
                        System.out.println("Product quantity");
                        System.out.println(cartItem.getQuantity());
                    }
                }

                if(shoppingResultView.getDroppedItems().size() == 0){
                    System.out.println("There where no dropped items");
                }
                else {
                    System.out.println("Dropped Items");
                    for(CartItemView cartItem : shoppingResultView.getDroppedItems()){
                        ItemView item = cartItem.getItem();
                        ItemIdView itemId = item.getItemId();
                        System.out.println("Product Id");
                        System.out.println(itemId.getProductId());
                        System.out.println("Supplier Id");
                        System.out.println(itemId.getSupplierId());
                        System.out.println("Product description");
                        System.out.println(item.getDesc());
                        System.out.println("Product price");
                        System.out.println(item.getPrice());
                        System.out.println("Product quantity");
                        System.out.println(cartItem.getQuantity());
                    }
                }
                System.out.println("This cart price was:");
                System.out.println(shoppingResultView.getTotalPrice());
            }

            List<CartView> carts = new ArrayList();
            carts = client.listCarts();

            if(carts.size() == 0){
                System.out.println("There are no carts");
            }
            else{
                System.out.println("Existing carts");
            }

            for(CartView cart: carts){
                System.out.println(cart.getCartId());


                if(cart.getItems().size() == 0){
                    System.out.println("This cart is empty");
                }
                else{
                    for(CartItemView cartItem : cart.getItems()){
                        ItemView item = cartItem.getItem();
                        ItemIdView itemId = item.getItemId();
                        System.out.println(itemId.getProductId());
                        System.out.println(itemId.getSupplierId());
                        System.out.println(item.getDesc());
                        System.out.println(item.getPrice());
                        System.out.println(cartItem.getQuantity());
                    }
                }
            }

        }
        else{
            try{

                System.out.println("Invoke ping()...");
                String result = client.ping("client");
                System.out.println(result);

                {
			        ItemIdView id = new ItemIdView();
			        id.setProductId("p3");
			        id.setSupplierId("A61_Supplier1");
			        client.addToCart("Carrinho Rejeitado", id, 50, dateFormatter.format(new Date()));
                    client.addToCart("Carrinho Meio Fixe", id, 40, dateFormatter.format(new Date()));
		        }
                {
			        ItemIdView id = new ItemIdView();
			        id.setProductId("p2");
			        id.setSupplierId("A61_Supplier1");
                    client.addToCart("Carrinho nao usado", id, 25, dateFormatter.format(new Date()));
			        client.addToCart("Carrinho Meio Fixe", id, 25, dateFormatter.format(new Date()));
                    client.addToCart("Carrinho Fixe", id, 5, dateFormatter.format(new Date()));
		        }

                {
			        ItemIdView id = new ItemIdView();
			        id.setProductId("p1");
			        id.setSupplierId("A61_Supplier1");
			        client.addToCart("Carrinho Fixe", id, 2, dateFormatter.format(new Date()));
		        }

                String VALID_CC = "1234567890123452";

                client.buyCart("Carrinho Fixe", VALID_CC, dateFormatter.format(new Date()));
                client.buyCart("Carrinho Meio Fixe", VALID_CC, dateFormatter.format(new Date()));
                client.buyCart("Carrinho Rejeitado", VALID_CC, dateFormatter.format(new Date()));

                List<ShoppingResultView> shoppingResultViews = new ArrayList();
                shoppingResultViews = client.shopHistory();

                if(shoppingResultViews.size() == 0){
                    System.out.println("There were no purchases");
                }
                else{
                    System.out.println("Purchase history:");
                }

                for(ShoppingResultView shoppingResultView :shoppingResultViews){
                    System.out.println("Purchase Id");
                    System.out.println(shoppingResultView.getId());
                    System.out.println("Purchase result");
                    System.out.println(shoppingResultView.getResult().name());

                    if(shoppingResultView.getPurchasedItems().size() == 0){
                        System.out.println("There where no purchases");
                    }
                    else {
                        System.out.println("Purchased Items");
                        for(CartItemView cartItem : shoppingResultView.getPurchasedItems()){
                            ItemView item = cartItem.getItem();
                            ItemIdView itemId = item.getItemId();
                            System.out.println("Product Id");
                            System.out.println(itemId.getProductId());
                            System.out.println("Supplier Id");
                            System.out.println(itemId.getSupplierId());
                            System.out.println("Product description");
                            System.out.println(item.getDesc());
                            System.out.println("Product price");
                            System.out.println(item.getPrice());
                            System.out.println("Product quantity");
                            System.out.println(cartItem.getQuantity());
                        }
                    }

                    if(shoppingResultView.getDroppedItems().size() == 0){
                        System.out.println("There where no dropped items");
                    }
                    else {
                        System.out.println("Dropped Items");
                        for(CartItemView cartItem : shoppingResultView.getDroppedItems()){
                            ItemView item = cartItem.getItem();
                            ItemIdView itemId = item.getItemId();
                            System.out.println("Product Id");
                            System.out.println(itemId.getProductId());
                            System.out.println("Supplier Id");
                            System.out.println(itemId.getSupplierId());
                            System.out.println("Product description");
                            System.out.println(item.getDesc());
                            System.out.println("Product price");
                            System.out.println(item.getPrice());
                            System.out.println("Product quantity");
                            System.out.println(cartItem.getQuantity());
                        }
                    }
                    System.out.println("This cart price was:");
                    System.out.println(shoppingResultView.getTotalPrice());
                }

                List<CartView> carts = new ArrayList();
                carts = client.listCarts();

                if(carts.size() == 0){
                    System.out.println("There are no carts");
                }
                else{
                    System.out.println("Existing carts");
                }

                for(CartView cart: carts){
                    System.out.println(cart.getCartId());


                    if(cart.getItems().size() == 0){
                        System.out.println("This cart is empty");
                    }
                    else{
                        for(CartItemView cartItem : cart.getItems()){
                            ItemView item = cartItem.getItem();
                            ItemIdView itemId = item.getItemId();
                            System.out.println(itemId.getProductId());
                            System.out.println(itemId.getSupplierId());
                            System.out.println(item.getDesc());
                            System.out.println(item.getPrice());
                            System.out.println(cartItem.getQuantity());
                        }
                    }
                }


            } catch (Exception e){
                System.out.println("Mediator problems");
            }
        }
    }
}
