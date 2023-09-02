package obfuscate.mechanic.version;

public class RecoilOffset {

    private double hor, vert;

    public RecoilOffset(double hor, double vert) {
        this.hor = hor;
        this.vert = vert;
    }

    public double getHor() {
        return hor;
    }

    public double getVert() {
        return vert;
    }

    public RecoilOffset add(double hor, double vert) {
        this.hor += hor;
        this.vert += vert;
        return this;
    }
}
