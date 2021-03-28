package ija.ija2020.proj.geometry;

public interface Targetable {
    /**
     * Get x coordinate of the target
     * @return x coordinate in world space
     */
    public int getX();

    /**
     * Get y coordinate of the target
     * @return y coordinate in world space
     */
    public int getY();

    /**
     * Calculate distance to target
     * @param target Targetable object to which to calculate distance
     * @return Distance from this object to target
     */
    public int distance(Targetable target);
}
