package nl.tudelft.sem.template.user.api;

import java.util.List;
import java.util.UUID;
import nl.tudelft.sem.template.model.Address;
import nl.tudelft.sem.template.order.domain.user.UserIDNotFoundException;

public interface UserMicroServiceAPI {

    Address getUserAddress(UUID userID) throws UserIDNotFoundException;

    String getUserLocation(UUID userID) throws UserIDNotFoundException;

    List<String> getAllVendors();
}