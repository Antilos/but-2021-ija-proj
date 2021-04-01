package ija.ija2020.proj.store.map;

import ija.ija2020.proj.map.GridMap;
import ija.ija2020.proj.map.GridNode;
import ija.ija2020.proj.store.GoodsShelf;

public class StoreNode extends GridNode {

    private GoodsShelf shelf;

    public StoreNode(int x, int y, StoreMap parentMap, GoodsShelf shelf) {
        super(x, y, parentMap);
        this.shelf = shelf;
    }

    public StoreNode(int x, int y, StoreMap parentMap) {
        super(x, y, parentMap);
        this.shelf = null;
    }

    @Override
    public boolean isObstructed() {
        return shelf != null;
    }

    public boolean hasShelf(){
        return shelf != null;
    }

    public GoodsShelf getShelf() {
        return shelf;
    }

    public void setShelf(GoodsShelf shelf) {
        this.shelf = shelf;
    }
}
