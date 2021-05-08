package ija.ija2020.proj.store.map;

import ija.ija2020.proj.geometry.Drawable;
import ija.ija2020.proj.map.GridMap;
import ija.ija2020.proj.map.GridNode;
import ija.ija2020.proj.store.GoodsShelf;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StoreNode extends GridNode {

    private GoodsShelf shelf;
    private boolean hover = false;

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
    public Map<String, Integer>  getContent() {
        return shelf.getContent();
    }

    public void observers(){
        this.setChanged();
        this.notifyObservers();
    }

    @Override
    public boolean isHovered(){
        if (this.hover == true){
            return true;
        }
        return false;
    }

    @Override
    public List<Shape> getGUI() {
        List<Shape> gui = new ArrayList<>();

        Rectangle rect = new Rectangle(getX()*100,  getY()*100, 100, 100);
        rect.setStroke(Color.BLACK);
        rect.setStrokeWidth(2);
        if( this.hasShelf() == true){

            if (hover == true){
                rect.setFill(Color.BLACK);
            } else {
                rect.setFill(Color.BLUE);
            }

            rect.addEventHandler(MouseEvent.MOUSE_ENTERED,
                    new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent e) {
                            rect.setFill(Color.BLACK);
                            hover = true;
                            observers();
                        }
                    });
            rect.addEventHandler(MouseEvent.MOUSE_EXITED,
                    new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent e) {
                            rect.setFill(Color.BLUE);
                            hover = false;
                            observers();
                        }
                    });
        } else {
            rect.setFill(Color.WHITE);
            rect.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    rect.setFill(Color.YELLOW);
                    setObstructed(true);
                }
            });
        }
        gui.add(rect);
        return gui;
    }

}
