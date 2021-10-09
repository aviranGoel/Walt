package com.walt.model.modelRequset;

public class CreateOrder
{
    String customer;
    String restaurant;
    String deliveryTime;

    public CreateOrder(String customer, String restaurant, String deliveryTime)
    {
        this.customer = customer;
        this.restaurant = restaurant;
        this.deliveryTime = deliveryTime;
    }

    public String getCustomer()
    {
        return customer;
    }

    public void setCustomer(String customer)
    {
        this.customer = customer;
    }

    public String getRestaurant()
    {
        return restaurant;
    }

    public void setRestaurant(String restaurant)
    {
        this.restaurant = restaurant;
    }

    public String getDeliveryTime()
    {
        return deliveryTime;
    }

    public void setDeliveryTime(String deliveryTime)
    {
        this.deliveryTime = deliveryTime;
    }
}
