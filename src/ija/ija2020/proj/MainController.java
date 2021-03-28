package ija.ija2020.proj;

import ija.ija2020.proj.calendar.Calendar;
import ija.ija2020.proj.calendar.Event;
import ija.ija2020.proj.geometry.Targetable;
import ija.ija2020.proj.store.map.StoreMap;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Timer;
import java.util.TimerTask;

public class MainController {
    private static MainController instance = null;

    private CartController cartController;

    private Calendar cal;
    private StoreMap map;

    private LocalTime time = LocalTime.now();
    private LocalTime endTime = LocalTime.MAX;

    final long normalStepSize = 1; //init step size in seconds
    long tStep = normalStepSize; //step size in seconds


    public MainController() {
        this.cartController = new CartController();
        this.cal = new Calendar();
        this.map = new StoreMap(20, 20);
    }

    public Calendar getCalendar() {
        return cal;
    }

    public StoreMap getMap() {
        return map;
    }

    public LocalTime getTime() {
        return time;
    }

    private Thread simulation = new Thread(){
        public void run() {
            Event e;
            while (time.isBefore(endTime)) {
                e = cal.getNextEvent();
                while (e != null) {
                    if (e.getActivationTime().isBefore(endTime)) {
                        while (time.plusSeconds(tStep).isBefore(e.getActivationTime()) && time.plusSeconds(tStep).isBefore(endTime)) {
                            if (time.plusSeconds(tStep).isAfter(e.getActivationTime())) {
                                tStep = e.getActivationTime().until(time, ChronoUnit.SECONDS);
                            }
                            if (time.plusSeconds(tStep).isAfter(endTime)) {
                                tStep = endTime.until(time, ChronoUnit.SECONDS);
                            }
                            //integration or some shit
//                        System.out.println("T=" + t + " | Cart Position: (" + cart.getX() + ", " + cart.getY() + ")");
                            time = time.plusSeconds(tStep);

                            try {
                                this.sleep(tStep);
                            } catch (InterruptedException interruptedException) {
                                interruptedException.printStackTrace();
                            }

                        }

                        //perform event
                        e.performAction(time);

                        tStep = normalStepSize;
                        e = cal.getNextEvent();
                    }
                }

                while (time.isBefore(endTime)) {
                    if (time.plusSeconds(tStep).isAfter(endTime)) {
                        tStep = endTime.until(time, ChronoUnit.SECONDS);
                    }
                    //integration or some shit
//                System.out.println("T=" + t + " | Cart Position: (" + cart.getX() + ", " + cart.getY() + ")");
                    time = time.plusSeconds(tStep);

                    try {
                        this.sleep(tStep);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }

                    tStep = normalStepSize;
                }
            }
        }
    };

    public void startSimulation(){
        this.simulation.run();
    }

//    public void startTime(){
//        this.timer = new Timer(false);
//        this.timer.scheduleAtFixedRate(new TimerTask(){
//
//            @Override
//            public void run() {
//                time = time.plusSeconds(1);
//
//
//            }
//        }, 0, 1000);
//    }

    public static MainController getInstance(){
        if (instance == null){
            instance = new MainController();
        }
        return instance;
    }

    public static void main(String[] args){

    }

}
