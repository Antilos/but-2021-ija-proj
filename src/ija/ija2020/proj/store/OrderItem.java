package ija.ija2020.proj.store;

import java.util.LinkedList;
import java.util.List;

public class OrderItem {
    private final int amountNeeded;
    private int amountGathered;
    private final Goods goodsType;
    private List<GoodsItem> gatheredItems = new LinkedList<>();

    public OrderItem(int amount, Goods goodsType) {
        this.amountNeeded = amount;
        this.goodsType = goodsType;
    }

    public int getAmountNeeded() {
        return amountNeeded;
    }

    public int getAmountGathered() {
        return amountGathered;
    }

    public int getAmountRemaining(){
        return amountNeeded - amountGathered;
    }

    private void gather(int amount) throws IllegalArgumentException {
        if(amount <= this.getAmountRemaining()){
            amountGathered += amount;
        }else{
            throw new IllegalArgumentException("Amount too high: only "+this.getAmountRemaining()+" items are remaining to gather.");
        }
    }

    /**
     * Gathers as many items as possible from the shelf.
     * @param shelf Shelf to gather items from
     * @return The amount of items gathered
     */
    public int gatherFromShelf(GoodsShelf shelf){
        int amountToGather = 0;
        if (shelf.containsGoods(this.goodsType)){
            amountToGather = shelf.size(this.goodsType) >= this.getAmountRemaining() ? this.getAmountRemaining() : this.getAmountRemaining() - shelf.size(this.goodsType);

            for(int i = 0; i < amountToGather; i++){
                this.gatheredItems.add(shelf.removeAny(this.goodsType));
            }
            this.gather(amountToGather);
        }
        return amountToGather;
    }

    public Goods getGoodsType() {
        return goodsType;
    }
}
