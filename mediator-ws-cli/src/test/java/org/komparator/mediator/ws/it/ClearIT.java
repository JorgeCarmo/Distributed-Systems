package org.komparator.mediator.ws.it;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import org.komparator.mediator.ws.cli.MediatorClient;
import org.komparator.mediator.ws.ItemView;
import org.komparator.mediator.ws.InvalidItemId_Exception;

import org.komparator.supplier.ws.cli.SupplierClient;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadProduct_Exception;
import org.komparator.supplier.ws.ProductView;

import org.komparator.security.Manager;

/**
 * Test suite
 */
public class ClearIT extends BaseIT {

    @BeforeClass
    public static void oneTimeSetUp() throws BadProductId_Exception, BadProduct_Exception {
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
            product.setPrice(12);
            product.setQuantity(15);
            manager.setCertificate(supplierNames[1]);
            supplierClients[1].createProduct(product);
 		}
        {
            ProductView product = new ProductView();
 			product.setId("Y2");
 			product.setDesc("Muay Thai");
 			product.setPrice(20);
 			product.setQuantity(30);
            manager.setCertificate(supplierNames[0]);
 			supplierClients[0].createProduct(product);
            product.setPrice(15);
 			product.setQuantity(10);
            manager.setCertificate(supplierNames[1]);
 			supplierClients[1].createProduct(product);
 		}
 		{
 			ProductView product = new ProductView();
 			product.setId("Z3");
 			product.setDesc("Soccer ball");
 			product.setPrice(50);
 			product.setQuantity(30);
            manager.setCertificate(supplierNames[0]);
 			supplierClients[0].createProduct(product);
            product.setPrice(30);
 			product.setQuantity(20);
            manager.setCertificate(supplierNames[1]);
 			supplierClients[1].createProduct(product);
         }
    }

 	@AfterClass
 	public static void oneTimeTearDown() {
 		mediatorClient.clear();
 	}

 	// initialization and clean-up for each test
 	@Before
 	public void setUp() {
 	}

 	@After
 	public void tearDown() {
 	}

    @Test
    public void sucessClear() throws InvalidItemId_Exception {
        mediatorClient.clear();
        List<ItemView> items = mediatorClient.getItems("X1");
        assertEquals(items.size(), 0);
    }
}
