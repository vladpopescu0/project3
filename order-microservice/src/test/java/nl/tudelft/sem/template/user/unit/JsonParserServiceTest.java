package nl.tudelft.sem.template.user.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import nl.tudelft.sem.template.user.services.JsonParserService;
import org.junit.jupiter.api.Test;


class JsonParserServiceTest {

    transient String jsonValid = """
            {
              "latitude": 51.998513,
              "longitude": 4.37127
            }""";
    transient String jsonNoLat = """
            {
              "latitude": ,
              "longitude": 4.37127
            }""";
    transient String jsonNoLong = """
            {
              "latitude": 51.998513,
              "longitude":
            }""";
    transient String jsonString = """
            {
              "latitude": oh hi,
              "longitude": 4.37127
            }""";
    transient String jsonExtraAttr = """
            {
              "latitude": 51.998513,
              "longitude": 4.37127,
              "angle": 30
            }""";
    transient String vendor2 = """
            [
                {
                    "userID": "550e8400-e29b-41d4-a716-446655440000",
                    "user": {
                        "id": "550e8400-e29b-41d4-a716-446655440000",
                        "firstname": "John",
                        "surname": "James",
                        "email": "john@email.com",
                        "avatar": "www.avatar.com/avatar.png",
                        "password": "12345",
                        "verified": false,
                        "userType": "Customer"
                    },
                    "address": {
                        "street": "437 Lytton",
                        "city": "Palo Alto",
                        "country": "Germany",
                        "zip": "2511RE"
                    },
                    "location": {
                        "latitude": 51.998513,
                        "longitude": 4.37127
                    }
                },
                {
                    "userID": "110e8400-e29b-41d4-a716-446655440000",
                    "user": {
                        "id": "110e8400-e29b-41d4-a716-446655440000",
                        "firstname": "John",
                        "surname": "James",
                        "email": "john@email.com",
                        "avatar": "www.avatar.com/avatar.png",
                        "password": "12345",
                        "verified": false,
                        "userType": "Customer"
                    },
                    "address": {
                        "street": "437 Lytton",
                        "city": "Palo Alto",
                        "country": "Germany",
                        "zip": "2511RE"
                    },
                    "location": {
                        "latitude": 5.998513,
                        "longitude": 41.37127
                    }
                }
            ]""";
    transient String vendor2NotValid = """
            [
                {
                    "userID": "550e8400-e29b-41d4-a716-446655440000",
                    "user": {
                        "id": "550e8400-e29b-41d4-a716-446655440000",
                        "firstname": "John",
                        "surname": "James",
                        "email": "john@email.com",
                        "avatar": "www.avatar.com/avatar.png",
                        "password": "12345",
                        "verified": false,
                        "userType": "Customer"
                    },
                    "address": {
                        "street": "437 Lytton",
                        "city": "Palo Alto",
                        "country": "Germany",
                        "zip": "2511RE"
                    },
                    "location": {
                        "latitude": 51.998513,
                        "longitude": 4.37127
                    }
                },
                {
                    "userID": "110e8400-e29b-41d4-a716-446655440000",
                    "user": {
                        "id": "110e8400-e29b-41d4-a716-446655440000",
                        "firstname": "John",
                        "surname": "James",
                        "email": "john@email.com",
                        "avatar": "www.avatar.com/avatar.png",
                        "password": "12345",
                        "verified": false,
                        "userType": "Customer"
                    },
                    "address": {
                        "street": "437 Lytton",
                        "city": "Palo Alto",
                        "country": "Germany",
                        "zip": "2511RE"
                    }
                }
            ]""";
    transient String vendor2NoLoc = """
            [
                {
                    "userID": "550e8400-e29b-41d4-a716-446655440000",
                    "user": {
                        "id": "550e8400-e29b-41d4-a716-446655440000",
                        "firstname": "John",
                        "surname": "James",
                        "email": "john@email.com",
                        "avatar": "www.avatar.com/avatar.png",
                        "password": "12345",
                        "verified": false,
                        "userType": "Customer"
                    },
                    "address": {
                        "street": "437 Lytton",
                        "city": "Palo Alto",
                        "country": "Germany",
                        "zip": "2511RE"
                    },
                    "location": {
                        "latitude": 51.998513,
                        "longitude": 4.37127
                    }
                },
                {
                    "userID": "110e8400-e29b-41d4-a716-446655440000",
                    "user": {
                        "id": "110e8400-e29b-41d4-a716-446655440000",
                        "firstname": "John",
                        "surname": "James",
                        "email": "john@email.com",
                        "avatar": "www.avatar.com/avatar.png",
                        "password": "12345",
                        "verified": false,
                        "userType": "Customer"
                    },
                    "address": {
                        "street": "437 Lytton",
                        "city": "Palo Alto",
                        "country": "Germany",
                        "zip": "2511RE"
                    },
                    "location": {
                        "latitude": 5.998513,
                        "longitude": "oh hi"
                    }
                }
            ]""";
    transient String vendor1InValidUUID = """
              {
                "userID": "oh hi",
                "user": {
                  "id": "550e8400-e29b-41d4-a716-446655440000",
                  "firstname": "John",
                  "surname": "James",
                  "email": "john@email.com",
                  "avatar": "www.avatar.com/avatar.png",
                  "password": "12345",
                  "verified": false,
                  "userType": "Customer"
                },
                "address": {
                  "street": "437 Lytton",
                  "city": "Palo Alto",
                  "country": "Germany",
                  "zip": "2511RE"
                },
                "location": {
                  "latitude": 51.998513,
                  "longitude": 4.37127
                }
              }
            """;
    transient String vendorInvalidJson = """
              {
                userID : "oh hi",
                "user": {
                  "id": "550e8400-e29b-41d4-a716-446655440000",
                  "firstname": "John",
                  "surname": "James",
                  "email": "john@email.com",
                  "avatar": "www.avatar.com/avatar.png",
                  "password": "12345",
                  "verified": false,
                  "userType": "Customer"
                },
                "address": {
                  "street": "437 Lytton",
                  "city": "Palo Alto",
                  "country": "Germany",
                  "zip": "2511RE"
                },
                "location": {
                  "latitude": 51.998513,
                  "longitude": 4.37127
                }
              }
            """;

    transient String customerJson = """
            {
              "id": "550e8400-e29b-41d4-a716-446655440000",
              "firstname": "John",
              "surname": "James",
              "email": "john@email.com",
              "avatar": "www.avatar.com/avatar.png",
              "password": "12345",
              "verified": false,
              "userType": "Customer"
            }""";

    transient String vendorJson = """
            {
              "id": "550e8400-e29b-41d4-a716-446655440000",
              "firstname": "John",
              "surname": "James",
              "email": "john@email.com",
              "avatar": "www.avatar.com/avatar.png",
              "password": "12345",
              "verified": false,
              "userType": "Vendor"
            }""";

    transient String courierJson = """
            {
              "id": "550e8400-e29b-41d4-a716-446655440000",
              "firstname": "John",
              "surname": "James",
              "email": "john@email.com",
              "avatar": "www.avatar.com/avatar.png",
              "password": "12345",
              "verified": false,
              "userType": "Courier"
            }""";

    transient String adminJson = """
            {
              "id": "550e8400-e29b-41d4-a716-446655440000",
              "firstname": "John",
              "surname": "James",
              "email": "john@email.com",
              "avatar": "www.avatar.com/avatar.png",
              "password": "12345",
              "verified": false,
              "userType": "Admin"
            }""";
    transient UUID uuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    @Test
    void parseLocationValid() {
        List<Double> list = Arrays.asList(51.998513, 4.37127);
        List<Double> result = JsonParserService.parseLocation(jsonValid);
        assertEquals(list, result);
    }

    @Test
    void parseLocationNoLat() {
        List<Double> result = JsonParserService.parseLocation(jsonNoLat);
        assertNull(result);
    }

    @Test
    void parseLocationNoLong() {
        List<Double> result = JsonParserService.parseLocation(jsonNoLong);
        assertNull(result);
    }

    @Test
    void parseLocationString() {
        List<Double> result = JsonParserService.parseLocation(jsonString);
        assertNull(result);
    }

    @Test
    void parseLocationExtraAttr() {
        List<Double> list = Arrays.asList(51.998513, 4.37127);
        List<Double> result = JsonParserService.parseLocation(jsonExtraAttr);
        assertEquals(list, result);
    }
    

    @Test
    void parseVendorsLocationEmpty() {
        HashMap<UUID, List<Double>> result = JsonParserService.parseVendorsLocation("");
        assertNull(result);
    }

    @Test
    void parseVendorsLocationValid() {
        HashMap<UUID, List<Double>> hashMap = new HashMap<>();
        hashMap.put(uuid, Arrays.asList(51.998513, 4.37127));
        hashMap.put(UUID.fromString("110e8400-e29b-41d4-a716-446655440000"), Arrays.asList(5.998513, 41.37127));
        HashMap<UUID, List<Double>> result = JsonParserService.parseVendorsLocation(vendor2);
        assertEquals(hashMap, result);
    }

    @Test
    void parseVendorsLocationNoLocation() {
        HashMap<UUID, List<Double>> hashMap = new HashMap<>();
        hashMap.put(uuid, Arrays.asList(51.998513, 4.37127));
        HashMap<UUID, List<Double>> result = JsonParserService.parseVendorsLocation(vendor2NotValid);
        assertEquals(hashMap, result);
    }

    @Test
    void parseVendorsLocationMissingLocation() {
        HashMap<UUID, List<Double>> hashMap = new HashMap<>();
        hashMap.put(uuid, Arrays.asList(51.998513, 4.37127));
        HashMap<UUID, List<Double>> result = JsonParserService.parseVendorsLocation(vendor2NoLoc);
        assertEquals(hashMap, result);
    }

    @Test
    void parseVendorsLocationInvalidUUID() {
        HashMap<UUID, List<Double>> hashMap = new HashMap<>();
        HashMap<UUID, List<Double>> result = JsonParserService.parseVendorsLocation(vendor1InValidUUID);
        assertEquals(hashMap, result);
    }

    @Test
    void parseVendorsLocationInvalidJson() {
        HashMap<UUID, List<Double>> result = JsonParserService.parseVendorsLocation(vendorInvalidJson);
        assertNull(result);
    }

    @Test
    void parseUserTypeJsonIsNull() {
        String result = JsonParserService.parseUserType(null);
        assertNull(result);
    }

    @Test
    void parseUserTypeIsCustomer() {
        String result = JsonParserService.parseUserType(customerJson);
        assertEquals(result, "Customer");
    }

    @Test
    void parseUserTypeIsVendor() {
        String result = JsonParserService.parseUserType(vendorJson);
        assertEquals(result, "Vendor");
    }

    @Test
    void parseUserTypeIsCourier() {
        String result = JsonParserService.parseUserType(courierJson);
        assertEquals(result, "Courier");
    }

    @Test
    void parseUserTypeIsAdmin() {
        String result = JsonParserService.parseUserType(adminJson);
        assertEquals(result, "Admin");
    }

    @Test
    void parseVendorCuisineEmptyList() {
        HashMap<UUID, String> result = JsonParserService.parseVendorCuisine(new ArrayList<>());
        assertNull(result);
    }

    @Test
    void parseVendorCuisinePartial() {
        String v1 = """
              {
                "userID": "550e8400-e29b-41d4-a716-446655440000",
                "user": {
                  "id": "550e8400-e29b-41d4-a716-446655440000",
                  "firstname": "John",
                  "surname": "James",
                  "email": "john@email.com",
                  "avatar": "www.avatar.com/avatar.png",
                  "password": "12345",
                  "verified": false,
                  "userType": "Customer"
                },
                "cuisineType": "italian"
            }""";
        String v2 = """
              {
                "userID": "110e8400-e29b-41d4-a716-446655440000",
                "user": {
                  "id": "110e8400-e29b-41d4-a716-446655440000",
                  "firstname": "John",
                  "surname": "James",
                  "email": "john@email.com",
                  "avatar": "www.avatar.com/avatar.png",
                  "password": "12345",
                  "verified": false,
                  "userType": "Customer"
                },
                "cuisineType":
            }""";
        String v3 = """
              {
                "userID": "220e8400-e29b-41d4-a716-446655440000",
                "user": {
                  "id": "220e8400-e29b-41d4-a716-446655440000",
                  "firstname": "John",
                  "surname": "James",
                  "email": "john@email.com",
                  "avatar": "www.avatar.com/avatar.png",
                  "password": "12345",
                  "verified": false,
                  "userType": "Customer"
                },
                "cuisineType": "Asian"
            }""";

        HashMap<UUID, String> result = JsonParserService.parseVendorCuisine(List.of(v1, v2, v3));
        HashMap<UUID, String> expected = new HashMap<>();
        expected.put(uuid, "italian");
        expected.put(UUID.fromString("220e8400-e29b-41d4-a716-446655440000"), "Asian");
        assertThat(result).isEqualTo(expected);
    }
}
