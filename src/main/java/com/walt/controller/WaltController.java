package com.walt.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.walt.WaltServiceImpl;
import com.walt.dao.*;
import com.walt.model.*;
import com.walt.model.modelRequset.CreateOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/")
public class WaltController
{
    @Autowired
    WaltServiceImpl waltService;

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
     * A Post Request:
     * The end point: '/createOrder'.
     * The Request Body Parameters: CreateOrder.
     * Create a new Delivery by the given order details and assign a Driver to its.
     * @param createOrder Given Order details: customer, restaurant and deliveryTime (CreateOrder type).
     * @return A message if a new Delivery been created with an assigned Driver, or an error.
     */
    @RequestMapping(value = "/createOrder",consumes = "application/json", method = POST)
    @ResponseBody
    public ResponseEntity<?> createOrderAndAssignDriver(@RequestBody CreateOrder createOrder)
    {
        // Request Body Parameters.
        String customerName = createOrder.getCustomer();
        String restaurantName = createOrder.getRestaurant();
        String deliveryTimeName = createOrder.getDeliveryTime();

        // Find the Customer by the given CustomerName.
        Customer customer = customerRepository.findByName(customerName);
        if (customer == null)
        {
            return ResponseEntity
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body("Error - Customer does not exists!");
        }

        // Find the Restaurant by the given RestaurantName.
        Restaurant restaurant = restaurantRepository.findByName(restaurantName);

        // The below not necessary: Assume the Restaurant exists in the system.
//        if (restaurant == null)
//        {
//            return ResponseEntity
//                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
//                    .body("Error - Restaurant does not exists!");
//        }

        Date deliveryTime;
        try
        {
            // Format Delivery Date and Time: 'dd-MM-yyyy HH:mm'.
            deliveryTime = new SimpleDateFormat("dd-MM-yyyy HH:mm").parse(deliveryTimeName);
        }
        catch (ParseException e)
        {
            return ResponseEntity
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body("Error - Delivery DateTime format need to be: 'dd-MM-yyyy HH:mm'!");
        }

        Delivery delivery;
        try
        {
            // Create a new Delivery by the given order and assign a Driver to its.
            delivery = waltService.createOrderAndAssignDriver(customer, restaurant, deliveryTime);
        }
        catch (Exception e)
        {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error - Server failed to create new order!");
        }

        Object responseMessage = "New order created and a driver assigned!";
        return (ResponseEntity<?>) responseMessage;
    }

    /**
     * A Get Request:
     * The end point: '/driverRankReport'.
     * Create a rank report of all Drivers and the total distance of the Delivery they made.
     * The rank report, order by total distance in descending order.
     * @return List that represent the rank report.
     */
    @RequestMapping(value = "/driverRankReport", method = GET)
    @ResponseBody
    public ResponseEntity<?> getDriverRankReport()
    {
        // Create a rank report of all Drivers and the total distance of the Delivery they made.
        List<DriverDistance> driverDistances = waltService.getDriverRankReport();

        return (ResponseEntity<?>) driverDistances;
    }

    /**
     * A Get Request:
     * The end point: '/driverRankReportByCity'.
     * The Request Body Parameters: city.
     * Create a rank report of all Drivers in the given City and the total distance of the Delivery they made.
     * The rank report, order by total distance in descending order.
     * @param city Given City of the Drivers (String type).
     * @return List that represent the rank report from the given City.
     */
    @RequestMapping(value = "/driverRankReportByCity", method = GET)
    @ResponseBody
    public ResponseEntity<?> getDriverRankReportByCity(@RequestParam("city") String city)
    {
        // Find the City by the given CityName.
        City givenCity = cityRepository.findByName(city);
        if (givenCity == null)
        {
            return ResponseEntity
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body("Error - City does not exists in the system!");
        }

        // Create a rank report of all Drivers in the given City and the total distance of the Delivery they made.
        List<DriverDistance> driverDistances = waltService.getDriverRankReportByCity(givenCity);

        return (ResponseEntity<?>) driverDistances;
    }
}
