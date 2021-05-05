package ija.ija2020.proj.store.map;

import ija.ija2020.proj.geometry.Drawable;
import ija.ija2020.proj.map.GridMap;
import ija.ija2020.proj.map.GridNode;
import ija.ija2020.proj.store.GoodsShelf;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

import java.util.ArrayList;
import java.util.List;

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
        return shelf != null ? true : super.isObstructed();
    }

    public void toggleObstructed(){
        super.setObstructed(super.isObstructed() ? false : true);
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

    @Override
    public List<Shape> getGUI() {
        List<Shape> gui = new ArrayList<>();

        Rectangle rect = new Rectangle(getX()*100,  getY()*100, 100, 100);
        rect.setFill(Color.WHITE);
        rect.setStroke(Color.BLACK);
        rect.setStrokeWidth(2);
        if( this.hasShelf() == true){
            rect.setFill(Color.BLUE);
        }
        gui.add(rect);
        return gui;
    }
}
