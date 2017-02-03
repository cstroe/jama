# JAMA: Java Matrix Package

JAMA is a basic linear algebra package for Java. It provides user-level
classes for constructing and manipulating real, dense matrices. It is
meant to provide sufficient functionality for routine problems, packaged in a
way that is natural and understandable to non-experts. It is intended to
serve as **the** standard matrix class for Java, and will be proposed as
such to the [Java Grande Forum](http://www.npac.syr.edu/javagrande/) and then to
[Sun](http://java.sun.com/).

A straightforward public-domain reference implementation has been developed by
the [MathWorks](http://www.mathworks.com/) and [NIST](http://www.nist.gov/) as 
a strawman for such a class.  We are releasing this version in order to obtain 
public comment.  There is no guarantee that future versions of JAMA will be 
compatible with this one.

A sibling matrix package, [Jampack](ftp://math.nist.gov/pub/Jampack/Jampack/AboutJampack.html),
has also been developed at NIST and the University of Maryland.  The
two packages arose from the need to evaluate alternate designs for
the implementation of matrices in Java.  JAMA is based on a single
matrix class within a strictly object-oriented framework.  Jampack
uses a more open approach that lends itself to extension by the
user.  As it turns out, for the casual user the packages differ
principally in the syntax of the matrix operations.  We hope you will
take the time to look at Jampack along with JAMA.  There is much to
be learned from both packages.

## Capabilities

JAMA is comprised of six Java classes:
* Matrix
* CholeskyDecomposition
* LUDecomposition
* QRDecomposition
* SingularValueDecomposition
* EigenvalueDecomposition

The Matrix class provides the fundamental operations of numerical linear
algebra. Various constructors create Matrices from two dimensional
arrays of double precision floating point numbers. Various _gets_
and _sets_ provide access to submatrices and matrix elements.
The basic arithmetic operations include matrix addition and multiplication,
matrix norms and selected element-by-element array operations. A
convenient matrix print method is also included.

Five fundamental matrix decompositions, which consist of pairs or triples
of matrices, permutation vectors, and the like, produce results in five
decomposition classes. These decompositions are accessed by the Matrix
class to compute solutions of simultaneous linear equations, determinants,
inverses and other matrix functions. The five decompositions are:

* Cholesky Decomposition of symmetric, positive definite matrices
* LU Decomposition (Gaussian elimination) of rectangular matrices
* QR Decomposition of rectangular matrices
* Eigenvalue Decomposition of both symmetric and nonsymmetric square matrices
* Singular Value Decomposition of rectangular matrices

The current JAMA deals only with real matrices. We expect that future versions
will also address complex matrices. This has been deferred since crucial
design decisions cannot be made until certain issues regarding the 
implementation of complex in the Java language are resolved.

The design of JAMA represents a compromise between the need for pure
and elegant object-oriented design and the need to enable high performance
implementations.

<center><table border cellspacing='0' cellpadding='10' bgcolor="#FFFFCC">
<caption><b><i>Summary of JAMA Capabilities</i></b></caption>

<td align='right' valign='top'><b>Object Manipulation</b></td>
<td>constructors
<br>set elements
<br>get elements
<br>copy
<br>clone</td>
</tr>

<tr valign='top'>
<td align='right'><b>Elementary Operations</b></td>
<td>addition
<br>subtraction
<br>multiplication
<br>scalar multiplication
<br>element-wise multiplication
<br>element-wise division
<br>unary minus
<br>transpose
<br>norm</td>
</tr>

<tr valign='top'>
<td align='right'><b>Decompositions</b></td>
<td>Cholesky
<br>LU
<br>QR
<br>SVD
<br>symmetric eigenvalue
<br>nonsymmetric eigenvalue</td>
</tr>

<tr valign='TOP'>
<td align='right'><b>Equation Solution</b></td>
<td>nonsingular systems
<br>least squares</td>
</tr>

<tr valign='top'>
<td align='right' valign='top'><b>Derived Quantities</b></td>
<td>condition number
<br>determinant
<br>rank
<br>inverse
<br>pseudoinverse</td>
</tr>
</table></center>

## Example of Use

The following simple example solves a 3x3
linear system Ax=b and computes the
norm of the residual.

    double[][] array = {{1.,2.,3},{4.,5.,6.},{7.,8.,10.}};
    Matrix A = new Matrix(array);
    Matrix b = Matrix.random(3,1);
    Matrix x = A.solve(b);
    Matrix Residual = A.times(x).minus(b);
    double rnorm = Residual.normInf();

## Reference Implementation

The implementation of JAMA downloadable
from this site is meant to be a reference implementation only.
As such, it is pedagogical in nature. The algorithms employed are
similar to those of the classic Wilkinson and Reinsch Handbook, i.e. the
same algorithms used in
[EISPACK](http://www.netlib.org/eispack/), [LINPACK](http://www.netlib.org/linpack/)
and [MATLAB](http://www.mathworks.com/).

Matrices are stored internally as native Java arrays
(i.e., <code>double[][]</code>).
The coding style is straightforward and readable. While the reference
implementation itself should provide reasonable execution speed for small
to moderate size applications, we fully expect software vendors and Java
VMs to provide versions which are optimized for particular environments.

## Not Covered

JAMA is by no means a complete linear algebra
environment. For example, there are no provisions for matrices with
particular structure (e.g., banded, sparse) or for more specialized
decompositions (e.g. Shur, generalized eigenvalue).
Complex matrices are not included.
It is not our intention to ignore these important problems. We expect
that some of these (e.g. complex) will be addressed in future versions.
It is our intent that the design of JAMA not preclude extension to some
of these additional areas.

Finally, JAMA is not a general-purpose array class. Instead, it
focuses on the principle mathematical functionality required to do numerical
linear algebra. As a result, there are no methods for array operations
such as reshaping or applying elementary functions (e.g. sine, exp, log)
elementwise. Such operations, while quite useful in many applications,
are best collected into a separate **array** class.

## Versions

* Current version 1.0.3 (November 9, 2012)</p>

## Discussion Group

A discussion group has been established. Comments and suggestions sent to
[jama@nist.gov](mailto:jama@nist.gov) will automatically be sent to the 
JAMA authors, as well as to all subscribers.

To subscribe, send email to [listproc@nist.gov](mailto:listproc@nist.gov)
containing the text:

    subscribe jama _your-name_

in the message body. A public
[archive of the discussion](http://cio.nist.gov/esd/emaildir/lists/jama/maillist.html)
can be browsed.

<p class='small'>[Note: NIST will not use the email addresses provided for any
purpose other than the maintenance of this discussion list.  Participants may
remove themselves at any time by sending an email message to
<a href="mailto:listproc@nist.gov">listproc@nist.gov</a> containing the text
<pre>unsubscribe jama</pre>
in the message body. See the
<a href="http://www.nist.gov/public_affairs/disclaim.htm">NIST Privacy
Policy</a>.]</p>

## Authors

JAMA's initial design, as well as this reference implementation, was developed by

<table width="70%" style='margin-left:5em'>
<tr>
<td valign='top'>
  Joe Hicklin<br>
  <a href="http://www.nist.gov/cgi-bin/exit_nist.cgi?timeout=5&url=http://www.mathworks.com/company/cleve_bio.shtml">Cleve Moler</a><br>
  Peter Webb</td>
<td valign='bottom'>
... <i>from <a href="http://www.mathworks.com/">The MathWorks</a></i></td>
<td width='5em'>&nbsp;</td>
<td valign='top'>
  <a href="/~RBoisvert/">Ronald F. Boisvert</a><br>
  <a href="/~BMiller/">Bruce Miller</a><br>
  <a href="/~RPozo/">Roldan Pozo</a><br>
  <a href="/~KRemington/">Karin Remington</a></td>
<td valign='bottom'>
... <i>from <a href="http://www.nist.gov/">NIST</a></i></td>
</tr>
</table>
</p>

## License

This software is a cooperative product of The MathWorks and the National 
Institute of Standards and Technology (NIST) which has been released to the
public domain.  Neither The MathWorks nor NIST assumes any responsibility
whatsoever for its use by other parties, and makes no guarantees, expressed
or implied, about its quality, reliability, or any other characteristic.


## Related Links &amp; Libraries

* [Java for Computational Science and Engineering](http://www.npac.syr.edu/projects/javaforcse/)
* [Java Numerics Working Group](http://math.nist.gov/javanumerics/)
* [NIST Mathematical and Computational Sciences Division](http://math.nist.gov/mcsd/)


As Jama is in the public domain
other developers are free to adopt and adapt this code
to other styles of programming or to extend or modernize the API.
You might find one of these libraries to be more suitable to your purposes.
Make note, however, that NIST makes **no endorsement** of these projects.  
We are currently aware of the following ports of Jama:

* [de.mukis.jama](http://muuki88.github.com/jama-osgi/) is hosted at Apache's Maven, with unit tests.

## Footer

Identification of commercial products on this page is
for information only, and does not imply recommendation or endorsement
by the National Institute of Standards and Technology.
