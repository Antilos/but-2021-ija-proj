package ija.ija2020.proj.vehicle;

import ija.ija2020.proj.MainController;
import ija.ija2020.proj.TimeUpdateable;
import ija.ija2020.proj.calendar.Event;
import ija.ija2020.proj.geometry.Movable;
import ija.ija2020.proj.geometry.Targetable;
import ija.ija2020.proj.map.GridMap;
import ija.ija2020.proj.map.GridNode;
import ija.ija2020.proj.store.GoodsOrder;
import ija.ija2020.proj.store.GoodsShelf;
import ija.ija2020.proj.store.OrderItem;
import ija.ija2020.proj.store.map.StoreMap;
import ija.ija2020.proj.store.map.StoreNode;
import javafx.util.Pair;

import java.time.LocalTime;
import java.util.Deque;

public class Cart implements Movable, TimeUpdateable {
    private final MainController mainController = MainController.getInstance();

    private final String name;
    private final StoreMap map;
    private int capacity; //capacity in units
    private int speed; //speed in units per second

    private GoodsOrder order;
    private Pair<OrderItem, GoodsShelf> closestPair = null;
    private Deque<GridNode> curPath = null;

    private StoreNode pos;

    public Cart(String name, int capacity, int speed, StoreMap map, StoreNode pos, GoodsOrder order) {
        this.name = name;
        this.capacity = capacity;
        this.speed = speed;
        this.map = map;
        this.pos = pos;

        this.order = order;

        //schedule first event
        GridNode nextNode = this.curPath.peekLast();
        LocalTime t1 = mainController.getTime().plusSeconds((long) (this.getCurNode().distance(nextNode) / this.getSpeed()));
        this.mainController.getCalendar().insertEvent(new Event(t1, 0, this::update));
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
        System.out.println("T=" + time.toString() + " | Cart " + this.name + ": (" + this.getX() + ", " + this.getY() + ")");
        if (this.curPath != null){ //if we already have a path
            GridNode nextNode = this.curPath.pollLast();
            if (nextNode != null){ //opposite should only happen if the order was empty or if the shelf is adjecent ot us
                this.moveTo(nextNode); //move to the next node on the path
            }
        }else{
            if (this.closestPair == null){ //if we have an order pair, we just need a path to the shelf
                //find the shelf for the next order item
                this.closestPair = this.order.getClosest(this.map, this.pos);
            }
            GridNode nextNode = this.map.getNode(
                    this.closestPair.getValue().getArea().getClosestCorner(this.getCurNode())
            );
            this.curPath = this.getCurNode().getPathToNodeOnGrid(this.map, nextNode);
        }

        //if we made it to the end
        if(this.curPath.peekLast() == null){
            //gather from the shelf
            closestPair.getKey().gatherFromShelf(closestPair.getValue());

            //get next pair and path
            this.closestPair = this.order.getClosest(this.map, this.pos);
            GridNode nextNode = this.map.getNode(
                    this.closestPair.getValue().getArea().getClosestCorner(this.getCurNode())
            );
            this.curPath = this.getCurNode().getPathToNodeOnGrid(this.map, nextNode);
        }

        //schedule new event
        GridNode nextNode = this.curPath.peekLast();
        LocalTime t1 = time.plusSeconds((long) (this.getCurNode().distance(nextNode) / this.getSpeed()));
        this.mainController.getCalendar().insertEvent(new Event(t1, 0, this::update));
    }
}
