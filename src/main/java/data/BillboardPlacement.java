package data;

public class BillboardPlacement {
    public int year;
    public int week;
    public int placement;

    public BillboardPlacement(int year, int week, int placement) {
        this.year = year;
        this.week = week;
        this.placement = placement;
    }

    @Override
    public String toString() {
        return "BillboardPlacement{" +
                "year=" + year +
                ", week=" + week +
                ", placement=" + placement +
                '}';
    }
}
