package ija.ija2020.proj.vehicle;

import ija.ija2020.proj.CartController;
import ija.ija2020.proj.MainController;
import ija.ija2020.proj.calendar.Event;
import ija.ija2020.proj.geometry.Movable;
import ija.ija2020.proj.geometry.Targetable;
import ija.ija2020.proj.map.GridNode;
import ija.ija2020.proj.store.*;
import ija.ija2020.proj.store.map.StoreMap;
import ija.ija2020.proj.store.map.StoreNode;
import javafx.util.Pair;

import java.time.LocalTime;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

/**
 * Vehicle that can move through a store to fulfill orders
 * @see CartController
 * @see GoodsOrder
 */
public class Cart extends Observable implements Movable, Stockable {
    private static final long START_UP_DELAY = 1;
    private final MainController mainController = MainController.getInstance();
    private final CartController parentController;

    private final StoreMap map;
    private int capacity; //capacity in units
    private int speed; //speed in units per second

    private GoodsOrder order;
    private LinkedList<GoodsItem> items;

    private Pair<OrderItem, GoodsShelf> closestPair = null;
    private Deque<GridNode> curPath = null;

    private StoreNode pos;
    private boolean isDroppingOff = false;
    private boolean isFree = true;

    /**
     * Creates a new cart with a capacity and speed
     * Should be called from a CartController, otherwise the behaviour is undefined.
     * @see CartController
     * @param parentController The controller that handles this cart
     * @param capacity Number of items this cart can hold at once
     * @param speed Speed at which the cart moves in units per second
     * @param map Map through this cart will move
     * @param pos Node on which this cart will spawn
     */
    public Cart(CartController parentController, int capacity, int speed, StoreMap map, StoreNode pos) {
        this.parentController = parentController;
        this.addObserver(parentController);
        this.capacity = capacity;
        this.speed = speed;
        this.map = map;
        this.pos = pos;

        this.items = new LinkedList<>();
    }

    /**
     * Accepts the given order if it's not currently fulfilling another order
     * @param order the order to accept
     * @return True if the cart accepts the order, false if it's currently fulfilling another one
     */
    public boolean acceptOrder(GoodsOrder order){
        if (this.isFree) {
            this.isFree = false;
            this.order = order;
            order.setFulfillingVehicle(this);

            //schedule first event
            this.mainController.getCalendar().insertEvent(new Event(this.mainController.getTime().plusSeconds(this.START_UP_DELAY), 0, this::update));

            return true;
        }else{
            return false;
        }
    }

    /**
     * Is this car currently fulfilling an order
     * @return True if this cart is NOT currently fulfilling an order, False otherwise
     */
    public boolean isFree() {
        return isFree;
    }

    public boolean isFull(){
        return this.items.size() >= this.capacity;
    }

    public int getRemainingCapacity(){
        return this.capacity - this.items.size();
    }

    public float getSpeed() {
        return speed;
    }

    @Override
    public void moveTo(Targetable target) {
        pos = (StoreNode) this.map.getNode(target.getX(), target.getY());
    }

    @Override
    public int getX() {
        return pos.getX();
    }

    @Override
    public int getY() {
        return pos.getY();
    }

    public GridNode getCurNode(){
        return pos;
    }

    @Override
    public int distance(Targetable target) {
        //Manhattan Distance
        return Math.abs(target.getX() - pos.getX()) + Math.abs(target.getY() - pos.getY());
    }

    private void plotPathToShelf(GoodsShelf shelf){
        //get the closest unobstructed node adjacent to the shelf
        GridNode nextNode = this.map.getNode(
                shelf.getArea().getClosestCorner(this.getCurNode())
        ).getClosestUnobstructedAdjacent(this.getCurNode());

        this.curPath = this.getCurNode().getPathToNodeOnGrid(this.map, nextNode);
    }

    /**
     * Plot path to the next (closest) item in our order
     */
    private void plotNewPath(){
        this.closestPair = this.order.getClosest(this.map);
        plotPathToShelf(this.closestPair.getValue());
    }

    private void plotPathToDropOffPoint(){
        this.isDroppingOff = true;
        this.closestPair = null;
        this.curPath = this.getCurNode().getPathToNodeOnGrid(this.map, this.map.getNode(this.order.getDropOffPoint().getX(), this.order.getDropOffPoint().getY()));
    }

    /**
     * Calendar event for moving the cart
     * @param time time now
     */
    public void update(LocalTime time) {
        System.out.println("T=" + time.toString() + " | Cart " + this.toString() + ": (" + this.getX() + ", " + this.getY() + ")");

        if(!this.isFree) {
            if (this.curPath != null) { //if we already have a path
                GridNode nextNode = this.curPath.pollLast();
                if (nextNode != null) { //opposite should only happen if the order was empty or if the shelf is adjecent ot us
                    this.moveTo(nextNode); //move to the next node on the path
                }
            } else {
                this.plotNewPath();
                System.out.println(String.format("T=%s | Cart %s:(%d, %d) Ploting path to shelf %s",
                        time.toString(), this.toString(), this.getX(), this.getY(),
                        this.closestPair.getValue().getName()
                ));
            }

            //check whether we made it to the end
            if (this.curPath.peekLast() == null) {
                if (this.isDroppingOff) {
                    //dump items
                    System.out.println("T=" + time.toString() + " | Cart " + this.toString() + ": (" + this.getX() + ", " + this.getY() + ") Dropping of items.");
                    DropOffPoint dropOffPoint = this.order.getDropOffPoint();
                    GoodsItem item;
                    while((item = this.items.poll()) != null){
                        dropOffPoint.stockItem(item);
                    }
                    this.isDroppingOff = false;

                    //check if order is fulfilled, or if this was an intermediary drop off
                    if(this.order.isGathered()) { //could prob be cashed when setting drop off point
                        //mark order as fulfilled
                        this.order.markAsFulfilled();
                        System.out.println(String.format("T=%s | Cart %s:(%d, %d) Order Fulfilled", time.toString(), this.toString(), this.getX(), this.getY()));

                        //reset cart
                        this.items = new LinkedList<>();
                        this.isDroppingOff = false;
                        this.isFree = true;
                        this.notifyObservers();
                        return;
                    }else { //plot new path
                        this.plotNewPath();
                        System.out.println(String.format("T=%s | Cart %s:(%d, %d) Plotting path to shelf %s",
                                time.toString(), this.toString(), this.getX(), this.getY(),
                                this.closestPair.getValue().getName()
                        ));
                    }
                } else {
                    //gather from the shelf
                    System.out.println("T=" + time.toString() + " | Cart " + this.toString() + ": (" + this.getX() + ", " + this.getY() + ") Gathering from shelf");
                    closestPair.getKey().gatherFromShelf(closestPair.getValue(), this);
                }

                // check if we have gathered all the items or are full
                if (this.order.isGathered() || this.isFull()) {
                    //plot path to drop off point
                    System.out.println(String.format("T=%s | Cart %s:(%d, %d) Plotting path to drop off point. Order fulfilled: %s",
                            time.toString(), this.toString(), this.getX(), this.getY(),
                            this.order.isGathered()
                    ));
                    this.plotPathToDropOffPoint();
                }else {
                    //get next pair and path
                    this.plotNewPath();
                }
            }

            //schedule new event
            GridNode nextNode = this.curPath.peekLast();
            LocalTime t1 = time.plusSeconds((long) (this.getCurNode().distance(nextNode) / this.getSpeed()));
            System.out.println(String.format("T=%s | Cart %s:(%d, %d) scheduling event to %s",
                    time.toString(), this.toString(), this.getX(), this.getY(),
                    t1.toString()
            ));
            this.mainController.getCalendar().insertEvent(new Event(t1, 0, this::update));
        }
    }

    @Override
    public void stockItem(GoodsItem item) throws StockableCapacityExceededException {
        if(this.items.size() + 1 > this.capacity){
            throw new StockableCapacityExceededException();
        }else{
            this.items.add(item);
        }
    }

    @Override
    public GoodsItem takeItem(GoodsItem item, Stockable destination) {
        if(this.items != null) {
            if (this.items.remove(item)){
                destination.stockItem(item);
                return item;
            }else{
                return null;
            }
        }
        return null;
    }

    @Override
    public GoodsItem takeAnyOfType(Goods type, Stockable destination) {
        if(this.items != null) {
            if (!this.items.isEmpty()) {
                GoodsItem result = this.items.remove(0);
                destination.stockItem(result);
                return result;
            }
        }
        return null;
    }
}
