package ija.ija2020.proj.map;

import ija.ija2020.proj.geometry.Targetable;

import java.util.*;

public class GridNode implements Targetable {
    private final int x;
    private final int y;
    private boolean obstructed;

    public GridNode(int x, int y) {
        this.x = x;
        this.y = y;
        this.obstructed = false;
    }

    public GridNode(int x, int y, boolean isObstructed) {
        this.x = x;
        this.y = y;
        this.obstructed = isObstructed;
    }

    public boolean isObstructed() {
        return obstructed;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int distance(Targetable target) {
        //Manhattan Distance
        return Math.abs(target.getX() - this.x) + Math.abs(target.getY() - this.x);
    }

    public void setObstructed(boolean obstructed) {
        this.obstructed = obstructed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GridNode gridNode = (GridNode) o;
        return x == gridNode.x && y == gridNode.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    public Deque<GridNode> getPathToNodeOnGrid(GridMap grid, GridNode end){
        class MazeNode{
            public int x;
            public int y;

            public MazeNode(int x, int y) {
                this.x = x;
                this.y = y;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                MazeNode mazeNode = (MazeNode) o;
                return x == mazeNode.x && y == mazeNode.y;
            }

            @Override
            public int hashCode() {
                return Objects.hash(x, y);
            }

            public int f = Integer.MAX_VALUE;
            public int g = Integer.MAX_VALUE; //cost from start
            public int h = Integer.MAX_VALUE; //manhatan distance to end
            public MazeNode parent = null;
            public boolean visited = false;
            public boolean hComputed = false;
        }

        class MazeNodeComparator implements Comparator<MazeNode> {
            @Override
            public int compare(MazeNode o1, MazeNode o2) {
                if(o1.f < o2.f){
                    return -1;
                }else if(o1.f > o2.f){
                    return 1;
                }
                return 0;
            }
        }

        //A* with manhatan distance
        MazeNode endMazeNode = null;

        //prepare the maze
        int width = grid.getWidth();
        int height = grid.getHeight();
        MazeNode[] maze = new MazeNode[width*height];
        //prepare maze queue
        PriorityQueue<MazeNode> open = new PriorityQueue<MazeNode>(new MazeNodeComparator());

        //prepare start node
        MazeNode start = new MazeNode(this.x, this.y);
        start.f = 0;
        start.g = 0;
        start.h = Math.abs(end.getX() - start.x) + Math.abs(end.getY() - start.y);
        start.hComputed = true;
        open.add(start);

        while (!open.isEmpty()){
            //get first node from open
            MazeNode node = open.poll();
            node.visited = true;

            //generate neighbours
            List<GridNode> neighbours = grid.getUnobstructedNeighbours(node.x, node.y);
            for(GridNode neigh : neighbours){
                MazeNode mazeNeigh = maze[neigh.x + neigh.y * width];
                if (mazeNeigh == null){
                    mazeNeigh = new MazeNode(neigh.x, neigh.y);
                    maze[neigh.x + neigh.y * width] = mazeNeigh;
                }
                if (mazeNeigh.visited){
                    continue;
                }
                if (neigh == end) {
                    mazeNeigh.parent = node;
                    endMazeNode = mazeNeigh;
                    break;
                }

                //update root distance
                mazeNeigh.g = Math.min(mazeNeigh.g, node.g+1);

                //update heuristic
                if(!mazeNeigh.hComputed) {
                    mazeNeigh.h = Math.abs(end.getX() - mazeNeigh.x) + Math.abs(end.getY() - mazeNeigh.y);
                    mazeNeigh.hComputed = true;
                }

                //compute score
                final int minDistance = Math.min(mazeNeigh.f, mazeNeigh.g+mazeNeigh.h);
                if (minDistance != mazeNeigh.f){
                    mazeNeigh.f = minDistance;
                    mazeNeigh.parent = node;
                }

                if (!open.contains(mazeNeigh)) {
                    open.add(mazeNeigh);
                }
            }

            if(endMazeNode != null){
                break;
            }
        }

        //get the route (or failure)
        if(endMazeNode != null){
            Deque<GridNode> result = new LinkedList<GridNode>();
            MazeNode node = endMazeNode;
            while(node.parent != null){
                result.add(grid.getNode(node.x, node.y));
                node = node.parent;
            }
            return result;
        }else{
            return null;
        }
    }

}
