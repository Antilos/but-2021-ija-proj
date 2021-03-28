package ija.ija2020.proj.store.map;

import ija.ija2020.proj.geometry.Rectangle;
import ija.ija2020.proj.map.GridMap;
import ija.ija2020.proj.store.Goods;
import ija.ija2020.proj.store.GoodsShelf;

import java.util.*;

public class StoreMap extends GridMap {

    private List<GoodsShelf> shelves = new ArrayList<>();

    public StoreMap(int height, int width) {
        super(height, width);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                this.grid[i + j * width] = new StoreNode(i, j);
            }
        }
    }

    public void addShelf(GoodsShelf shelf){
        if (this.shelves.contains(shelf)){
            throw new IllegalArgumentException("Shelf " + shelf.toString() + " already part of this map. Remove it first, or use the appropriate method for moving.");
        }
        //TODO: Check for overlap with another shelf
        this.shelves.add(shelf);
        Rectangle area = shelf.getArea();
        for (int i = area.getX1(); i <= area.getX2(); i++) {
            for (int j = area.getY1(); j <= area.getY2(); j++) {
                ((StoreNode) this.getNode(i, j)).setShelf(shelf);
            }
        }
    }

    public List<GoodsShelf> getShelvesWithGoods(Goods goods){
        List<GoodsShelf> result = new LinkedList<>();
        for (GoodsShelf shelf : this.shelves) {
            if (shelf.containsGoods(goods)) {
                result.add(shelf);
            }
        }

        return result;
    }




}
