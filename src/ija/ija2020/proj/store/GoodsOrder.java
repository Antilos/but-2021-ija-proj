package ija.ija2020.proj.store;

import ija.ija2020.proj.geometry.Rectangle;
import ija.ija2020.proj.geometry.Position;
import ija.ija2020.proj.geometry.Targetable;
import ija.ija2020.proj.store.map.StoreMap;
import javafx.util.Pair;
import jdk.jshell.spi.ExecutionControl.NotImplementedException;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GoodsOrder {

    private List<OrderItem> order;
    private List<List<GoodsShelf>> cachedOrderedShelves;

    public void add(OrderItem article){
        order.add(article);
    }

    public void add(int amount, Goods goodType){
        order.add(new OrderItem(amount, goodType));
    }

    /**
     * Get Find the closest shelve that can be gathered from to continue fulfilling this order
     * @param map Store map to search
     * @param pos Position of the vehicle that is fullfiling this order
     * @return OrderItem and shelf that is closest to pos.
     */
    public Pair<OrderItem, GoodsShelf> getClosest(StoreMap map, Targetable pos) {
        //Order the orders and shelves
        class StoreShelfDistanceComparator implements Comparator<GoodsShelf> {
            @Override
            public int compare(GoodsShelf o1, GoodsShelf o2) {
                if(o1.getArea().distance(pos) < o2.getArea().distance(pos)){
                    return -1;
                }else if(o1.getArea().distance(pos) > o2.getArea().distance(pos)){
                    return 1;
                }
                return 0;
            }
        }

        class OrderStoreShelfPairDistanceComparator implements Comparator<Pair<OrderItem, GoodsShelf>> {
            @Override
            public int compare(Pair<OrderItem, GoodsShelf> o1, Pair<OrderItem, GoodsShelf> o2) {
                if(o1.getValue().getArea().distance(pos) < o2.getValue().getArea().distance(pos)){
                    return -1;
                }else if(o1.getValue().getArea().distance(pos) > o2.getValue().getArea().distance(pos)){
                    return 1;
                }
                return 0;
            }
        }

        List<Pair<OrderItem, GoodsShelf>> shelves = new LinkedList<>();
        for (OrderItem item : order){
            if (item.getAmountRemaining() == 0){//order item already fulfilled
                continue;
            }
            List<GoodsShelf> tmp = map.getShelvesWithGoods(item.getGoodsType());

            tmp.sort(new StoreShelfDistanceComparator());
            shelves.add(new Pair<>(item, tmp.get(0)));
        }

        shelves.sort(new OrderStoreShelfPairDistanceComparator());

        //Return the closest one
        return shelves.get(0);
    }

}
