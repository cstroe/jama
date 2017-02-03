package Jama;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import static java.time.Duration.ofSeconds;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;

/** TestMatrix tests the functionality of the Jama Matrix class and associated decompositions.
 <P>
 Run the test from the command line using
 <BLOCKQUOTE><PRE><CODE>
 java Jama.test.TestMatrix 
 </CODE></PRE></BLOCKQUOTE>
 Detailed output is provided indicating the functionality being tested
 and whether the functionality is correctly implemented.   Exception handling
 is also tested.
 <P>
 The test is designed to run to completion and give a summary of any implementation errors
 encountered. The final output should be:
 <BLOCKQUOTE><PRE><CODE>
 TestMatrix completed.
 Total errors reported: n1
 Total warning reported: n2
 </CODE></PRE></BLOCKQUOTE>
 If the test does not run to completion, this indicates that there is a
 substantial problem within the implementation that was not anticipated in the test design.
 The stopping point should give an indication of where the problem exists.
 **/
@DisplayName("A Java Matrix")
public class MatrixTest implements MatrixTestHelper {
    private Matrix A, B, I, SUB, M, R, Z, O;

    private final int invalidld = 5; /* should trigger bad shape for construction with val */
    private final int validld = 3; /* leading dimension of intended test Matrices */
    private final int ib=1, ie=2, jb=1, je=3; /* index ranges for sub Matrix */

    private final Class<? extends Throwable> oob = ArrayIndexOutOfBoundsException.class;
    private final Class<? extends Throwable> iae = IllegalArgumentException.class;

    private double[] columnwise, rowwise;
    private double[][] avals;
    private int[] columnindexset, rowindexset, badcolumnindexset, badrowindexset;

    @BeforeEach
    public void setUp() {
        columnwise = new double[] {1.,2.,3.,4.,5.,6.,7.,8.,9.,10.,11.,12.};
        rowwise = new double[] {1.,4.,7.,10.,2.,5.,8.,11.,3.,6.,9.,12.};
        avals = new double[][] {{1.,4.,7.,10.},{2.,5.,8.,11.},{3.,6.,9.,12.}};

        columnindexset = new int[] {1,2,3};
        rowindexset = new int[] {1,2};
        badcolumnindexset = new int[] {1,2,4};
        badrowindexset = new int[] {1,3};

        A = new Matrix(columnwise,validld);
        B = new Matrix(avals);
        SUB = new Matrix(new double[][]{{5., 8., 11.}, {6., 9., 12.}});
        I = new Matrix(new double[][]{{1., 0., 0., 0.}, {0., 1., 0., 0.}, {0., 0., 1., 0.}});
        M = new Matrix(2,3,0.);
        R = Matrix.random(A.getRowDimension(), A.getColumnDimension());
        Z = new Matrix(A.getRowDimension(),A.getColumnDimension());
        O = new Matrix(A.getRowDimension(),A.getColumnDimension(),1.0);
    }

    @Nested @DisplayName("its constructor")
    class Constructor {
        @Test @DisplayName("should throw an exception on row mismatch")
        public void testConstructorArg() {
            assertThrows(iae, () -> new Matrix(columnwise,invalidld));
        }

        @Test @DisplayName("should throw exception if input array is 'ragged'")
        public void testRaggedArray() {
            final double[][] rvals = {{1.,4.,7.},{2.,5.,8.,11.},{3.,6.,9.,12.}};
            final int raggedr = 0; /* (raggedr,raggedc) should be out of bounds in ragged array */
            final int raggedc = 4;

            assertThrows(IllegalArgumentException.class, () -> {
                Matrix A = new Matrix(rvals);
                A.get(raggedr,raggedc);
            });
        }

        @Test @DisplayName("should produce deep copies")
        public void testDeepCopy() {
            Matrix A = new Matrix(columnwise,validld);
            Matrix B = new Matrix(avals);
            double tmp = B.get(0,0);
            avals[0][0] = 0.0;
            B.minus(A);
            avals[0][0] = tmp;
            B = Matrix.constructWithCopy(avals);
            tmp = B.get(0,0);
            avals[0][0] = 0.0;
            avals[0][0] = columnwise[0];

            assertThat(0.0, is(closeTo(tmp - B.get(0,0), 0.000001)));
        }

        @Test @DisplayName("should create identity matrix")
        public void testIdentity() {
            assertNormDifference(I, Matrix.identity(3,4));
        }
    }

    @Nested @DisplayName("its access methods")
    class AccessMethods {
        @Test @DisplayName("should correctly return the number of rows")
        public void testGetRowDimension() {
            assertEquals(3, B.getRowDimension());
        }

        @Test @DisplayName("should correctly return the number of columns")
        public void testGetColumnDimension() {
            assertEquals(4, B.getColumnDimension());
        }

        @Test @DisplayName("should return the backing array upon a shallow copy")
        public void testGetArray() {
            assertSame(avals, B.getArray());
        }

        @Test @DisplayName("should return a copy of the array upon a deep copy")
        public void testGetArrayCopy() {
            double[][] barray = B.getArrayCopy();
            assertNotSame(avals, barray, "Data should be deep copied.");
            assertNormDifference(barray, avals);
        }

        @Test @DisplayName("should successfully (deep) copy by columns")
        public void testGetColumnPackedCopy() {
            assertNormDifference(B.getColumnPackedCopy(), columnwise);
        }

        @Test @DisplayName("should successfully (deep) copy by rows")
        public void testGetRowPackedCopy() {
            assertNormDifference(B.getRowPackedCopy(), rowwise);
        }

        @Test @DisplayName("should throw exception when accessing data beyond matrix dimensions")
        public void testThrowsOutOfBoundIndexException() {
            assertAll("OutOfBoundsException expected but not thrown",
                () -> assertThrows(oob, () -> B.get(B.getRowDimension(), B.getColumnDimension() - 1)),
                () -> assertThrows(oob, () -> B.get(B.getRowDimension() - 1, B.getColumnDimension()))
            );
        }

        @Test @DisplayName("should correctly retrieve a value by coordinates")
        public void testGet() {
            final double expected = avals[B.getRowDimension()-1][B.getColumnDimension()-1];
            assertEquals(expected, B.get(B.getRowDimension()-1, B.getColumnDimension()-1));
        }

        @Test @DisplayName("should check bounds when retrieving sub-matrix")
        public void testGetMatrixThrowsException() {
            assertThrows(oob, () -> B.getMatrix(ib, ie+B.getRowDimension()+1, jb, je));
            assertThrows(oob, () -> B.getMatrix(ib, ie, jb, je+B.getColumnDimension()+1));
        }

        @Test @DisplayName("should retrieve sub-matrix")
        public void testGetMatrix() {
            assertNormDifference(SUB, new Matrix(avals).getMatrix(ib,ie,jb,je));
        }

        @Test @DisplayName("should check bounds when getting a sub-matrix using a column index set")
        public void testGetMatrixWithColumnIndex() {
            assertThrows(oob, () -> B.getMatrix(ib, ie, badcolumnindexset));
            assertThrows(oob, () -> B.getMatrix(ib, ie+B.getRowDimension()+1, columnindexset));
        }

        @Test @DisplayName("should successfully retrieve sub-matrix using column index set")
        public void testGetSubMatrixUsingColumnIndexSet() {
            assertNormDifference(SUB, B.getMatrix(ib,ie,columnindexset));
        }

        @Test @DisplayName("should check bounds when getting a sub-matrix using a row index")
        public void testGetSubMatrixWithRowIndexShouldCheckBounds() {
            assertThrows(oob, () -> B.getMatrix(badrowindexset, jb, je));
            assertThrows(oob, () -> B.getMatrix(rowindexset, jb, je+B.getColumnDimension()+1));
        }

        @Test @DisplayName("should successfully retrieve a sub-matrix using a row index")
        public void testGetSubMatrixUsingRowIndexSet() {
            assertNormDifference(SUB, B.getMatrix(rowindexset,jb,je));
        }

        @Test @DisplayName("should check index bounds when using two index sets")
        public void testChecksIndexBoundsWithTwoSets() {
            assertThrows(oob, () -> B.getMatrix(badrowindexset,columnindexset));
            assertThrows(oob, () -> B.getMatrix(rowindexset,badcolumnindexset));
        }

        @Test @DisplayName("should successfully return sub-matrix when using two index sets")
        public void testGetSubMatrixWithTwoSets() {
            assertNormDifference(SUB, B.getMatrix(rowindexset,columnindexset));
        }
    }

    @Nested @DisplayName("its mutator methods")
    class MutatorMethods {
        @Test @DisplayName("should check bounds when setting values in the matrix")
        public void testSetChecksBounds() {
            assertThrows(oob, () -> B.set(B.getRowDimension(), B.getColumnDimension() - 1, 0.));
            assertThrows(oob, () -> B.set(B.getRowDimension() - 1, B.getColumnDimension(), 0.));
        }

        @Test @DisplayName("should set values in the matrix")
        public void testSetWorks() {
            B.set(ib, jb, 0.);
            assertNormDifference(B.get(ib, jb), 0.);
        }

        @Test @DisplayName("should check matrix bounds when setting matrix using sub-matrix coordinates")
        public void testSetMatrixUsingBoundsChecksBounds() {
            assertThrows(oob, () -> B.setMatrix(ib, ie + B.getRowDimension() + 1, jb, je, M));
            assertThrows(oob, () -> B.setMatrix(ib, ie, jb, je + B.getColumnDimension() + 1, M));
        }

        @Test @DisplayName("should set sub-matrix using coordinates")
        public void testSetMatrixUsingBoundsWorks() {
            B.setMatrix(ib, ie, jb, je, M);
            assertNormDifference(M.minus(B.getMatrix(ib, ie, jb, je)), M);
            B.setMatrix(ib, ie, jb, je, SUB);
        }

        @Test @DisplayName("should check bounds when setting a sub-matrix using a column index set")
        public void testSetSubMatrixUsingColumnIndexChecksBounds() {
            assertThrows(oob, () -> B.setMatrix(ib, ie+B.getRowDimension()+1, columnindexset, M));
            assertThrows(oob, () -> B.setMatrix(ib, ie, badcolumnindexset, M));
        }

        @Test @DisplayName("should set a sub-matrix using a column index set")
        public void testSetSubMatrixUsingColumnIndexSet() {
            B.setMatrix(ib,ie,columnindexset,M);
            assertNormDifference(M.minus(B.getMatrix(ib,ie,columnindexset)),M);
            B.setMatrix(ib,ie,jb,je,SUB);
        }

        @Test @DisplayName("should check bounds when setting a sub-matrix using a row index set")
        public void testSetSubMatrixWithRowIndexSetChecksBounds() {
            assertThrows(oob, () -> B.setMatrix(rowindexset, jb, je+B.getColumnDimension()+1, M));
            assertThrows(oob, () -> B.setMatrix(badrowindexset, jb, je, M));
        }

        @Test @DisplayName("should set sub-matrix using a row index set with bounds")
        public void testSetSubMatrixWithRowIndexset() {
            B.setMatrix(rowindexset, jb, je, M);
            assertNormDifference(M.minus(B.getMatrix(rowindexset, jb, je)), M);
            B.setMatrix(ib, ie, jb, je, SUB);
        }

        @Test @DisplayName("should check bounds when setting a sub-matrix with two index sets and a matrix")
        public void testShouldCheckBoundsWhenSettingSubMatrixWithTwoIndexSetsAndAMatrix() {
            assertThrows(oob, () -> B.setMatrix(rowindexset,badcolumnindexset,M));
            assertThrows(oob, () -> B.setMatrix(badrowindexset,columnindexset,M));
        }

        @Test @DisplayName("should set a sub-matrix when using two index sets and a matrix")
        public void testSetSubMatrixTwoSetsAndAMatrix() {
            B.setMatrix(rowindexset,columnindexset,M);
            assertNormDifference(M.minus(B.getMatrix(rowindexset,columnindexset)),M);
        }
    }

    @Nested @DisplayName("its array-like methods")
    public class ArrayLikeMethods {
        private final int nonconformld = 4; /* leading dimension which is valid, but nonconforming */
        private Matrix S, C;

        @BeforeEach
        public void setUp() {
            S = new Matrix(columnwise,nonconformld);
            A = R;
        }

        @Test @DisplayName("should raise nonconformance on minus")
        public void testNonconformance() {
            assertThrows(iae, () -> S = A.minus(S));
        }

        @Test @DisplayName("should perform subtraction")
        public void testMinus() {
            assertThat(0., is(closeTo(A.minus(R).norm1(), 0.000001)));
        }

        @Test @DisplayName("should raise nonconformance on minusEquals")
        public void testMinusEqualsCheck() {
            A = R.copy();
            A.minusEquals(R);
            assertThrows(iae, () -> A.minusEquals(S));
        }

        @Test @DisplayName("should perform subtraction and assignment")
        public void testMinusEquals() {
            A = R.copy();
            A.minusEquals(R);
            assertThat(A.norm1(), is(closeTo(0., 0.000001)));
            Z = new Matrix(A.getRowDimension(),A.getColumnDimension());
            assertThat(A.minus(Z).norm1(), is(closeTo(0., 0.0000001)));
        }

        @Test @DisplayName("should raise nonconformance when doing plus")
        public void testPlusNonconformance() {
            A = R.copy();
            B = Matrix.random(A.getRowDimension(),A.getColumnDimension());
            C = A.minus(B);
            assertThrows(iae, () -> A.plus(S));
            assertNormDifference(C.plus(B),A);
        }

        @Test @DisplayName("should preserve additivity: (C = A - B, and C + B == A)")
        public void testAdditivity() {
            A = R.copy();
            B = Matrix.random(A.getRowDimension(),A.getColumnDimension());
            C = A.minus(B);
            assertNormDifference(C.plus(B),A);
        }

        @Test @DisplayName("should successfully perform plusEquals")
        public void testMinusPlusEquals() {
            C = A.minus(B);
            C.plusEquals(B);
            assertThrows(iae, () -> A.plusEquals(S));
            assertNormDifference(C,A);
        }

        @Test @DisplayName("should perform uminus: (-A + A == zeros) ")
        public void testUminus() {
            A = R.uminus();
            assertNormDifference(A.plus(R),Z);
        }

        @Test @DisplayName("should check nonconformance on arrayLeftDivide")
        public void testArrayLeftDivideNonconformance() {
            A = R.copy();
            C = A.arrayLeftDivide(R);
            assertThrows(iae, () -> A.arrayLeftDivide(S));
        }

        @Test @DisplayName("should perform arrayLeftDivide properly: (M.\\M != ones)")
        public void testArrayLeftDivide() {
            A = R.copy();
            O = new Matrix(A.getRowDimension(),A.getColumnDimension(),1.0);
            C = A.arrayLeftDivide(R);
            assertNormDifference(C,O);
        }

        @Test @DisplayName("should check nonconformance on arrayLeftDivideEquals")
        public void testArrayLeftDivideEqualsNonconformance() {
            A = R.copy();
            assertThrows(iae, () -> A.arrayLeftDivideEquals(S));
        }

        @Test @DisplayName("should perform arrayLeftDivideEquals")
        public void testArrayLeftDivideEquals() {
            A = R.copy();
            A.arrayLeftDivideEquals(R);
            assertNormDifference(A,O);
        }

        @Test @DisplayName("should check nonconformance on arrayRightDivide")
        public void testArrayRightDivideNonconformanceCheck() {
            A = R.copy();
            assertThrows(iae, () -> A.arrayRightDivide(S));
        }

        @Test @DisplayName("should successfully perform arrayRightDivide")
        public void testArrayRightDivide() {
            C = A.arrayRightDivide(R);
            assertNormDifference(C,O);
        }

        @Test @DisplayName("should check nonconformance on arrayRightDivideEquals")
        public void testArrayRightDivideEqualsNonconformanceCheck() {
            assertThrows(iae, () -> A.arrayRightDivideEquals(S));
        }

        @Test @DisplayName("should successfully perform arrayRightDivideEquals (M./M = ones)")
        public void testArrayRightDivideEquals() {
            A.arrayRightDivideEquals(R);
            assertNormDifference(A,O);
        }

        @Test @DisplayName("should check nonconformance when doing arrayTimes")
        public void testArrayTimesNonconformance() {
            A = R.copy();
            assertThrows(iae, () -> A.arrayTimes(S));
        }

        @Test @DisplayName("should successfully perform arrayTimes: (A = R, C = A.*B, and C./B = A)")
        public void testArrayTimes() {
            A = R.copy();
            C = A.arrayTimes(B);
            assertNormDifference(C.arrayRightDivideEquals(B), A);
        }

        @Test @DisplayName("should check for nonconformance when performing arrayTimesEquals")
        public void testArrayTimesEqualsNonconformanc() {
            assertThrows(iae, () -> A.arrayTimesEquals(S));
        }

        @Test @DisplayName("should perform arrayTimesEquals: (A = R, A = A.*B, and A./B = R)")
        public void test() {
            A.arrayTimesEquals(B);
            assertNormDifference(A.arrayRightDivideEquals(B),R);
        }
    }

    @Nested @DisplayName("its I/O methods")
    class InputOutputMethods {
        @Test @DisplayName("should write and read from a stream")
        public void testStreamReadWrite() throws IOException {
            DecimalFormat fmt = new DecimalFormat("0.0000E00");
            fmt.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintWriter FILE = new PrintWriter(baos);
            A.print(FILE,fmt,10);
            FILE.close();
            Matrix R = Matrix.read(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(baos.toByteArray()))));
            assertThat(A.minus(R).norm1(), is(closeTo(0., 0.000001)));
        }

        @Test @DisplayName("should searialize using Java Serialization")
        public void testJavaSerialization() throws IOException, ClassNotFoundException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(baos);
            out.writeObject(R);
            out.flush();
            ObjectInputStream sin = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
            A = (Matrix) sin.readObject();
            assertNormDifference(A,R);
        }
    }

    @Nested @DisplayName("its Linear Algebra methods")
    class LinearAlgebraMethods {
        private Matrix T;
        private double[][] pvals = {{4.,1.,1.},{1.,2.,3.},{1.,3.,6.}};

        @BeforeEach
        public void setUp() {
            A = new Matrix(columnwise,3);
        }

        @Test @DisplayName("should compute transpose")
        public void testTranspose() {
            T = new Matrix(new double[][] {{1.,2.,3.},{4.,5.,6.},{7.,8.,9.},{10.,11.,12.}});
            assertNormDifference(A.transpose(),T);
        }

        @Test @DisplayName("should compute correct norm")
        public void testTransposeNorm() {
            assertNormDifference(A.norm1(),33.);
        }

        @Test @DisplayName("should compute correct normInf")
        public void testTransposeNormInf() {
            assertNormDifference(A.normInf(), 30.);
        }

        @Test @DisplayName("should compute correct normF")
        public void testTranposeNormF() {
            assertNormDifference(A.normF(), Math.sqrt(650));
        }

        @Test @DisplayName("should compute trace")
        public void testTrace() {
            assertNormDifference(A.trace(), 15);
        }

        @Test @DisplayName("should compute determinant")
        public void testDeterminant() {
            assertNormDifference(A.getMatrix(0,A.getRowDimension()-1, 0, A.getRowDimension()-1).det(), 0.);
        }

        @Test @DisplayName("should compute correct Matrix*Matrix calculation")
        public void testMatrixTimesMatrix() {
            double[][] square = {{166.,188.,210.},{188.,214.,240.},{210.,240.,270.}};
            Matrix SQ = new Matrix(square);
            assertNormDifference(A.times(A.transpose()),SQ);
        }

        @Test @DisplayName("should multiply by zero")
        public void testMultiplyByZero() {
            assertNormDifference(A.times(0.), Z);
        }

        @Test @DisplayName("should compute QR decomposition")
        public void testQRdecomp() {
            A = new Matrix(columnwise,4);
            QRDecomposition QR = A.qr();
            R = QR.getR();
            assertNormDifference(A,QR.getQ().times(R));
        }

        @Test @DisplayName("should compute SVD")
        public void testSVD() {
            A = new Matrix(columnwise,4);
            SingularValueDecomposition SVD = A.svd();
            assertNormDifference(A,SVD.getU().times(SVD.getS().times(SVD.getV().transpose())));
        }

        @Test @DisplayName("should compute rank")
        public void testRank() {
            Matrix DEF = new Matrix(avals);
            assertNormDifference(DEF.rank(),Math.min(DEF.getRowDimension(),DEF.getColumnDimension())-1);
        }

        @Test @DisplayName("should compute cond")
        public void testCond() {
            double[][] condmat = {{1.,3.},{7.,9.}};
            Matrix B = new Matrix(condmat);
            SingularValueDecomposition SVD = B.svd();
            double [] singularvalues = SVD.getSingularValues();
            assertNormDifference(B.cond(),singularvalues[0]/singularvalues[Math.min(B.getRowDimension(),B.getColumnDimension())-1]);
        }

        @Test @DisplayName("should compute LU decomposition")
        public void testLUdecomp() {
            A = new Matrix(columnwise,4);
            int n = A.getColumnDimension();
            A = A.getMatrix(0,n-1,0,n-1);
            A.set(0,0,0.);
            LUDecomposition LU = A.lu();
            assertNormDifference(A.getMatrix(LU.getPivot(),0,n-1),LU.getL().times(LU.getU()));
        }

        @Test @DisplayName("should compute inverse")
        public void testInverse() {
            A = new Matrix(columnwise,4);
            int n = A.getColumnDimension();
            A = A.getMatrix(0,n-1,0,n-1);
            A.set(0,0,0.);
            Matrix X = A.inverse();
            assertNormDifference(A.times(X),Matrix.identity(3,3));
        }

        @Test @DisplayName("should compute solve")
        public void testSolve() {
            double[][] sqSolution = {{13.},{15.}};
            Matrix O = new Matrix(SUB.getRowDimension(),1,1.0);
            Matrix SOL = new Matrix(sqSolution);
            Matrix SQ = SUB.getMatrix(0,SUB.getRowDimension()-1,0,SUB.getRowDimension()-1);
            assertNormDifference(SQ.solve(SOL),O);
        }

        @Test @DisplayName("should compute CholeskyDecomposition")
        public void testCholeskyDecomposition() {
            A = new Matrix(pvals);
            CholeskyDecomposition Chol = A.chol();
            Matrix L = Chol.getL();
            assertNormDifference(A,L.times(L.transpose()));
        }

        @Test @DisplayName("should computer CholeskyDecomposition solve")
        public void testCholeskyDecompositionSolve() {
            A = new Matrix(pvals);
            CholeskyDecomposition Chol = A.chol();
            Matrix X = Chol.solve(Matrix.identity(3,3));
            assertNormDifference(A.times(X),Matrix.identity(3,3));
        }

        @Test @DisplayName("should compute symmetric Eigenvalue decomposition")
        public void testEigenvalues() {
            A = new Matrix(pvals);
            EigenvalueDecomposition Eig = A.eig();
            Matrix D = Eig.getD();
            Matrix V = Eig.getV();
            assertNormDifference(A.times(V),V.times(D));
        }

        @Test @DisplayName("should compute non-symmetric Eigenvalue decomposition")
        public void testEigenvalueDecomNonSym() {
            double[][] evals = {{0.,1.,0.,0.},{1.,0.,2.e-7,0.},{0.,-2.e-7,0.,1.},{0.,0.,1.,0.}};
            A = new Matrix(evals);
            EigenvalueDecomposition Eig = A.eig();
            Matrix D = Eig.getD();
            Matrix V = Eig.getV();
            assertNormDifference(A.times(V),V.times(D));
        }

        @Test @DisplayName("should not hang on eigenvalue decompsition")
        public void test() {
            double[][] badeigs = {{0,0,0,0,0}, {0,0,0,0,1},{0,0,0,1,0}, {1,1,0,0,1},{1,0,1,0,1}};
            Matrix bA = new Matrix(badeigs);
            assertTimeout(ofSeconds(10), bA::eig);
        }
    }
}
