package ija.ija2020.proj.store;

import java.time.LocalDate;

public class GoodsItem {

    private LocalDate loadingDate;
    private Goods goodsType;

    public GoodsItem(Goods goodsType, LocalDate loadingDate){
        this.loadingDate = loadingDate;
        this.goodsType = goodsType;
    }

    public Goods goods() {
        return goodsType;
    }

    public boolean sell() {
        return this.goodsType.remove(this);
    }
}
