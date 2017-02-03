package Jama;

public interface MatrixTestHelper {
    /** Check magnitude of difference of scalars. **/
    default void assertNormDifference(double x, double y) {
        double eps = Math.pow(2.0,-52.0);
        if (x == 0 & Math.abs(y) < 10*eps) return;
        if (y == 0 & Math.abs(x) < 10*eps) return;
        if (Math.abs(x-y) > 10*eps*Math.max(Math.abs(x),Math.abs(y))) {
            throw new RuntimeException("The difference x-y is too large: x = " + Double.toString(x) + "  y = " + Double.toString(y));
        }
    }

    /** Check norm of difference of "vectors". **/
    default void assertNormDifference(double[] x, double[] y) {
        if (x.length == y.length ) {
            for (int i=0;i<x.length;i++) {
                assertNormDifference(x[i],y[i]);
            }
        } else {
            throw new RuntimeException("Attempt to compare vectors of different lengths");
        }
    }

    /** Check norm of difference of arrays. **/
    default void assertNormDifference(double[][] x, double[][] y) {
        Matrix A = new Matrix(x);
        Matrix B = new Matrix(y);
        assertNormDifference(A,B);
    }

    /** Check norm of difference of Matrices. **/
    default void assertNormDifference(Matrix X, Matrix Y) {
        double eps = Math.pow(2.0,-52.0);
        if (X.norm1() == 0. & Y.norm1() < 10*eps) return;
        if (Y.norm1() == 0. & X.norm1() < 10*eps) return;
        if (X.minus(Y).norm1() > 1000*eps*Math.max(X.norm1(),Y.norm1())) {
            throw new AssertionError("The norm of (X-Y) is too large: " +  Double.toString(X.minus(Y).norm1()));
        }
    }
}
