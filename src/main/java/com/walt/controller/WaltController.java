package com.walt.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.walt.WaltServiceImpl;
import com.walt.dao.*;
import com.walt.model.Customer;
import com.walt.model.Delivery;
import com.walt.model.Restaurant;
import com.walt.model.modelRequset.CreateOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.xml.crypto.Data;

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

    @RequestMapping(value = "/createOrder",consumes = "application/json", method = POST)
    @ResponseBody
    public ResponseEntity<?> createOrderAndAssignDriver(@RequestBody CreateOrder createOrder)
    {
        System.out.println("WaltController.createOrderAndAssignDriver");

        String customer_name = createOrder.getCustomer();
        String restaurant_name = createOrder.getRestaurant();
        String deliveryTime_name = createOrder.getDeliveryTime();

        System.out.println(customer_name);
        System.out.println(restaurant_name);
        System.out.println(deliveryTime_name);

        Customer customer = customerRepository.findByName(customer_name);
        Restaurant restaurant = restaurantRepository.findByName(restaurant_name);

        Date deliveryTime = null;
        try
        {
            deliveryTime = new SimpleDateFormat("dd-MM-yyyy HH:mm").parse(deliveryTime_name);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        System.out.println(deliveryTime);

        try
        {
//            Delivery delivery = waltService.createOrderAndAssignDriver(customer, restaurant, deliveryTime);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }
}
