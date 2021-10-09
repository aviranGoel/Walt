package com.walt.model;

import javax.persistence.*;

@Entity
public class Driver extends NamedEntity {

    @ManyToOne
    City city;

    double distance;

    public Driver(){}

    public Driver(String name, City city){
        super(name);
        this.city = city;
        this.distance = 0;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public double getDistance() { return distance; }

    public void updateDistance(double distance)
    {
        this.distance = this.distance + distance;
    }
}
