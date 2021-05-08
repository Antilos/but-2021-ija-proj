package ija.ija2020.proj;

import ija.ija2020.proj.geometry.Drawable;
import ija.ija2020.proj.store.GoodsOrder;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Shape;

import java.util.ArrayList;
import java.util.List;

public class LayoutController {

    @FXML
    private AnchorPane mainMap;

    @FXML
    private TextArea content;

    @FXML
    private TextArea orderfield;

    private List<Drawable> elements = new ArrayList<>();

    @FXML
    private void onZoom(ScrollEvent scrollevent){
        scrollevent.consume();
        double zoom = scrollevent.getDeltaY() > 0 ? 1.1 : 0.9;
        mainMap.setScaleX(mainMap.getScaleX() * zoom);
        mainMap.setScaleY(mainMap.getScaleY() * zoom);
    }


    private void queueOrder(String order){
        MainController.getInstance().queueOrder(MainController.getInstance().parseOrder(order));
    }


    @FXML
    private void order(){
        System.out.println("order");
        queueOrder(orderfield.getText());
    }

    public void setElements(List<Drawable> elementspar) {
        this.elements = elementspar;
        for (Drawable drawable : elements){
            mainMap.getChildren().addAll(drawable.getGUI());
            if( drawable.isHovered()){
                if(drawable.getContent() != null){
                    String s = "";
                    for (String key: drawable.getContent().keySet()) {
                        s += key;
                        s += " ";
                        s += drawable.getContent().get(key);
                        s += "\n";
                    }
                    if ( drawable.getContent().size() == 0){
                        content.setText("empty");
                    }else {
                        content.setText(s);
                    }
                }
            }else{
                content.setText("");
            }
        }
    }

}
