package com.walt.model;

public class DriverDistanceClass implements DriverDistance
{
    private Driver driver;
    private Long totalDistance;

    public DriverDistanceClass(Driver driver, Long totalDistance)
    {
        this.driver = driver;
        this.totalDistance = totalDistance;
    }

    @Override
    public Driver getDriver()
    {
        return this.driver;
    }

    @Override
    public Long getTotalDistance()
    {
        return this.getTotalDistance();
    }


}
