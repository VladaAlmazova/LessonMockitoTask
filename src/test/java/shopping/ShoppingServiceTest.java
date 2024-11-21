package shopping;

import customer.Customer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import product.Product;
import product.ProductDao;

/**
 * Тестирование класса {@link ShoppingService}
 */
class ShoppingServiceTest {

    /**
     * Фиктивное взаимодействие с БД
     */
    private final ProductDao productDaoMock = Mockito.mock(ProductDao.class);

    /**
     * Экземпляр сервиса покупок
     */
    private ShoppingService shoppingService;
    /**
     * корзина
     */
    private Cart cart;

    /**
     * Инициализация полей сервис покупок и корзина
     */
    @BeforeEach
    void initShoppingServiceAndCart() {
        shoppingService = new ShoppingServiceImpl(productDaoMock);
        cart = new Cart(Mockito.mock(Customer.class));
    }

    /**
     * Тест. Получение корзины покупателя
     */
    @Test
    void testGetCart() {
        //метод не содержит логики, которую можно было бы проверить.
    }

    /**
     * Тест. Получение всех товаров магазина
     */
    @Test
    void testGetAllProducts() {
        //метод не содержит логики, которую можно было бы проверить.
    }

    /**
     * Тест. Получение товара по названию
     */
    @Test
    void testGetProductByName() {
        //метод не содержит логики, которую можно было бы проверить.
    }

    /**
     * Тест. Удачное совершение покупки
     */
    @Test
    void testSuccessfulBuy() throws BuyException {
        String productName = "bread";
        Product product = new Product(productName, 2);

        Mockito.when(productDaoMock.getByName(productName))
                .thenReturn(product);
        cart.add(product, 1);
        boolean resultBuy = shoppingService.buy(cart);

        Assertions.assertTrue(resultBuy);
        //изменилось количество товара
        Assertions.assertEquals(1, product.getCount(),
                "Количество товара должно измениться после покупки");
        //изменения сохранены в БД
        Mockito.verify(productDaoMock).save(product);
        //Корзина отчистилась
        Assertions.assertEquals(0, cart.getProducts().size(),
                "Корзина должна отчищаться после покупки");
    }

    /**
     * Тест. Неудачное совершение покупки
     * Попытка покупки пустой корзины
     */
    @Test
    void testUnsuccessfulBuy() throws BuyException {
        Assertions.assertFalse(shoppingService.buy(cart));
    }

    /**
     * Тест. Ошибка при покупке BuyException
     * попытка купить больше продукта, чем есть в наличии
     */
    @Test
    void testThrowBuyException() {
        String productName = "bread";
        Product product = new Product(productName, 3);

        Mockito.when(productDaoMock.getByName(productName))
                .thenReturn(product);
        cart.add(product, 2);
        product.subtractCount(2);

        BuyException buyException = Assertions.assertThrows(BuyException.class, () ->
                shoppingService.buy(cart));
        Assertions.assertEquals("В наличии нет необходимого количества товара 'bread'",
                buyException.getMessage());
    }
}