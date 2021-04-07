package ija.ija2020.proj;

import ija.ija2020.proj.calendar.Event;

import java.io.IOException;

/**
 * Demo class that demonstrates work done by xkocal00 so far
 *
 * Loads data from files in /data folder and initiates the store.
 * Spawns carts to fulfill orders.
 * Movement of the carts can be seen in console.
 */
public class Xkocal00Main {
    public static void main(String[] args) throws IOException {
        MainController mainController = MainController.getInstance();

        //Start assigning orders
        mainController.getCalendar().insertEvent(new Event(mainController.getTime(), 1, mainController::processOrderAction));

        System.out.println("Starting simulation");
        mainController.startSimulation();
    }
}
