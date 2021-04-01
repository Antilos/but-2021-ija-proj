package ija.ija2020.proj.map;

import ija.ija2020.proj.geometry.Targetable;

import java.util.LinkedList;
import java.util.List;

public class GridMap {
    private final int width;
    private final int height;
    protected final GridNode[] grid;

    public GridMap(int height, int width) {
        this.width = width;
        this.height = height;
        this.grid = new GridNode[width*height];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                grid[i + j * width] = new GridNode(i, j, this);
            }
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getSize(){ return width * height; }

    /**
     * Get node by coordinates
     * @param x horizontal (x) coordinate of the node
     * @param y vertical (y) coordinate of the node
     * @return The node on this map with the given coordinates
     */
    public GridNode getNode(int x, int y){
        int index = x + y * width;
        if (index >= width * height || index < 0) {
            return null;
        }else{
            return grid[index];
        }
    }

    public GridNode getNode(Targetable pos){
        int index = pos.getX() + pos.getY() * width;
        if (index >= width * height || index < 0) {
            return null;
        }else{
            return grid[index];
        }
    }

    private final int[][] NEIGHBOUR_MODS = {{1,1},{1,0},{1,-1},{0,-1},{-1,-1},{-1,0},{-1,1},{0,1}};
    public List<GridNode> getUnobstructedNeighbours(int x, int y){
        List<GridNode> result = new LinkedList<GridNode>();

        for(int[] mod : NEIGHBOUR_MODS){
            int x1 = x + mod[0];
            int y1 = y + mod[1];
            if(x1 < 0 || y1 < 0){
                continue;
            }
            GridNode neigh = getNode(x1, y1);
            if (neigh != null && !neigh.isObstructed()){
                result.add(neigh);
            }
        }

        return result;
    }

    public List<GridNode> getUnobstructedNeighbours(GridNode node){
        List<GridNode> result = new LinkedList<GridNode>();

        for(int[] mod : NEIGHBOUR_MODS){
            int y = node.getY();
            int x = node.getX();
            GridNode neigh = getNode(x +mod[0], y +mod[1]);
            if (neigh != null && !neigh.isObstructed()){
                result.add(neigh);
            }
        }

        return result;
    }
}
