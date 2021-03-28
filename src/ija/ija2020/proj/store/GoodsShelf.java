package ija.ija2020.proj.store;

import ija.ija2020.proj.geometry.Rectangle;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GoodsShelf {

    private final Map<Goods, LinkedList<GoodsItem>> goodsDict;
    private final Rectangle area;

    public GoodsShelf(Rectangle area) {
        this.goodsDict = new HashMap<Goods, LinkedList<GoodsItem>>();
        this.area = area;
    }

    public Rectangle getArea() {
        return area;
    }

    public void put(GoodsItem goodsItem) {
        if(goodsDict.containsKey(goodsItem.goods())){
            goodsDict.get(goodsItem.goods()).add(goodsItem);
        }else{
            LinkedList<GoodsItem> newList = new LinkedList<GoodsItem>();
            newList.add(goodsItem);
            goodsDict.put(goodsItem.goods(), newList);
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

    public GoodsItem removeAny(Goods goods) {
        List<GoodsItem> tmp = goodsDict.get(goods);
        if(tmp != null) {
            if (!tmp.isEmpty()) {
                return tmp.remove(0);
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
