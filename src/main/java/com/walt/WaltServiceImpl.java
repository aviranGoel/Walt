package com.walt;

import com.walt.dao.*;
import com.walt.model.*;
import javafx.util.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.*;

@Service
public class WaltServiceImpl implements WaltService {

    @Resource
    CityRepository cityRepository;

    @Resource
    CustomerRepository customerRepository;

    @Resource
    DriverRepository driverRepository;

    @Resource
    DeliveryRepository deliveryRepository;

    @Resource
    RestaurantRepository restaurantRepository;

    /**
     * Create a new order, by the given Customer, Restaurant and Date of deliveryTime,
     * ans assign an available Driver to the order Delivery.
     * Note: The Customer City and the Restaurant City must by the same.
     * @param customer Customer who order a Delivery (Customer type).
     * @param restaurant Restaurant where the order from (Restaurant type).
     * @param deliveryTime Date And Time of the Delivery (Date type).
     * @return A new Delivery which created (Delivery type).
     * @throws Exception if the Customer City and the Restaurant City are not the same.
     */
    @Override
    public Delivery createOrderAndAssignDriver(Customer customer, Restaurant restaurant, Date deliveryTime) throws Exception
    {
        City customer_city = customer.getCity();
        String customer_city_name = customer_city.getName();
        // Check if the Customer City and the Restaurant City are the same.
        // Throw Exception if not.
        if ( ! (customer_city_name.equals(restaurant.getCity().getName())))
        {
            throw new Exception("Customer City not same as Restaurant City!");
        }

        // Find which Driver is available to take the new Delivery.
        // Note: If more then one Driver is available, we choose the least busy Driver according to the Driver history.
        Driver chosenDriver = findTheChosenDriver(customer_city, deliveryTime);

        // Case when no driver is available to take the delivery.
        if (chosenDriver == null)
        {
            throw new Exception("Sorry no driver available to take the new Delivery!");
        }

        // Create new delivery.
        Delivery delivery = new Delivery(chosenDriver, restaurant, customer, deliveryTime);

        // Update the delivery distance for the chosen driver.
        double distance = delivery.getDistance();
        chosenDriver.updateDistance(distance);
        driverRepository.save(chosenDriver);

        // Save the new delivery.
        deliveryRepository.save(delivery);

        return delivery;
    }

    /**
     * Find which Driver is available to take the new Delivery.
     * Note: If more then one Driver is available, we choose the least busy Driver according to the Driver history.
     * @param deliveryCity City of the Delivery (City type).
     * @param deliveryTime Date And Time of the Delivery (Date type).
     * @return Available Driver to take the delivery (if exists) or null (Driver type).
     */
    private Driver findTheChosenDriver(City deliveryCity, Date deliveryTime)
    {
        // Convert deliveryTime from Date to Timestamp, to check equality.
        Timestamp ts_deliveryTime = new Timestamp(deliveryTime.getTime());

        // Create Map of Driver and the total of its Deliveries.
        // Note: The Map will contain only the Drivers that available to take the new Delivery.
        Map<Driver, Integer> map_driver_totalDeliveries = createMap_driver_totalDeliveries(deliveryCity, ts_deliveryTime);

        // Case when the Map is empty, meaning no Driver available to take the new Delivery.
        if (map_driver_totalDeliveries.isEmpty())
        {
            return null;
        }

        // Take the first Entry from the Map.
        Map.Entry<Driver, Integer> firstEntry = map_driver_totalDeliveries.entrySet().iterator().next();

        // Update it is a temporary Chosen Driver and as Minimum Total Deliveries.
        Driver chosenDriver = firstEntry.getKey();
        Integer minimumDeliveries = firstEntry.getValue();

        // Check which of the Driver it is the least busy Driver according to the Driver history.
        // And he will be the Chosen Driver to take the Delivery.
        for (Map.Entry<Driver, Integer> entry : map_driver_totalDeliveries.entrySet())
        {
            if (entry.getValue() < minimumDeliveries)
            {
                chosenDriver = entry.getKey();
                minimumDeliveries = entry.getValue();
            }
        }
        return chosenDriver;
    }

    /**
     * Create Map of Driver and the total of its Deliveries.
     * Note: The Map will contain only the Drivers that available to take the new Delivery.
     * @param deliveryCity City of the Delivery (City type).
     * @param ts_deliveryTime Timestamp of the Delivery (Timestamp type).
     * @return Map of Driver and the total of its Deliveries (Map type).
     */
    private Map<Driver, Integer> createMap_driver_totalDeliveries(City deliveryCity, Timestamp ts_deliveryTime)
    {
        // Find all Drivers of the Delivery City.
        List<Driver> driversFromCity = driverRepository.findAllDriversByCity(deliveryCity);

        // Create Map of Driver and the total of its Deliveries.
        // Note: The Map will contain only the Drivers that available to take the new Delivery.
        Map<Driver, Integer> map_driver_totalDeliveries = new LinkedHashMap<>();

        Integer totalDeliveriesOfDriver;
        for (Driver driver : driversFromCity)
        {
            // Find total Deliveries of current Driver.
            // If the current Driver not available to take the new Delivery, its total Deliveries will be -1.
            totalDeliveriesOfDriver = findTotalDeliveriesOfDriver(driver, ts_deliveryTime);

            // Case when the current Driver is available to take the new Delivery.
            if (totalDeliveriesOfDriver != -1)
            {
                // Add the Driver and its total Deliveries to the Map.
                map_driver_totalDeliveries.put(driver, totalDeliveriesOfDriver);
            }
        }
        return map_driver_totalDeliveries;
    }

    /**
     * Find total Deliveries of given Driver.
     * If the given Driver not available to take the new Delivery, its total Deliveries will be -1.
     * @param driver given Driver (Driver type).
     * @param ts_deliveryTime Timestamp of the Delivery (Timestamp type).
     * @return Total Deliveries of given Driver (Integer type).
     */
    private Integer findTotalDeliveriesOfDriver(Driver driver, Timestamp ts_deliveryTime)
    {
        // Find all Deliveries of given Driver.
        List<Delivery> deliveryOfDriver = deliveryRepository.findByDriver(driver);
        Date delivery_deliveryTime;
        boolean driverFreeToDeliver = true;
        for (Delivery delivery : deliveryOfDriver)
        {
            delivery_deliveryTime = delivery.getDeliveryTime();

            // Case when the given Driver not available to take the new Delivery.
            // (He as other Delivery on the save time).
            if (delivery_deliveryTime.equals(ts_deliveryTime))
            {
                // Update a flag (Driver not available to take the new Delivery).
                driverFreeToDeliver = false;
                break;
            }
        }

        // Case when the flag is false (Driver not available to take the new Delivery).
        if ( ! (driverFreeToDeliver))
        {
            return -1;
        }

        // Driver total Deliveries.
        return deliveryOfDriver.size();
    }

    /**
     * Create a rank report of all Drivers and the total distance of the Delivery they made.
     * @return List that represent the rank report, order by total distance in descending order (List<DriverDistance> type).
     */
    @Override
    public List<DriverDistance> getDriverRankReport()
    {
        // Find all Drivers.
        List<Driver> driverList = (List<Driver>) driverRepository.findAll();

        // Create and return the ordered rank report List
        return getDriverDistanceListDescendingOrder(driverList);
    }

    /**
     * Create a List of all Drivers and the total distance of the Delivery they made,
     * using the given List of Drivers.
     * @param driverList Given List of Drivers (List<Driver> type).
     * @return List of DriverDistance order by total distance in descending order (List<DriverDistance> type).
     */
    private List<DriverDistance> getDriverDistanceListDescendingOrder(List<Driver> driverList)
    {
        // Create a descending order List of Pairs of Driver name and its total Distance.
        List<Pair<String, Double>> list_pair_driverNameDistance = getDescendingOrderList_driverNameDistance(driverList);

        List<DriverDistance> list_driverDistance = new ArrayList<>();

        Driver driver;
        Long distance;
        DriverDistance driverDistance;

        // Create the List of DriverDistance.
        // It will be in descending order, because we follow list_pair_driverNameDistance (a descending order List).
        for (Pair<String, Double> pair_driverNameDistance : list_pair_driverNameDistance)
        {
            driver = driverRepository.findByName(pair_driverNameDistance.getKey());
            distance = pair_driverNameDistance.getValue().longValue();

            // Create new DriverDistance.
            driverDistance = new DriverDistanceClass(driver, distance);

            // Add a DriverDistance to the List
            list_driverDistance.add(driverDistance);
        }

        return list_driverDistance;
    }

    /**
     * Create List of Pairs.
     * Each Pair represent Driver name and its total Distance.
     * The List order by total distance in descending order
     * @param driverList Given List of Drivers (List<Driver> type).
     * @return List of Pairs of Driver name and its total Distance, order by total distance in descending order (List<Pair<String, Double>> type).
     */
    private List<Pair<String, Double>> getDescendingOrderList_driverNameDistance(List<Driver> driverList)
    {
        List<Pair<String, Double>> list_pair_driverNameDistance = new ArrayList<>();

        // Create the List of Pairs of Driver name and its total Distance.
        for (Driver driver : driverList)
        {
            // Add new Pair of Driver name and its total Distance, to the List.
            list_pair_driverNameDistance.add(new Pair<>(driver.getName(), driver.getDistance()));
        }

        // Sort the List in descending order.
        Collections.sort(list_pair_driverNameDistance, new Comparator<Pair<String, Double>>()
        {
            /**
             * Create compare to the Comparator, in order to Sort the Collections.
             * Compare by total Distance of the Pair Object (Pair Value, a Double type).
             * Note: We want descending order, so we compare pair2 to pair1.
             * Note: If we want ascending order, we need to compare pair1 to pair2.
             * @param pair1 First pair to compare (Pair<String, Double> type).
             * @param pair2 Second pair to compare (Pair<String, Double> type).
             * @return A value which represent which pair is bigger (int type).
             */
            @Override
            public int compare(final Pair<String, Double> pair1, final Pair<String, Double> pair2)
            {
                return pair2.getValue().compareTo(pair1.getValue());
            }
        }
        );

        return list_pair_driverNameDistance;
    }

    /**
     * Create a rank report of all Drivers in the given City and the total distance of the Delivery they made.
     * @param city Given City of the Drivers (City type).
     * @return List that represent the rank report from the given City, order by total distance in descending order (List<DriverDistance> type).
     */
    @Override
    public List<DriverDistance> getDriverRankReportByCity(City city)
    {
        // Find all Drivers.
        List<Driver> driverList = (List<Driver>) driverRepository.findAllDriversByCity(city);

        // Create and return the ordered rank report List
        return getDriverDistanceListDescendingOrder(driverList);
    }
}
