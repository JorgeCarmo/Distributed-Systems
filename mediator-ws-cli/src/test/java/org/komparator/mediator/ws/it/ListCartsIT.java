package org.komparator.mediator.ws.it;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;

import java.util.Date;

import org.komparator.mediator.ws.ItemIdView;
import org.komparator.mediator.ws.InvalidCartId_Exception;
import org.komparator.mediator.ws.InvalidItemId_Exception;
import org.komparator.mediator.ws.InvalidQuantity_Exception;
import org.komparator.mediator.ws.NotEnoughItems_Exception;

import org.komparator.supplier.ws.cli.SupplierClient;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadProduct_Exception;

import org.komparator.security.Manager;

/**
 * Test suite
 */
public class ListCartsIT extends BaseIT {

    @Before
 	public void setUp() throws BadProduct_Exception, BadProductId_Exception, InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
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

        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("X1");
        itemId.setSupplierId(supplierNames[0]);

        mediatorClient.addToCart("SuperCart", itemId, 10, dateFormatter.format(new Date()));

 	}

 	@After
 	public void tearDown() {
        mediatorClient.clear();
 	}

    @Test
    public void listsCartsEmptyTest() {
        assertNotNull(mediatorClient.listCarts());
    }
}
