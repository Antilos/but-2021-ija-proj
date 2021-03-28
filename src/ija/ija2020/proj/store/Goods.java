package ija.ija2020.proj.store;

import java.time.LocalDate;
import java.util.ArrayList;

public class Goods {

    private String name;
    private ArrayList<GoodsItem> goodsList;

    public Goods(String name){
        this.name = name;
        goodsList = new ArrayList<GoodsItem>();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 17;
        result = prime * result + name.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o){
        if(this == o){
            return true;
        }
        if(null == o){
            return false;
        }
        if(!(o instanceof ija.ija2020.homework1.goods.Goods)) {
            return false;
        }
        ija.ija2020.homework1.goods.Goods goods = (ija.ija2020.homework1.goods.Goods) o;
        return goods.getName().equals(this.getName());
    }

    public String getName() {
        return name;
    }

    public boolean addItem(GoodsItem goodsItem) {
        return goodsList.add(goodsItem);
    }

    public GoodsItem newItem(LocalDate localDate) {
        GoodsItem item = new GoodsItem(this, localDate);
        goodsList.add(item);
        return item;
    }

    public boolean remove(GoodsItem goodsItem) {
        return goodsList.remove(goodsItem);
    }

    public boolean empty() {
        return goodsList.isEmpty();
    }

    public int size() {
        return goodsList.size();
    }
}
