package nl.tudelft.sem.template.order.controllers;

import java.util.UUID;
import nl.tudelft.sem.template.order.api.OrderApi;
import nl.tudelft.sem.template.order.domain.user.OrderNotFoundException;
import nl.tudelft.sem.template.order.domain.user.OrderService;
import nl.tudelft.sem.template.order.domain.user.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController implements OrderApi {

    private final transient OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * OrderID isPaid controller methods.
     * It throws a 402 if the order is not paid and a 404 if the order is not found.
     *
     * @param orderID the id of the order to be checked
     * @return the http response as said in the description
     */
    @Override
    public ResponseEntity<Void> orderOrderIDIsPaidGet(UUID orderID) {
        try {
            boolean isPaid = orderService.orderIsPaid(orderID);
            if (isPaid) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(402).build();
            }
        } catch (OrderNotFoundException notFound) {
            return ResponseEntity.notFound().build();
        }
    }

    //Methods
}