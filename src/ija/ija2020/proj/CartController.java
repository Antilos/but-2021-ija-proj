package ija.ija2020.proj;

import ija.ija2020.proj.calendar.Calendar;
import ija.ija2020.proj.geometry.Position;
import ija.ija2020.proj.store.GoodsOrder;
import ija.ija2020.proj.store.map.StoreNode;
import ija.ija2020.proj.vehicle.Cart;

import java.util.LinkedList;
import java.util.List;

public class CartController {
    private final MainController mainController = MainController.getInstance();

    private List<Cart> carts = new LinkedList<>();

    public void spawnCart(String name, int capacity, int speed,  StoreNode spawnPoint, GoodsOrder order){
        //create new cart (it starts itself automatically)
        Cart cart = new Cart(name, capacity, speed, mainController.getMap(), spawnPoint, order);
        this.carts.add(cart);
    }
}
