package org.komparator.mediator.ws.it;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.Date;

import static org.junit.Assert.assertNotNull;

import org.komparator.mediator.ws.ItemIdView;
import org.komparator.mediator.ws.InvalidCartId_Exception;
import org.komparator.mediator.ws.InvalidItemId_Exception;
import org.komparator.mediator.ws.InvalidQuantity_Exception;
import org.komparator.mediator.ws.NotEnoughItems_Exception;
import org.komparator.mediator.ws.EmptyCart_Exception;
import org.komparator.mediator.ws.InvalidCreditCard_Exception;


import org.komparator.supplier.ws.cli.SupplierClient;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadProduct_Exception;

import org.komparator.security.Manager;

/**
 * Test suite
 */
public class ShopHistoryIT extends BaseIT {

    private static final String VALID_CC = "1234567890123452";

    @Before
 	public void setUp() throws InvalidCreditCard_Exception, EmptyCart_Exception, BadProduct_Exception, BadProductId_Exception, InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        mediatorClient.clear();
        Manager manager = Manager.getInstance();

        {
 			ProductView product = new ProductView();
 			product.setId("X1");
 			product.setDesc("Basketball");
 			product.setPrice(10);
 			product.setQuantity(12);
            manager.setCertificate(supplierNames[0]);
 			supplierClients[0].createProduct(product);

        }
        {
            ProductView product = new ProductView();
 			product.setId("Y2");
 			product.setDesc("Baseball");
 			product.setPrice(15);
 			product.setQuantity(50);
            manager.setCertificate(supplierNames[0]);
 			supplierClients[0].createProduct(product);
        }
        {
            ProductView product = new ProductView();
 			product.setId("Z3");
 			product.setDesc("Soccer ball");
 			product.setPrice(20);
 			product.setQuantity(60);
            manager.setCertificate(supplierNames[0]);
 			supplierClients[0].createProduct(product);
        }

        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("X1");
        itemId.setSupplierId(supplierNames[0]);

        mediatorClient.addToCart("SuperCart", itemId, 10, dateFormatter.format(new Date()));
        mediatorClient.buyCart("SuperCart", VALID_CC, dateFormatter.format(new Date()));

        itemId.setProductId("Y2");
        itemId.setSupplierId(supplierNames[0]);

        mediatorClient.addToCart("SuperCart", itemId, 15, dateFormatter.format(new Date()));
        mediatorClient.buyCart("SuperCart", VALID_CC, dateFormatter.format(new Date()));

        itemId.setProductId("Z3");
        itemId.setSupplierId(supplierNames[0]);

        mediatorClient.addToCart("SuperCart", itemId, 20, dateFormatter.format(new Date()));
        mediatorClient.buyCart("SuperCart", VALID_CC, dateFormatter.format(new Date()));
 	}

 	@After
 	public void tearDown() {
        mediatorClient.clear();
 	}

    @Test
    public void shopHistoryEmptyTest() {
        assertNotNull(mediatorClient.shopHistory());

    }

}
