package com.time.tdd.di.container;

import jakarta.inject.Named;
import junit.framework.Test;
import org.atinject.tck.Tck;
import org.atinject.tck.auto.Car;
import org.atinject.tck.auto.Convertible;
import org.atinject.tck.auto.Drivers;
import org.atinject.tck.auto.DriversSeat;
import org.atinject.tck.auto.Engine;
import org.atinject.tck.auto.FuelTank;
import org.atinject.tck.auto.Seat;
import org.atinject.tck.auto.Tire;
import org.atinject.tck.auto.V8Engine;
import org.atinject.tck.auto.accessories.Cupholder;
import org.atinject.tck.auto.accessories.SpareTire;

/**
 * @author XuJian
 * @date 2023-04-13 23:11
 **/
public class JakartaTCK {

    public static Test suite() {
        ContextConfig config = new ContextConfig();
        config.from(new Config() {
            @Export(Car.class)
            Convertible car;

            @Drivers
            @Export(Seat.class)
            DriversSeat drivers;

            Seat seat;

            Tire tire;

            @Export(Engine.class)
            V8Engine engine;

            @Named("spare")
            @Export(Tire.class)
            SpareTire spare;

            FuelTank fuelTank;

            @Static
            SpareTire spareTire;
            Cupholder cupholder;

            @Static
            Convertible convertible;


        });

        Car car = config.getContext().get(ComponentRef.of(Car.class)).get();
        return Tck.testsFor(car, true, true);
    }


}

