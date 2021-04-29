package ija.ija2020.proj;

import ija.ija2020.proj.calendar.Calendar;
import ija.ija2020.proj.calendar.Event;
import ija.ija2020.proj.store.DropOffPoint;
import ija.ija2020.proj.store.Goods;
import ija.ija2020.proj.store.GoodsItem;
import ija.ija2020.proj.store.GoodsOrder;
import ija.ija2020.proj.store.map.StoreMap;
import ija.ija2020.proj.store.map.StoreNode;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.util.Pair;

import java.io.IOException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

/**
 * Controls the flow of the program. Runs the simulation
 *
 * Runs a discreet simulation of NextEvent type that simulates the warehouse.
 */
public class MainController implements Observer{
    private static MainController instance = null;

    private CartController cartController;
    private DataLoader dataLoader;

    private Calendar cal;
    private StoreMap map;
    private Map<String, Goods> goodsTypes;
    private PriorityQueue<GoodsOrder> waitingOrders;
    private List<GoodsOrder> activeOrders;
    private List<GoodsOrder> fulfilledOrders;

    private final int DEFAULT_CART_CAPACITY = 20;
    private static final int ORDER_ACCEPT_DELAY = 1;

    private LocalTime time = LocalTime.now();
    private LocalTime endTime = LocalTime.MAX;

    final long normalStepSize = 1; //init step size in seconds
    long tStep = normalStepSize; //step size in seconds
    private DropOffPoint defaultDropOffPoint;

    @Override
    public void update(Observable o, Object arg) {
        //observe orders to see when they become fulfilled
        if (o instanceof GoodsOrder){
            GoodsOrder order = (GoodsOrder) o;
            if(order.isFulfilled()){
                this.activeOrders.remove(order);
                this.fulfilledOrders.add(order);
            }
        }
    }

    private class GoodsOrderPriorityComparator implements Comparator<GoodsOrder> {
        @Override
        public int compare(GoodsOrder o1, GoodsOrder o2) {
            if(o1.getPriority() > o2.getPriority()){
                return -1;
            }else if(o1.getPriority() < o2.getPriority()){
                return 1;
            }
            return 0;
        }
    }

    public MainController() throws IOException {
        this.cal = new Calendar();
        this.goodsTypes = new HashMap<>();
        this.waitingOrders = new PriorityQueue<>(new GoodsOrderPriorityComparator());
        this.activeOrders = new LinkedList<>();
        this.fulfilledOrders = new LinkedList<>();

        this.dataLoader = new DataLoader();
        this.initStore("data/map.csv", "data/goods.csv", "data/shelves.csv", "data/defaultDropOffPoint.csv", "data/orders.csv", "data/stocks.csv");

        this.cartController = new CartController(DEFAULT_CART_CAPACITY, (StoreNode) this.map.getNode(0, 0));
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

    private void loadMap(String filename) throws IOException {
        this.map = dataLoader.loadMap(filename);
    }

    private void loadGoodsTypes(String filename) throws IOException {
        this.goodsTypes = dataLoader.loadGoodsTypes(filename).stream().collect(toMap(Goods::getName, Function.identity()));
    }

    private void loadShelves(String filename) throws IOException {
        this.map.addShelves(this.dataLoader.loadShelves(filename));
    }

    /**
     * Loads new orders from a file, and sets them as waiting to be processed
     * @param filename Name of the file with order definitions
     * @param append If true, the new orders are appended to the queue of waiting orders, otherwise the queue is overwritten
     * @throws IOException
     */
    private void loadOrders(String filename, DropOffPoint dropOffPoint, boolean append) throws IOException {
        if (append){
            List<GoodsOrder> orders = this.dataLoader.loadOrders(filename, this.goodsTypes);
            for (GoodsOrder order : orders){
                order.setDropOffPoint(dropOffPoint);
                this.waitingOrders.add(order);
            }
        }else{
            this.waitingOrders = new PriorityQueue<>(new GoodsOrderPriorityComparator());
            List<GoodsOrder> orders = this.dataLoader.loadOrders(filename, this.goodsTypes);
            for (GoodsOrder order : orders){
                order.setDropOffPoint(dropOffPoint);
                this.waitingOrders.add(order);
            }
        }
    }

    private void loadDefaultDropOffPoint(String filename) throws IOException {
        this.defaultDropOffPoint = this.dataLoader.loadDefaultDropOffPoint(filename);
    }

    private void loadDefaultStocks(String filename) throws IOException {
        List<Pair<String, GoodsItem>> stocks = this.dataLoader.loadDefaultStocks(filename, this.goodsTypes);
        for (Pair<String, GoodsItem> stock : stocks){
            this.map.getShelfByName(stock.getKey()).stockItem(stock.getValue());
        }
    }

    private void initStore(String mapFilename, String goodsFilename, String shelvesFilename, String defaultDropOffPointFilename, String initOrdersFilename, String defaultStockFilename) throws IOException {
        this.loadMap(mapFilename);
        this.loadGoodsTypes(goodsFilename);
        this.loadShelves(shelvesFilename);
        this.loadDefaultDropOffPoint(defaultDropOffPointFilename);
        this.loadOrders(initOrdersFilename, this.defaultDropOffPoint,true);
        this.loadDefaultStocks(defaultStockFilename);
    }

    /**
     * Close off area for carts (make all nodes inside it obstructed)
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    public void closeArea(int x1, int y1, int x2, int y2){
        for(int x=x1; x <= x2; x++){
            for(int y=y1; y < y2; y++){
                this.getMap().getNode(x, y).setObstructed(true);
            }
        }
    }

    /**
     * Calendar event action that tries to assign as many waiting orders to carts as possible
     * @param t time
     */
    public void processOrderAction(LocalTime t){
        GoodsOrder order;

        //try to process as many orders as possible at once
        while((order = this.waitingOrders.peek()) != null) {
            if (this.cartController.acceptOrder(order) != null) {
                System.out.println(String.format("T=%s | Accepting order %s", t.toString(), order.toString()));
                    this.activeOrders.add(this.waitingOrders.poll()); //mark order as active if it was accepted
            }else{
                //no carts available at this time
                break;
            }
        }
        //TODO:Try to plan this outside of this event (like when we load new orders from somewhere)
        //this.getCalendar().insertEvent(new Event(this.getTime().plusSeconds(MainController.ORDER_ACCEPT_DELAY), 0, this::processOrderAction));
    }

    private Thread simulation = new Thread(){
        public void run() {
            Event e;
            while (time.isBefore(endTime)) {
                e = cal.getNextEvent();
                while (e != null) {
                    if (e.getActivationTime().isBefore(endTime)) {
                        while(!time.plusSeconds(tStep).isAfter(e.getActivationTime()) && !time.plusSeconds(tStep).isAfter(endTime)) {
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
                                //System.out.println(String.format("T=%s | going to sleep for %d seconds", time.toString(), tStep));
                                this.sleep(tStep*1000);
                            } catch (InterruptedException interruptedException) {
                                interruptedException.printStackTrace();
                            }

                        }

                        //perform event
                        //System.out.println(String.format("T=%s | Performing action %s", time.toString(), e.getAction().toString()));
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

    private Service<Void> simService = new Service<Void>(){

        @Override
        protected Task createTask() {
            return new Task<Void>(){

                @Override
                protected Void call() throws Exception {
                    Event e;
                    while (time.isBefore(endTime)) {
                        e = cal.getNextEvent();
                        while (e != null) {
                            if (e.getActivationTime().isBefore(endTime)) {
                                while(!time.plusSeconds(tStep).isAfter(e.getActivationTime()) && !time.plusSeconds(tStep).isAfter(endTime)) {
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
                                        //System.out.println(String.format("T=%s | going to sleep for %d seconds", time.toString(), tStep));
                                        Thread.sleep(tStep*1000);
                                    } catch (InterruptedException interruptedException) {
                                        interruptedException.printStackTrace();
                                        break;
                                    }

                                }

                                //perform event
                                //System.out.println(String.format("T=%s | Performing action %s", time.toString(), e.getAction().toString()));
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
                                Thread.sleep(tStep*1000);
                            } catch (InterruptedException interruptedException) {
                                interruptedException.printStackTrace();
                                break;
                            }

                            tStep = normalStepSize;
                        }
                    }
                    return null;
                }
            };
        }
    };

    public void startSimulation(){
        //this.simulation.run();
        this.simService.start();
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
        if (MainController.instance == null){
            try {
                MainController.instance = new MainController();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return MainController.instance;
    }

    public static void main(String[] args) throws IOException {
        MainController mainController = MainController.getInstance();

        //load data
//        System.out.println("Loading Data");
//        mainController.loadMap("data/map.csv");
//        mainController.loadGoodsTypes("data/goods.csv");
//        mainController.loadShelves("data/shelves.csv");
//        mainController.loadDefaultDropOffPoint("data/defaultDropOffPoint.csv");
//        mainController.loadOrders("data/orders.csv", mainController.defaultDropOffPoint, true);


        //Start assigning orders
        mainController.getCalendar().insertEvent(new Event(mainController.getTime(), 1, mainController::processOrderAction));

        System.out.println("Starting simulation");
        mainController.startSimulation();
    }

}
