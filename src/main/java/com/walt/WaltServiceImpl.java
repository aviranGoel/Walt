package com.walt;

import com.walt.dao.*;
import com.walt.model.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.beans.IntrospectionException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
            return null;
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

    @Override
    public List<DriverDistance> getDriverRankReport()
    {
        System.out.println("WaltServiceImpl.getDriverRankReport");
        return null;
    }

    @Override
    public List<DriverDistance> getDriverRankReportByCity(City city)
    {
        System.out.println("WaltServiceImpl.getDriverRankReportByCity");
        return null;
    }
}
