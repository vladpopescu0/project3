package nl.tudelft.sem.template.order.domain.user;

import com.fasterxml.jackson.core.type.TypeReference;
import nl.tudelft.sem.template.order.commons.Address;
import nl.tudelft.sem.template.order.commons.Dish;
import nl.tudelft.sem.template.order.commons.Order;
import nl.tudelft.sem.template.order.domain.user.repositories.DishRepository;
import nl.tudelft.sem.template.order.domain.user.repositories.OrderRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTests {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private DishRepository dishRepository;

    @InjectMocks
    private OrderService orderService;

    Dish d1;
    Dish d2;
    Order order1;
    Order order2;

    @BeforeEach
    void setup() {
        d1 = new Dish();
        d1.setDishID(UUID.randomUUID());
        d1.setDescription("very tasty");
        d1.setImage("img");
        d1.setName("Pizza");
        d1.setPrice(5.0f);
        List<String> allergies = new ArrayList<>();
        allergies.add("lactose");
        d1.setListOfAllergies(allergies);
        List<String> ingredients = new ArrayList<>();
        ingredients.add("Cheese");
        ingredients.add("Salami");
        ingredients.add("Tomato Sauce");
        d1.setListOfIngredients(ingredients);
        d1.setVendorID(UUID.randomUUID());

        d2 = new Dish();
        d2.setDishID(d1.getVendorID());
        d2.setDescription("very tasty");
        d2.setImage("img");
        d2.setName("Lasagna");
        d2.setPrice(10.0f);
        List<String> allergies2 = new ArrayList<>();
        allergies2.add("lactose");
        allergies2.add("gluten");
        d2.setListOfAllergies(allergies2);
        List<String> ingredients2 = new ArrayList<>();
        ingredients2.add("Gluten");
        ingredients2.add("Cheese");
        ingredients2.add("Tomato Sauce");
        d2.setListOfIngredients(ingredients2);
        d2.setVendorID(UUID.randomUUID());

        Address a1 = new Address();
        a1.setStreet("Mekelweg 5");
        a1.setCity("Delft");
        a1.setCountry("Netherlands");
        a1.setZip("2628CC");

        List<UUID> listOfDishes1 = List.of(d1.getDishID(),d2.getDishID());

        order1 = new Order();
        order1.setOrderID(UUID.randomUUID());
        order1.setVendorID(d1.getVendorID());
        order1.setCustomerID(UUID.randomUUID());
        order1.setAddress(a1);
        order1.setDate(new BigDecimal("1700007405000"));
        order1.setListOfDishes(listOfDishes1);
        order1.setSpecialRequirements("Knock on the door");
        order1.setOrderPaid(true);
        order1.setStatus(Order.StatusEnum.DELIVERED);
        order1.setRating(4);

        Address a2 = new Address();
        a2.setStreet("Mekelweg 9");
        a2.setCity("Delft");
        a2.setCountry("Netherlands");
        a2.setZip("2628CD");

        List<UUID> listOfDishes2 = Collections.singletonList(d1.getDishID());

        order2 = new Order();
        order2.setOrderID(UUID.randomUUID());
        order2.setVendorID(d1.getVendorID());
        order2.setCustomerID(UUID.randomUUID());
        order2.setAddress(a1);
        order2.setDate(new BigDecimal("1790006416060"));
        order2.setListOfDishes(listOfDishes2);
        order2.setSpecialRequirements("Knock on the door");
        order2.setOrderPaid(true);
        order2.setStatus(Order.StatusEnum.ACCEPTED);
        order2.setRating(4);
    }

    @Test
    void testCheckUUIDIsUnique_WhenExists() {
        UUID existingUUID = UUID.randomUUID();
        when(orderRepository.existsByOrderID(existingUUID)).thenReturn(true);

        boolean isUnique = orderService.checkUUIDIsUnique(existingUUID);

        Assertions.assertTrue(isUnique);
    }

    @Test
    void testCheckUUIDIsUnique_WhenNotExists() {
        UUID nonExistingUUID = UUID.randomUUID();
        when(orderRepository.existsByOrderID(nonExistingUUID)).thenReturn(false);

        boolean isUnique = orderService.checkUUIDIsUnique(nonExistingUUID);

        Assertions.assertFalse(isUnique);
    }

    @Test
    void testOrderIsPaid_WhenOrderExistsAndIsPaid() throws OrderNotFoundException {
        UUID orderID = UUID.randomUUID();
        Order paidOrder = new Order();
        paidOrder.setOrderID(orderID);
        paidOrder.setOrderPaid(true);

        when(orderRepository.existsByOrderID(orderID)).thenReturn(true);
        when(orderRepository.findOrderByOrderID(orderID)).thenReturn(Optional.of(paidOrder));

        boolean isPaid = orderService.orderIsPaid(orderID);

        Assertions.assertTrue(isPaid);
    }

    @Test
    void testOrderIsPaid_WhenOrderExistsAndIsNotPaid() throws OrderNotFoundException {
        UUID orderID = UUID.randomUUID();
        Order unpaidOrder = new Order();
        unpaidOrder.setOrderID(orderID);
        unpaidOrder.setOrderPaid(false);

        when(orderRepository.existsByOrderID(orderID)).thenReturn(true);
        when(orderRepository.findOrderByOrderID(orderID)).thenReturn(Optional.of(unpaidOrder));

        boolean isPaid = orderService.orderIsPaid(orderID);

        Assertions.assertFalse(isPaid);
    }

    @Test
    void testOrderIsPaid_WhenOrderDoesNotExist() {
        UUID nonExistingOrderID = UUID.randomUUID();

        when(orderRepository.existsByOrderID(nonExistingOrderID)).thenReturn(false);

        Assertions.assertThrows(OrderNotFoundException.class, () -> orderService.orderIsPaid(nonExistingOrderID));
    }

    @Test
    void getOrdersFromCostumerAtVendor_VendorDoesNotExist(){
        UUID nonExistingVendorID = UUID.randomUUID();

        when(orderRepository.existsByVendorID(nonExistingVendorID)).thenReturn(false);

        Assertions.assertThrows(VendorNotFoundException.class, () -> orderService.getOrdersFromCostumerAtVendor(nonExistingVendorID,order1.getCustomerID()));
    }

    @Test
    void getOrdersFromCostumerAtVendor_CustomerDoesNotExist(){
        UUID nonExistingCustomerID = UUID.randomUUID();

        when(orderRepository.existsByVendorID(order1.getVendorID())).thenReturn(true);
        when(orderRepository.existsByCustomerID(nonExistingCustomerID)).thenReturn(false);

        Assertions.assertThrows(CustomerNotFoundException.class, () -> orderService.getOrdersFromCostumerAtVendor(order1.getVendorID(),nonExistingCustomerID));
    }

    @Test
    void getOrdersFromCostumerAtVendor_EmptyOrder(){
        when(orderRepository.existsByVendorID(order1.getVendorID())).thenReturn(true);
        when(orderRepository.existsByCustomerID(order1.getCustomerID())).thenReturn(true);
        when(orderRepository.findOrdersByVendorIDAndCustomerID(order1.getVendorID(),order1.getCustomerID())).thenReturn(Optional.empty());

        Assertions.assertThrows(NoOrdersException.class, () -> orderService.getOrdersFromCostumerAtVendor(order1.getVendorID(),order1.getCustomerID()));
    }

    @Test
    void getOrdersFromCostumerAtVendor_NonEmptyResult() throws VendorNotFoundException, CustomerNotFoundException, NoOrdersException {
        when(orderRepository.existsByVendorID(order1.getVendorID())).thenReturn(true);
        when(orderRepository.existsByCustomerID(order1.getCustomerID())).thenReturn(true);
        when(orderRepository.findOrdersByVendorIDAndCustomerID(order1.getVendorID(),order1.getCustomerID())).thenReturn(Optional.of(List.of(order1,order2)));

        List<Order> result = orderService.getOrdersFromCostumerAtVendor(order1.getVendorID(),order1.getCustomerID());

        assertThat(result).isEqualTo(List.of(order1,order2));
    }

    @Test
    void getOrderVolume_VendorDoesNotExist() {
        UUID nonExistingVendorID = UUID.randomUUID();

        when(orderRepository.existsByVendorID(nonExistingVendorID)).thenReturn(false);

        Assertions.assertThrows(VendorNotFoundException.class, () -> orderService.getOrderVolume(nonExistingVendorID));
    }

    @Test
    void getOrderVolume_NoOrders() {
        when(orderRepository.existsByVendorID(order1.getVendorID())).thenReturn(true);
        when(orderRepository.countOrderByVendorID(order1.getVendorID())).thenReturn(Optional.empty());

        Assertions.assertThrows(NoOrdersException.class, () -> orderService.getOrderVolume(order1.getVendorID()));
    }

    @Test
    void getOrderVolume_WithOrders() throws VendorNotFoundException, NoOrdersException {
        when(orderRepository.existsByVendorID(order1.getVendorID())).thenReturn(true);
        when(orderRepository.countOrderByVendorID(order1.getVendorID())).thenReturn(Optional.of(21));

        assertThat(orderService.getOrderVolume(order1.getVendorID())).isEqualTo(21);
    }

    @Test
    void getDishesSortedByVolume_VendorDoesNotExist(){
        UUID nonExistingVendorID = UUID.randomUUID();

        when(orderRepository.existsByVendorID(nonExistingVendorID)).thenReturn(false);

        Assertions.assertThrows(VendorNotFoundException.class, () -> orderService.getDishesSortedByVolume(nonExistingVendorID));
    }

    @Test
    void getDishesSortedByVolume_DishNotFound(){
        List<Object> byteList = new ArrayList<>();
        for(UUID curId : List.of(d1.getDishID(),d2.getDishID())) {
            String id = curId.toString().replace("-", "");
            byte[] idBytes = new byte[16];
            for (int i = 0; i < 16; i++) {
                int cur = Integer.parseInt(id.substring(2 * i, 2 * i + 2), 16);
                if (cur >= 128) {
                    cur -= 256;
                }
                idBytes[i] = (byte) cur;
            }
            byteList.add(idBytes);
        }

        when(orderRepository.existsByVendorID(order1.getVendorID())).thenReturn(true);
        when(orderRepository.countDishesOccurrencesFromVendor(order1.getVendorID())).thenReturn(byteList);
        when(dishRepository.findDishByDishID(d1.getDishID())).thenReturn(Optional.of(d1));
        when(dishRepository.findDishByDishID(d2.getDishID())).thenReturn(Optional.empty());

        Assertions.assertThrows(DishNotFoundException.class, () -> orderService.getDishesSortedByVolume(order1.getVendorID()));
    }

    @Test
    void getDishesSortedByVolume_DishesFound() throws VendorNotFoundException, DishNotFoundException {
        List<Object> byteList = new ArrayList<>();
        for(UUID curId : List.of(d2.getDishID(),d1.getDishID())) {
            String id = curId.toString().replace("-", "");
            byte[] idBytes = new byte[16];
            for (int i = 0; i < 16; i++) {
                int cur = Integer.parseInt(id.substring(2 * i, 2 * i + 2), 16);
                if (cur >= 128) {
                    cur -= 256;
                }
                idBytes[i] = (byte) cur;
            }
            byteList.add(idBytes);
        }

        when(orderRepository.existsByVendorID(order1.getVendorID())).thenReturn(true);
        when(orderRepository.countDishesOccurrencesFromVendor(order1.getVendorID())).thenReturn(byteList);
        when(dishRepository.findDishByDishID(d1.getDishID())).thenReturn(Optional.of(d1));
        when(dishRepository.findDishByDishID(d2.getDishID())).thenReturn(Optional.of(d2));

        List<Dish> res = orderService.getDishesSortedByVolume(order1.getVendorID());
        assertThat(res.size()).isEqualTo(2);
        assertThat(res.get(0)).isEqualTo(d2);
        assertThat(res.get(1)).isEqualTo(d1);
    }

    @Test
    void getOrderVolumeByTime_VendorNotFound() {
        UUID nonExistingVendorID = UUID.randomUUID();

        when(orderRepository.existsByVendorID(nonExistingVendorID)).thenReturn(false);

        Assertions.assertThrows(VendorNotFoundException.class, () -> orderService.getOrderVolumeByTime(nonExistingVendorID));
    }

    @Test
    void getOrderVolumeByTime_NoOrders() throws VendorNotFoundException, NoOrdersException {
        when(orderRepository.existsByVendorID(order1.getVendorID())).thenReturn(true);
        when(orderRepository.findOrdersByVendorID(order1.getVendorID())).thenReturn(Optional.empty());

        Assertions.assertThrows(NoOrdersException.class, () -> orderService.getOrderVolumeByTime(order1.getVendorID()));
    }

    @Test
    void getOrderVolumeByTime_LargeTest() throws VendorNotFoundException, NoOrdersException {
        Random r = new Random();
        int[] time = new int[24];
        List<Order> orders = new ArrayList<>();
        for(int i = 0; i<1000; i++){
            long cur = r.nextInt();
            Order o = new Order();
            o.setDate(BigDecimal.valueOf(cur));
            o.setListOfDishes(new ArrayList<>());
            orders.add(o);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(cur);
            int hours = calendar.get(Calendar.HOUR_OF_DAY);
            time[hours]++;
        }
        List<Integer> correctTime = new ArrayList<>();
        for (int j : time) {
            correctTime.add(j);
        }

        when(orderRepository.existsByVendorID(order1.getVendorID())).thenReturn(true);
        when(orderRepository.findOrdersByVendorID(order1.getVendorID())).thenReturn(Optional.of(orders));

        List<Integer> volume = orderService.getOrderVolumeByTime(order1.getVendorID());
        assertThat(volume).isEqualTo(correctTime);
    }
}
