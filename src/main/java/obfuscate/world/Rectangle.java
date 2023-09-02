package obfuscate.world;

public class Rectangle {

    private Pos2D a;
    private Pos2D b;

    public Pos2D getA() {
        return a;
    }

    public Pos2D getB() {
        return b;
    }

    public Rectangle(Pos2D a, Pos2D b) {
        this.a = a;
        this.b = b;
    }
}
