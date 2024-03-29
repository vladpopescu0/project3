package nl.tudelft.sem.template.order.domain.user;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import nl.tudelft.sem.template.model.Address;
import nl.tudelft.sem.template.order.domain.helpers.Coordinates;
import nl.tudelft.sem.template.user.api.UserMicroServiceAPI;
import nl.tudelft.sem.template.user.services.JsonParserService;
import nl.tudelft.sem.template.user.services.MockLocationService;
import nl.tudelft.sem.template.user.services.UserMicroServiceService;
import org.springframework.stereotype.Service;

/**
 * The type Restaurant service.
 */
@Service
public class RestaurantService {

    private final transient UserMicroServiceAPI userMicroServiceService;
    private final transient MockLocationService mockedLocationService;


    /**
     * Instantiates a new Restaurant service.
     *
     * @param userMicroServiceService the user microservice service
     * @param mockedLocationService mocked location service
     */
    public RestaurantService(UserMicroServiceService userMicroServiceService, MockLocationService mockedLocationService) {
        this.userMicroServiceService = userMicroServiceService;
        this.mockedLocationService = mockedLocationService;
    }

    /**
     *  Gets all restaurants.
     *  Algorithm:
     *  Get userLocation based on ID,
     *  Get all the vendors,
     *  filter the vendors on distance from the user
     *
     * @param userID the user id of the customer
     * @return list of UUID from the vendors in a specific radius
     * @throws RuntimeException in case of other exceptions, just throw RunTimeException
     */
    public List<UUID> getAllRestaurants(UUID userID) throws RuntimeException {

        // get vendor location and UUID
        try {
            String jsonVendors = userMicroServiceService.getAllVendors();
            HashMap<UUID, List<Double>> vendors = JsonParserService.parseVendorsLocation(jsonVendors);
            // option: error thrown in the JsonParserService could be caught in this try catch
            if (vendors == null || vendors.isEmpty()) {
                throw new RuntimeException("Something went wrong parsing vendors");
            }
            // try to get the user address first
            List<Double> userLocation = getUserLocation(userID);
            return processVendors(userLocation, vendors);
        } catch (Exception e) {
            throw new RuntimeException("Could not get vendors");
        }
    }

    /**
     * getter for the location of the user.
     *
     * @param userID UUID of the user
     * @return List of doubles representing the latitude (index=0) and longitude (index=1)
     * @throws UserIDNotFoundException if the user is not found
     */
    public List<Double> getUserLocation(UUID userID) throws UserIDNotFoundException {
        try {
            Address userAddress = userMicroServiceService.getUserAddress(userID);
            // this always returns the geo coordinates of TU Aula, unless we catch an error
            return mockedLocationService.convertAddressToGeoCoords(userAddress);
        } catch (Exception e) {

            // if we catch an error, then get the user's current location
            return userLocationHandler(userID);
        }
    }

    /** Handles the checking for user existence.
     *
     * @param userID the id of the checked user
     * @return the checked userLocation
     * @throws UserIDNotFoundException if the user does not exist
     */
    private List<Double> userLocationHandler(UUID userID) throws UserIDNotFoundException {
        try {
            String jsonUser = userMicroServiceService.getUserLocation(userID);
            if (jsonUser == null || jsonUser.isEmpty()) { // in case getUserLocation timed out.
                throw new UserIDNotFoundException(userID);
            }
            List<Double> userLocation = JsonParserService.parseLocation(jsonUser);
            // option: error thrown in the JsonParserService could be caught in this try catch
            if (userLocation == null) {
                throw new RuntimeException("Something went wrong parsing location");
            }
            return userLocation;
        } catch (Exception ex) {
            throw new UserIDNotFoundException(userID);
        }
    }

    /**
     * Filters vendors based on their proximity to the customer's location within a specified radius.
     *
     * @param userLocation the location of the user
     * @param vendors vendors that need to be filtered
     * @return a list of vendor UUIDs
     */
    public List<UUID> processVendors(List<Double> userLocation, HashMap<UUID, List<Double>> vendors) {
        return vendors.entrySet().stream()
                .filter(entry -> calculateDistance(new Coordinates(userLocation.get(0), userLocation.get(1)),
                        new Coordinates(entry.getValue().get(0), entry.getValue().get(1))) < 5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * (Consideration: move this to an utils class).
     * Calculate distance of two points (geoCoordinates).
     * Algorithm found at:
     * <a href="https://stackoverflow.com/questions/27928/calculate-distance-between-two-latitude-longitude-points-haversine-formula">...</a>
     *
     * @param user latitude and longitude of user
     * @param vendor latitude and longitude of vendor
     * @return the distance between two geo coordinate points
     */
    public double calculateDistance(Coordinates user,
                                    Coordinates vendor) {
        // Calculate distance between two points using Haversine formula
        final double r = 6371D; // radius of Earth in km
        final double p = Math.PI / 180;
        double vendorLatitude = vendor.latitude;
        double vendorLongitude = vendor.longitude;
        double userLatitude = user.latitude;
        double userLongitude = user.longitude;

        double a = 0.5 - Math.cos((vendorLatitude - userLatitude) * p) / 2
                + Math.cos(userLatitude * p) * Math.cos(vendorLatitude * p)
                * (1 - Math.cos((vendorLongitude - userLongitude) * p)) / 2;
        return 2 * r * Math.asin(Math.sqrt(a));
    }

    /**
     * Gets all restaurants with query.
     *
     * @param userID the user id of the customer
     * @param query  the query of the customer
     * @return list of UUID from the restaurants filtered by the query
     * @throws RuntimeException could not get restaurants or userID
     */
    public List<UUID> getAllRestaurantsWithQuery(UUID userID, String query) throws RuntimeException {
        List<UUID> restaurantsID = getAllRestaurants(userID); // may throw error
        List<String> restaurantsJson = userMicroServiceService.getVendorsFromID(restaurantsID);
        HashMap<UUID, String> restaurantsCuisines = JsonParserService.parseVendorCuisine(restaurantsJson);
        if (restaurantsCuisines == null) {
            throw new RuntimeException("No restaurants found");
        }
        return processVendorsByQuery(restaurantsCuisines, query);
    }

    /**
     * Process vendors by query.
     *
     * @param restaurantsCuisines hashmap of UUID and cuisine of vendors
     * @param query               the query
     * @return the list
     */
    public List<UUID> processVendorsByQuery(HashMap<UUID, String> restaurantsCuisines, String query) {
        return restaurantsCuisines.entrySet().stream()
                .filter(entry -> entry.getValue().toLowerCase(Locale.ENGLISH).contains(query.toLowerCase(Locale.ENGLISH)))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

}
