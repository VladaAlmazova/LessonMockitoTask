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
 * <p>
 * Ошибки:
 * <li>Корзина не привязана к пользователю. Поле customer в классе Card никак не используется</li>
 * <li>Метод getCart() выдает новый экземпляр корзины</li>
 * <li>Нет возможности купить все количество товара что есть</li>
 * <li>Корзина после совершения покупки не очищается, это не соответствует интерфейсу</li>
 * <li>Покупка отрицательного количества товара никак не отслеживается</li>
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
     * Инициализация полей сервис покупок и корзина
     */
    @BeforeEach
    void initShoppingServiceAndCart() {
        shoppingService = new ShoppingServiceImpl(productDaoMock);
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
     * Тест. Получение корзины покупателя
     * <li>Корзины разных покупателей не должны совпадать, так как там лежат разные продукты</li>
     * <li>В корзине должны лежать только добавленные в нее продукты</li>
     */
    @Test
    void testGetCart() {
        Customer customer1 = new Customer(1L, "11-11-11");
        Customer customer2 = new Customer(2L, "22-22-22");

        Product product1 = new Product("bread", 2);
        Product product2 = new Product("milk", 3);

        shoppingService.getCart(customer1).add(product1, 1);
        shoppingService.getCart(customer2).add(product2, 2);

        Cart cart1 = shoppingService.getCart(customer1);
        Cart cart2 = shoppingService.getCart(customer2);

        Assertions.assertNotEquals(cart1.getProducts(), cart2.getProducts());
        Assertions.assertTrue(cart1.getProducts().containsKey(product1) &&
                !cart1.getProducts().containsKey(product2));
    }

    /**
     * Тест. Удачное совершение покупки
     * <li>Создаются разные покупатели и разные товары</li>
     * <li>Покупателям добавляются в корзину разные товары</li>
     * <li>Производится покупка обеих корзин</li>
     * <p>Проверки</p>
     * <li>Покупки должны завершиться успешно</li>
     * <li>Количество товара в наличии должно измениться</li>
     * <li>Измененное количество должно быть сохранено в БД</li>
     * <li>Обе корзины должны очиститься после совершения покупки</li>
     */
    @Test
    void testSuccessfulBuy() throws BuyException {

        Customer customer1 = new Customer(1L, "11-11-11");
        Customer customer2 = new Customer(2L, "22-22-22");

        Product product1 = new Product("bread", 2);
        Product product2 = new Product("milk", 3);

        shoppingService.getCart(customer1).add(product1, 1);
        shoppingService.getCart(customer2).add(product2, 3);

        boolean resultBuyCart1 = shoppingService.buy(shoppingService.getCart(customer1));
        boolean resultBuyCart2 = shoppingService.buy(shoppingService.getCart(customer2));

        //покупки должны пройти успешно
        Assertions.assertTrue(resultBuyCart1 && resultBuyCart2);

        //изменилось количество товара
        Assertions.assertEquals(1, product1.getCount(),
                "Количество товара должно измениться после покупки");
        Assertions.assertEquals(0, product2.getCount(),
                "Количество товара должно измениться после покупки");

        //изменения сохранены в БД
        Mockito.verify(productDaoMock).save(product1);
        Mockito.verify(productDaoMock).save(product2);

        //Корзина отчистилась
        Assertions.assertEquals(0, shoppingService.getCart(customer1).getProducts().size(),
                "Корзина должна очищаться после покупки");
        Assertions.assertEquals(0, shoppingService.getCart(customer2).getProducts().size(),
                "Корзина должна отчищаться после покупки");
    }

    /**
     * Тест. Неудачное совершение покупки
     * Попытка покупки пустой корзины
     */
    @Test
    void testUnsuccessfulBuy() throws BuyException {

        Customer customer = new Customer(1L, "11-11-11");
        Assertions.assertFalse(shoppingService.buy(shoppingService.getCart(customer)));
    }

    /**
     * Тест. Ошибка при покупке BuyException
     * попытка купить больше продукта, чем есть в наличии
     */
    @Test
    void testThrowBuyException() throws BuyException {
        Customer customer1 = new Customer(1L, "11-11-11");
        Customer customer2 = new Customer(2L, "22-22-22");
        Product product = new Product("bread", 3);

        Cart cart1 = shoppingService.getCart(customer1);
        Cart cart2 = shoppingService.getCart(customer2);
        cart1.add(product, 2);
        cart2.add(product, 2);

        shoppingService.buy(cart1);

        BuyException buyException = Assertions.assertThrows(BuyException.class, () ->
                shoppingService.buy(cart2));
        Assertions.assertEquals("В наличии нет необходимого количества товара 'bread'",
                buyException.getMessage());
    }

    /**
     * Тест. Ошибка при покупке BuyException
     * попытка купить отрицательное количество товара
     */
    @Test
    void testThrowBuyExceptionNegativeQuantity() {
        Customer customer = new Customer(1L, "11-11-11");
        Product product = new Product("bread", 3);
        Cart cart = shoppingService.getCart(customer);

        cart.add(product, -2);

        BuyException buyException = Assertions.assertThrows(BuyException.class, () ->
                shoppingService.buy(cart));
        Assertions.assertEquals("Нельзя приобрести отрицательное количество товара 'bread'",
                buyException.getMessage());
    }
}