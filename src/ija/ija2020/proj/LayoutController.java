package ija.ija2020.proj;

import ija.ija2020.proj.geometry.Drawable;
import javafx.fxml.FXML;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Shape;

import java.util.ArrayList;
import java.util.List;

public class LayoutController {

    @FXML
    private AnchorPane mainMap;

    private List<Drawable> elements = new ArrayList<>();

    @FXML
    private void onZoom(ScrollEvent scrollevent){
        scrollevent.consume();
        double zoom = scrollevent.getDeltaY() > 0 ? 1.1 : 0.9;
        mainMap.setScaleX(mainMap.getScaleX() * zoom);
        mainMap.setScaleY(mainMap.getScaleY() * zoom);
    }

    public void setElements(List<Drawable> elementspar) {
        this.elements = elementspar;
        for (Drawable drawable : elements){
            mainMap.getChildren().addAll(drawable.getGUI());
        }
    }

}
