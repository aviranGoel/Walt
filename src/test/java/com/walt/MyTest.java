package com.walt;

import com.walt.dao.*;
import com.walt.model.*;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

@SpringBootTest()
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MyTest
{

    @TestConfiguration
    static class WaltServiceImplTestContextConfiguration {

        @Bean
        public WaltService waltService() {
            return new WaltServiceImpl();
        }
    }

    @Autowired
    WaltService waltService;

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

    @BeforeEach()
    public void prepareData()
    {
        City jerusalem = new City("Jerusalem");
        City tlv = new City("Tel-Aviv");
        City bash = new City("Beer-Sheva");
        City haifa = new City("Haifa");

        cityRepository.saveAll(Lists.newArrayList(jerusalem, tlv, bash, haifa));

        Driver mary = new Driver("Mary", tlv);
        Driver patricia = new Driver("Patricia", tlv);
        Driver jennifer = new Driver("Jennifer", haifa);
        Driver james = new Driver("James", bash);
        Driver john = new Driver("John", bash);
        Driver robert = new Driver("Robert", jerusalem);
        Driver david = new Driver("David", jerusalem);
        Driver daniel = new Driver("Daniel", tlv);
        Driver noa = new Driver("Noa", haifa);
        Driver ofri = new Driver("Ofri", haifa);
        Driver nata = new Driver("Neta", jerusalem);

        driverRepository.saveAll(Lists.newArrayList(mary, patricia, jennifer, james, john, robert, david, daniel, noa, ofri, nata));

        Customer beethoven = new Customer("Beethoven", tlv, "Ludwig van Beethoven");
        Customer mozart = new Customer("Mozart", jerusalem, "Wolfgang Amadeus Mozart");
        Customer chopin = new Customer("Chopin", haifa, "Frédéric François Chopin");
        Customer rachmaninoff = new Customer("Rachmaninoff", tlv, "Sergei Rachmaninoff");
        Customer bach = new Customer("Bach", tlv, "Sebastian Bach. Johann");

        customerRepository.saveAll(Lists.newArrayList(beethoven, mozart, chopin, rachmaninoff, bach));

        Restaurant meat = new Restaurant("meat", jerusalem, "All meat restaurant");
        Restaurant vegan = new Restaurant("vegan", tlv, "Only vegan");
        Restaurant cafe = new Restaurant("cafe", tlv, "Coffee shop");
        Restaurant chinese = new Restaurant("chinese", tlv, "chinese restaurant");
        Restaurant mexican = new Restaurant("mexican", tlv, "mexican restaurant ");

        restaurantRepository.saveAll(Lists.newArrayList(meat, vegan, cafe, chinese, mexican));

        String deliveryTime_name1 = "01-10-2021 15:00";
        String deliveryTime_name2 = "01-10-2021 16:00";
        String deliveryTime_name3 = "01-10-2021 17:00";
        String deliveryTime_name4 = "01-10-2021 15:00";
        String deliveryTime_name5 = "01-10-2021 16:00";
        String deliveryTime_name6 = "01-10-2021 15:00";

        Date deliveryTime1 = null;
        Date deliveryTime2 = null;
        Date deliveryTime3 = null;
        Date deliveryTime4 = null;
        Date deliveryTime5 = null;
        Date deliveryTime6 = null;
        try
        {
            deliveryTime1 = new SimpleDateFormat("dd-MM-yyyy HH:mm").parse(deliveryTime_name1);
            deliveryTime2 = new SimpleDateFormat("dd-MM-yyyy HH:mm").parse(deliveryTime_name2);
            deliveryTime3 = new SimpleDateFormat("dd-MM-yyyy HH:mm").parse(deliveryTime_name3);
            deliveryTime4 = new SimpleDateFormat("dd-MM-yyyy HH:mm").parse(deliveryTime_name4);
            deliveryTime5 = new SimpleDateFormat("dd-MM-yyyy HH:mm").parse(deliveryTime_name5);
            deliveryTime6 = new SimpleDateFormat("dd-MM-yyyy HH:mm").parse(deliveryTime_name6);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }

        Delivery delivery1 = new Delivery(mary, vegan, beethoven, deliveryTime1);
        Delivery delivery2 = new Delivery(mary, cafe, rachmaninoff, deliveryTime2);
        Delivery delivery3 = new Delivery(mary, chinese, bach, deliveryTime3);
        Delivery delivery4 = new Delivery(patricia, vegan, beethoven, deliveryTime4);
        Delivery delivery5 = new Delivery(patricia, cafe, rachmaninoff, deliveryTime5);
        Delivery delivery6 = new Delivery(daniel, mexican, bach, deliveryTime6);

        deliveryRepository.saveAll(Lists.newArrayList(delivery1, delivery2, delivery3, delivery4, delivery5, delivery6));

        updateDriverDistanceByDelivery(delivery1);
        updateDriverDistanceByDelivery(delivery2);
        updateDriverDistanceByDelivery(delivery3);
        updateDriverDistanceByDelivery(delivery4);
        updateDriverDistanceByDelivery(delivery5);
        updateDriverDistanceByDelivery(delivery6);
    }

    private void updateDriverDistanceByDelivery(Delivery delivery)
    {
        Driver driver = driverRepository.findByName(delivery.getDriver().getName());
        double distance = delivery.getDistance();

        driver.updateDistance(distance);

        driverRepository.save(driver);
    }

    @Test
    public void testBasics(){
        assertEquals((driverRepository.findAllDriversByCity(cityRepository.findByName("Beer-Sheva")).size()), 2);
    }

    @Test
    public void testBasics_repositorySize()
    {
        assertEquals(((List<City>) cityRepository.findAll()).size(),4);
        assertEquals(((List<Driver>) driverRepository.findAll()).size(),11);
        assertEquals(((List<Customer>) customerRepository.findAll()).size(),5);
        assertEquals(((List<Restaurant>) restaurantRepository.findAll()).size(),5);
        assertEquals(((List<Delivery>) deliveryRepository.findAll()).size(),6);
    }

    @Test
    public void createOrderAndAssignDriver_customerCityNotSameAsRestaurantCity()
    {
        Customer customer = customerRepository.findByName("Beethoven");
        Restaurant restaurant = restaurantRepository.findByName("meat");
        String deliveryTime_name = "01-10-2021 10:00";

        Date deliveryTime = null;

        try
        {
            deliveryTime = new SimpleDateFormat("dd-MM-yyyy HH:mm").parse(deliveryTime_name);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }

        try
        {
            assertEquals("Customer City not same as Restaurant City!", waltService.createOrderAndAssignDriver(customer, restaurant, deliveryTime));
            Assert.fail( "Should have thrown an exception" );
        }
        catch (Exception e)
        {
            String expectedMessage = "Customer City not same as Restaurant City!";
            assertEquals( "Exception message must be correct", expectedMessage, e.getMessage() );
        }
    }

    @Test
    public void createOrderAndAssignDriver_noDriverAvailable_null()
    {
        Customer customer = customerRepository.findByName("Beethoven");
        Restaurant restaurant = restaurantRepository.findByName("chinese");
        String deliveryTime_name = "01-10-2021 15:00";

        Date deliveryTime = null;

        try
        {
            deliveryTime = new SimpleDateFormat("dd-MM-yyyy HH:mm").parse(deliveryTime_name);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }

        try
        {
            Delivery delivery = waltService.createOrderAndAssignDriver(customer, restaurant, deliveryTime);
            assertEquals(null, delivery);
        }
        catch (Exception e)
        {
            String expectedMessage = "Customer City not same as Restaurant City!";
            assertEquals( "Exception message must be correct", expectedMessage, e.getMessage() );
        }
    }

    @Test
    public void createOrderAndAssignDriver_oneDriverAvailable()
    {
        Customer customer = customerRepository.findByName("Beethoven");
        Restaurant restaurant = restaurantRepository.findByName("chinese");
        String deliveryTime_name = "01-10-2021 16:00";

        Date deliveryTime = null;

        try
        {
            deliveryTime = new SimpleDateFormat("dd-MM-yyyy HH:mm").parse(deliveryTime_name);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }

        try
        {
            Delivery delivery = waltService.createOrderAndAssignDriver(customer, restaurant, deliveryTime);
            assertEquals("Daniel", delivery.getDriver().getName());
        }
        catch (Exception e)
        {
            String expectedMessage = "Customer City not same as Restaurant City!";
            assertEquals( "Exception message must be correct", expectedMessage, e.getMessage() );
        }
    }

    @Test
    public void createOrderAndAssignDriver_allDriverAvailable()
    {
        Customer customer = customerRepository.findByName("Beethoven");
        Restaurant restaurant = restaurantRepository.findByName("chinese");
        String deliveryTime_name = "01-10-2021 18:00";

        Date deliveryTime = null;

        try
        {
            deliveryTime = new SimpleDateFormat("dd-MM-yyyy HH:mm").parse(deliveryTime_name);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }

        try
        {
            Delivery delivery = waltService.createOrderAndAssignDriver(customer, restaurant, deliveryTime);
            assertEquals("Daniel", delivery.getDriver().getName());
        }
        catch (Exception e)
        {
            String expectedMessage = "Customer City not same as Restaurant City!";
            assertEquals( "Exception message must be correct", expectedMessage, e.getMessage() );
        }
    }

    @Test
    public void createOrderAndAssignDriver_driverDistanceUpdated()
    {
        Customer customer = customerRepository.findByName("Beethoven");
        Restaurant restaurant = restaurantRepository.findByName("chinese");
        String deliveryTime_name = "01-10-2021 16:00";

        Date deliveryTime = null;

        try
        {
            deliveryTime = new SimpleDateFormat("dd-MM-yyyy HH:mm").parse(deliveryTime_name);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }

        try
        {
            double driverOriginDistance = driverRepository.findByName("Daniel").getDistance();

            Delivery delivery = waltService.createOrderAndAssignDriver(customer, restaurant, deliveryTime);
            double deliveryDistance = delivery.getDistance();

            double driverNewDistance = driverOriginDistance + deliveryDistance;

            assertEquals(new Double(driverNewDistance), new Double(driverRepository.findByName("Daniel").getDistance()));
        }
        catch (Exception e)
        {
            String expectedMessage = "Customer City not same as Restaurant City!";
            assertEquals( "Exception message must be correct", expectedMessage, e.getMessage() );
        }
    }

    @Test
    public void createOrderAndAssignDriver_deliverySaved()
    {
        Customer customer = customerRepository.findByName("Beethoven");
        Restaurant restaurant = restaurantRepository.findByName("chinese");
        String deliveryTime_name = "01-10-2021 16:00";

        Date deliveryTime = null;

        try
        {
            deliveryTime = new SimpleDateFormat("dd-MM-yyyy HH:mm").parse(deliveryTime_name);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }

        try
        {
            Delivery delivery = waltService.createOrderAndAssignDriver(customer, restaurant, deliveryTime);
            assertEquals(((List<Delivery>) deliveryRepository.findAll()).size(),7);
        }
        catch (Exception e)
        {
            String expectedMessage = "Customer City not same as Restaurant City!";
            assertEquals( "Exception message must be correct", expectedMessage, e.getMessage() );
        }
    }

}
