package nl.tudelft.sem.template.order.domain.user;

import nl.tudelft.sem.template.order.commons.Dish;
import nl.tudelft.sem.template.order.domain.user.repositories.DishRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DishService {
    private final transient DishRepository dishRepository;

    @Autowired
    public DishService(DishRepository dishRepository) {
        this.dishRepository = dishRepository;
    }

    public Dish addDish(Dish dish) throws DishIdAlreadyInUseException {
        if(!checkUUIDIsUnique(dish.getDishID())){
            throw new DishIdAlreadyInUseException(dish.getDishID());
        }
        dish = dishRepository.save(dish);
        return dish;
    }

    public boolean checkUUIDIsUnique(UUID uuid) {
        return !dishRepository.existsByDishID(uuid);
    }

    public Dish getDishById(UUID dishID) throws DishNotFoundException {
        Optional<Dish> databaseDish = dishRepository.findDishByDishID(dishID);
        if(databaseDish.isEmpty()){
            throw new DishNotFoundException(dishID);
        }
        return databaseDish.get();
    }

    public List<Dish> getDishByVendorId(UUID vendorID) throws VendorNotFoundException {
        Optional<List<Dish>> databaseDishes = dishRepository.findDishesByVendorID(vendorID);
        if(databaseDishes.isEmpty()){
            throw new VendorNotFoundException(vendorID);
        }
        return databaseDishes.get();
    }

    public Dish updateDish(UUID dishID, Dish dish) throws DishNotFoundException {
        if(checkUUIDIsUnique(dishID)){
            throw new DishNotFoundException(dishID);
        }
        dish = dishRepository.save(dish);
        return dish;
    }

    public void deleteDishByDishId(UUID dishID) throws DishNotFoundException {
        if(checkUUIDIsUnique(dishID)){
            throw new DishNotFoundException(dishID);
        }
        dishRepository.deleteById(dishID);
    }

    public List<Dish> getAllergyFilteredDishesFromVendor(UUID vendorID, List<String> allergies) throws VendorNotFoundException {
        if(!dishRepository.existsByVendorID(vendorID)) {
            throw new VendorNotFoundException(vendorID);
        }
        Optional<List<Dish>> databaseDishes = dishRepository.findDishesByVendorIDAndListOfAllergies(vendorID, allergies);
        return databaseDishes.orElseGet(ArrayList::new);
    }
}
