package ija.ija2020.proj.store;

import ija.ija2020.proj.geometry.Rectangle;

import java.util.*;

public class GoodsShelf implements Stockable{

    private final String name;
    private final Map<Goods, LinkedList<GoodsItem>> goodsDict;
    private final Rectangle area;

    public GoodsShelf(String name, Rectangle area) {
        this.name = name;
        this.goodsDict = new HashMap<Goods, LinkedList<GoodsItem>>();
        this.area = area;
    }

    public Rectangle getArea() {
        return area;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GoodsShelf that = (GoodsShelf) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public void stockItem(GoodsItem goodsItem) throws StockableCapacityExceededException {
        if(goodsDict.containsKey(goodsItem.getGoodsType())){
            goodsDict.get(goodsItem.getGoodsType()).add(goodsItem);
        }else{
            LinkedList<GoodsItem> newList = new LinkedList<GoodsItem>();
            newList.add(goodsItem);
            goodsDict.put(goodsItem.getGoodsType(), newList);
        }
    }

    public boolean containsGoods(Goods goods) {
        List goodsList = (List)goodsDict.get(goods);
        if (goodsList == null){
            return false;
        }else{
            return !goodsList.isEmpty();
        }
    }

    @Override
    public GoodsItem takeItem(GoodsItem item, Stockable destination) {
        List<GoodsItem> tmp = goodsDict.get(item.getGoodsType());
        if(tmp != null) {
            if (tmp.remove(item)){
                destination.stockItem(item);
                return item;
            }else{
                return null;
            }
        }
        return null;
    }

    @Override
    public GoodsItem takeAnyOfType(Goods goods, Stockable destination) {
        List<GoodsItem> tmp = goodsDict.get(goods);
        if(tmp != null) {
            if (!tmp.isEmpty()) {
                GoodsItem result = tmp.remove(0);
                destination.stockItem(result);
                return result;
            }
        }
        return null;
    }

    public int size(Goods goods) {
        List<GoodsItem> tmp = goodsDict.get(goods);
        if (tmp != null) {
            return tmp.size();
        }else{
            return 0;
        }
    }
}
