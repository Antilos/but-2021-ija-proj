package ija.ija2020.proj.vehicle;

import ija.ija2020.proj.CartController;
import ija.ija2020.proj.MainController;
import ija.ija2020.proj.TimeUpdateable;
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

public class Cart extends Observable implements Movable, TimeUpdateable, Stockable {
    private static final long START_UP_DELAY = 1;
    private final MainController mainController = MainController.getInstance();
    private final CartController parentController;

    private final StoreMap map;
    private int capacity; //capacity in units
    private int speed; //speed in units per second

    private GoodsOrder order;
    private List<GoodsItem> items;

    private Pair<OrderItem, GoodsShelf> closestPair = null;
    private Deque<GridNode> curPath = null;

    private StoreNode pos;
    private boolean isDroppingOff = false;
    private boolean isFree = true;

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

    public boolean isFree() {
        return isFree;
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

    @Override
    public void update(LocalTime time) {
        System.out.println("T=" + time.toString() + " | Cart " + this.toString() + ": (" + this.getX() + ", " + this.getY() + ")");

        if(!this.isFree) {
            if (this.curPath != null) { //if we already have a path
                GridNode nextNode = this.curPath.pollLast();
                if (nextNode != null) { //opposite should only happen if the order was empty or if the shelf is adjecent ot us
                    this.moveTo(nextNode); //move to the next node on the path
                }
            } else {
                if (this.closestPair == null) { //if we have an order pair, we just need a path to the shelf
                    //find the shelf for the next order item
                    this.closestPair = this.order.getClosest(this.map);
                }
                //get the closest unobstructed node adjacent to the shelf
                GridNode nextNode = this.map.getNode(
                        this.closestPair.getValue().getArea().getClosestCorner(this.getCurNode())
                ).getClosestUnobstructedAdjacent(this.getCurNode());
                this.curPath = this.getCurNode().getPathToNodeOnGrid(this.map, nextNode);
            }

            //check whether we made it to the end
            if (this.curPath.peekLast() == null) {
                if (this.isDroppingOff) {
                    //dump items
                    System.out.println("T=" + time.toString() + " | Cart " + this.toString() + ": (" + this.getX() + ", " + this.getY() + ") Dropping of items.");
                    DropOffPoint dropOffPoint = this.order.getDropOffPoint();
                    for (GoodsItem item : this.items) {
                        dropOffPoint.stockItem(item);
                    }

                    //mark order as fullfiled
                    this.order.markAsFullfiled();

                    //reset cart
                    this.items = new LinkedList<>();
                    this.isDroppingOff = false;
                    this.isFree = true;
                    this.notifyObservers();
                } else {
                    //gather from the shelf
                    System.out.println("T=" + time.toString() + " | Cart " + this.toString() + ": (" + this.getX() + ", " + this.getY() + ") Gathering from shelf");
                    closestPair.getKey().gatherFromShelf(closestPair.getValue(), this);
                }

                //check
                // if we have gathered all the items
                if (this.order.isGathered()) {
                    //plot path to drop off point
                    this.isDroppingOff = true;
                    this.curPath = this.getCurNode().getPathToNodeOnGrid(this.map, this.map.getNode(this.order.getDropOffPoint().getX(), this.order.getDropOffPoint().getY()));
                } else {
                    //get next pair and path
                    this.closestPair = this.order.getClosest(this.map);

                    GridNode nextNode = this.map.getNode(
                            this.closestPair.getValue().getArea().getClosestCorner(this.getCurNode())
                    );
                    this.curPath = this.getCurNode().getPathToNodeOnGrid(this.map, nextNode);
                }
            }

            //schedule new event
            GridNode nextNode = this.curPath.peekLast();
            LocalTime t1 = time.plusSeconds((long) (this.getCurNode().distance(nextNode) / this.getSpeed()));
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
