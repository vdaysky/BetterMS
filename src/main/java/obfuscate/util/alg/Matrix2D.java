package obfuscate.util.alg;

public class Matrix2D {

    private final double[][] matrix;

    public Matrix2D(double[][] matrix) {
        this.matrix = matrix;
    }

    public Matrix2D inverse() {
        double[][] inverse = new double[2][2];

        double determinant = matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0];

        inverse[0][0] = matrix[1][1] / determinant;
        inverse[0][1] = -matrix[0][1] / determinant;
        inverse[1][0] = -matrix[1][0] / determinant;
        inverse[1][1] = matrix[0][0] / determinant;

        return new Matrix2D(inverse);
    }

    public Vector2D dot(Vector2D vector) {
        double x = matrix[0][0] * vector.getX() + matrix[0][1] * vector.getY();
        double y = matrix[1][0] * vector.getX() + matrix[1][1] * vector.getY();
        return new Vector2D(x, y);
    }

}
