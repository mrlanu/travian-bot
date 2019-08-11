public class Oasis {
    private String kind;
    private int x;
    private int y;
    private boolean hasElephants;

    public Oasis(String kind, int x, int y, boolean hasElephants) {
        this.kind = kind;
        this.x = x;
        this.y = y;
        this.hasElephants = hasElephants;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean isHasElephants() {
        return hasElephants;
    }

    public void setHasElephants(boolean hasElephantns) {
        this.hasElephants = hasElephantns;
    }

    @Override
    public String toString() {
        return "Oasis{" +
                "x=" + x +
                ", y=" + y +
                ", hasElephants=" + hasElephants +
                '}';
    }
}
