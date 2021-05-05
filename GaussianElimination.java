/*
Student: Thomas Welborn
Course: COSC 3312 - Numerical Methods
Title: Programming Assignment 2 - GaussianElimination.java
Professor: Cooper
Date Created: 11/01/19
Last Modified: 11/20/19

Assignment Instructions:
Given a linear s of no more than three equations, each of which has exactly
three terms (x1, x2, and x3) and a solution,
Write a program that does the following:
---Allows input for the coefficients for each term and the solution
---Performs Gaussian elimination to put the system in row echelon form
---Uses back substitution to compute values for x1, x2, and x3
---Performs GE with partial pivot to recompute the values for x1, x2, and x3
You should turn in the following:
---Source code with appropriate internal documentation
---A Microsoft Word document containing output from at least three different linear systems of which at least 2 must be ill conditioned systems.
---A descriptive comparison of the results of the two methods
*/

/**
The purpose of this program is to take a user provided arbitrary matrix of doubles
denoted as a two dimensional array with 4 terms and user provided solution for x1, x2, and x3. Then calculate
the values of x1, x2, and x3 using Gaussian Elimination and partial pivoting. Once the calculated values
are found, they are compared to the solutions provided by the user, and the relative errors are
calculated and displayed.
*/

import java.io.*;   // for using scanner class
import java.util.*; // for printing arrays and using scanner class
import java.lang.*; // for parsing and other math methods

public class GaussianElimination
{
  public static void main(String[] args) throws IOException
  {
	boolean userFlag = false; // for regulating whether or not user wants to enter another matrix to be evaluated
	do
	{
		// set-up needed variables
		Scanner keyboard = new Scanner(System.in); // for taking user input
		boolean tempUserFlag = false; // for ensuring valid user input
		String tempUserInput = " ";
		double tempUserDouble = 0.0;
		double[][] myMatrix = new double[3][4]; // the user's created matrix x1, x2, x3, y
		double[] mySolutions = new double[3]; // the user provided actual values of x1, x2, x3 used for relative error calculation

        // matricies used for test cases
        //double[][] myMatrix = {{.3, -1, 2, 8}, {1, 0, -1, -1}, {4, 2,-3, -4}};
        //double[] mySolutions = {1.96, -1.48, 2.96};

        //double[][] myMatrix = {{.4, -10, 10, 57}, {10, -9, 10, 68}, {20, -30, 40, 231}};
        //double[] mySolutions = {1.36, -2.22, 3.44};

        //double[][] myMatrix = {{.02, 0, -1, -3}, {.1, .1, .1, 1}, {3, -2, 1, 2}};
        //double[] mySolutions = {1, 2, 3};


		// make user provided matrix and store in myMatrix
		for (int i = 0; i < 3; i++)
		{
		  for (int j = 0; j < 4; j++)
		  {
			do
			{
			  System.out.println("Please enter your number for row " + (i+1) + ", column " + (j+1) + ": ");
			  tempUserInput = keyboard.nextLine();
			  try
			  {
				tempUserDouble = Double.parseDouble(tempUserInput);
			  }
			  catch(Exception e)
			  {
				System.out.println("The number you have entered is invalid. Only doubles may be entered. Try again.");
				continue; // will go back to beginning of do-while, letting the user re-enter their number
			  }
			  myMatrix[i][j] = tempUserDouble;
			  tempUserFlag = true;
			} while(!tempUserFlag);
			tempUserFlag = false;
		  }
		}

		// get user provided x1, x2, x3 values
		tempUserFlag = false;
		for (int i=0; i<3; i++)
		{
		  do
		  {
			System.out.println("Please enter the solution for x" + (i+1) + " : ");
			tempUserInput = keyboard.nextLine();
			try
			{
			  tempUserDouble = Double.parseDouble(tempUserInput);
			}
			catch(Exception e)
			{
			  System.out.println("The number you have entered is invalid. Only doubles may be entered. Try again.");
			  continue; // will go back to beginning of do-while, letting the user re-enter their number
			}
			mySolutions[i] = tempUserDouble;
			tempUserFlag = true;
		  } while(!tempUserFlag);
		  tempUserFlag = false;
		}

		//System.out.println(myMatrix);
		//System.out.println(Arrays.toString(myMatrix));
		System.out.println("The matrix you have created is:");
		System.out.println(Arrays.deepToString(myMatrix));
		System.out.println("------------------------------------------------------");
		System.out.println("Now performing Gaussian Elimination:");

		// convert matrix to row echelon
		double[][] rowEchelon = makeRowEchelon(myMatrix);
		double[][] rowEchelonPP = makeRowEchelonPartialPivot(myMatrix);

		//
		System.out.println("Solutions for x1, x2, and x3 are:");
		double[] noPivSolutions = new double[3];
		noPivSolutions = backSubstitution(rowEchelon);
		System.out.println(Arrays.toString(noPivSolutions));

		System.out.println("Solutions using partial pivoting:");
		double[] pivSolutions = new double[3];
		pivSolutions = backSubstitution(rowEchelonPP);
		System.out.println(Arrays.toString(pivSolutions));

		// get relative errors
		System.out.println("Here are the relative errors for the no pivot matrix:");
		double[] noPivErrors = new double[3];
		noPivErrors = findRelativeError(mySolutions, noPivSolutions);

		System.out.println("Here are the relative errors for the partial pivot matrix:");
		double[] pivErrors = new double[3];
		pivErrors = findRelativeError(mySolutions, pivSolutions);

		// determine if the user would like to enter another matrix
		System.out.println("------------------------------------------------------------");
		System.out.println("Would you like to enter another matrix for evaluation? (y/n): ");
		tempUserInput = keyboard.nextLine();
		if (tempUserInput == "y" || tempUserInput == "Y") {
		  userFlag = false;
		}
		else {
		  userFlag = true;
		}

    } while (!userFlag);
    System.out.println("Thank you for using GaussianElimination. Bye!");
  }//end main


  /**
  The makeRowEchelon method takes in a user-provided two-dimensional array of doubles with 3 rows
  and 4 columns. It will make a copy of the two-d array, manipulate it to row echelon form using Gaussian
  Elimination, and return it WITHOUT using partial pivoting.
  The algorithm used was partially taken from slide 06-linear systems.
  tempMatrix1 is manipulated to make the first column 0's.
  then tempMatrix 1 is copied to tempMatrix 2.
  tempMatrix2 is manipulated to make the last rows second column 0. then tempMatrix2 is returned.
  @param double[][] matrix
  @return double[][] a copy of the parameter matrix, manipulated to row echelon form
  */
  public static double[][] makeRowEchelon(double[][] matrix)
  {
	// set-up the temp variables used for the calculation.
	// tempMatrix 1 will be the first set of manipulations, turning the first column (rows [1] and [2]) to 0's.
	// tempMatrix 2 will be the final manipulation and will be the final row echelon form. this matrix will be returned.
	// the factors array will be the numbers multiplied by the pivot row to make the other rows 0.
	double[][] tempMatrix1 = new double[3][4];
	double[][] tempMatrix2 = new double[3][4];
	double factors[] = new double[2];
	factors[0] = matrix[1][0] / matrix[0][0];
	factors[1] = matrix[2][0] / matrix[0][0];


	for (int i=0; i<4; i++)
	{
	  tempMatrix1[0][i] = matrix[0][i];
	  tempMatrix2[0][i] = matrix[0][i];
    }

    // find value and round to 2 decimal places
    for (int i=1; i<3; i++)
    {
	  for (int j=0; j<4; j++)
	  {
		tempMatrix1[i][j] = Math.round(( matrix[i][j] - (matrix[0][j] * factors[i-1]) )*100.0) / 100.0;
	  }
    }

	System.out.println("The current matrix after making the first column 0's is:");
	System.out.println(Arrays.deepToString(tempMatrix1));

	// start manipulating tempMatrix2
	double factorLast = tempMatrix1[2][1] / tempMatrix1[1][1];
	for (int i=0; i<4; i++)
	{
	  tempMatrix2[1][i] = tempMatrix1[1][i];
	  tempMatrix2[2][i] = Math.round(( tempMatrix1[2][i] - (tempMatrix1[1][i] * factorLast) ) * 100.0) / 100.0;
    }

    // check for epsilon, a zero might be listed as an extremely small number
    double epsilon = 0.1;
    tempMatrix2[1][0] = 0;
    tempMatrix2[2][0] = 0;
    tempMatrix2[2][1]= 0;


    System.out.println("The final matrix in row echelon form is:");
    System.out.println(Arrays.deepToString(tempMatrix2));
    return tempMatrix2;
  }//end makeRowEchelon


  /**
  The makeRowEchelonPartialPivot method takes in a user-provided two-dimensional array of doubles with 3 rows
  and 4 columns. It will make a copy of the two-d array, manipulate it to row echelon form using Gaussian
  Elimination and partial pivoting, and return the manipulated two-d array.
  The algorithm used was taken from the slides 06-Linear Systems.
  tempMatrix1 is manipulated to make the first column 0's.
  then tempMatrix 1 is copied to tempMatrix 2.
  tempMatrix2 is manipulated to make the last rows second column 0. then tempMatrix2 is returned.
  @param double[][] matrix
  @return double[][] a copy of the parameter matrix, manipulated to row echelon form using partial pivoting
  */

  // returns a two dimensional matrix that is the given matrix converted to row echelon form
  // using partial pivoting
  public static double[][] makeRowEchelonPartialPivot(double[][] matrix)
  {
    System.out.println(Arrays.deepToString(matrix) + "this is the matrix that is the parameter");

    double[][] matrixManipulate = new double[3][4];
    for (int i = 0; i<3; i++)
    {
	  for (int j=0; j<4; j++)
	  {
		matrixManipulate[i][j] = matrix[i][j];
	  }
    }

	int tempMaxIndex = 0;
	double tempMax = matrix[0][0];
	for (int i = 1; i < 3; i++)
	{
	  if (Math.abs(matrix[i][0]) > Math.abs(tempMax))
	  {
		tempMax = matrix[i][0];
		tempMaxIndex = i;
	  }
    }

    if (tempMaxIndex != 0)
    {
	  matrixManipulate = swapRows(matrixManipulate, 0, tempMaxIndex);
    }

    System.out.println("This is the matrix after the rows are swapped" + Arrays.deepToString(matrixManipulate));

	double[][] tempMatrix1 = new double[3][4];
	double[][] tempMatrix2 = new double[3][4];
	double factors[] = new double[2];

	//factors[0] = Math.round((matrixManipulate[1][0] / matrixManipulate[0][0]) * 100.0) / 100.0;
	//factors[1] = Math.round((matrixManipulate[2][0] / matrixManipulate[0][0]) * 100.0) / 100.0;
	factors[0] = matrixManipulate[1][0] / matrixManipulate[0][0];
	factors[1] = matrixManipulate[2][0] / matrixManipulate[0][0];

    double epsilon = .00000000000001;
	for (int i=0; i<3; i++)
	{
	  for (int j=0; j<4; j++)
	  {
		  tempMatrix1[i][j] = matrixManipulate[i][j];
		  tempMatrix2[i][j] = matrixManipulate[i][j];
	  }
    }


    for (int i=1; i<3; i++)
    {
	  for (int j=0; j<4; j++)
	  {
		tempMatrix1[i][j] = Math.round((matrixManipulate[i][j] - (matrixManipulate[0][j] * factors[i-1])) * 100.0) / 100.0;
	  }
    }

	System.out.println("The current matrix after making the first column 0's is:");
	System.out.println(Arrays.deepToString(tempMatrix1));

	//check rows [1] and [2] to see if they need to be swapped
	if ( (Math.abs(tempMatrix1[1][1]) < Math.abs(tempMatrix1[2][1])) && (Math.abs(tempMatrix1[2][2]) > epsilon))
	{
	  tempMatrix1 = swapRows(tempMatrix1, 1, 2);
    }


	for (int i=0; i<3; i++)
	{
	  for (int j=0; j<4; j++)
	  {
		  tempMatrix2[i][j] = tempMatrix1[i][j];
	  }
    }

	// start manipulating tempMatrix2
	// make sure the last row needs to be manipulated. it might already have 0s in column[0] and column[1]
	if (tempMatrix2[1][2] != 0 && Math.abs(tempMatrix2[1][2]) > epsilon && Math.abs(tempMatrix2[2][2]) > epsilon)
	{
		double factorLast = tempMatrix1[2][1] / tempMatrix1[1][1];
		for (int i=0; i<4; i++)
		{
		  tempMatrix2[2][i] = Math.round((tempMatrix1[2][i] - (tempMatrix1[1][i] * factorLast)) * 100.0) / 100.0;
		}
    }


    tempMatrix2[1][0] = 0;
    tempMatrix2[2][0] = 0;
    tempMatrix2[2][1] = 0;
    System.out.println("The final matrix in row echelon form is:");
    System.out.println(Arrays.deepToString(tempMatrix2));
    return tempMatrix2;
  }//end makeRowEchelonPartialPivot



  /**
  The swapRows method takes in a user-provided two-dimensional array of doubles with 3 rows
  and 4 columns and two integers which are the indexes of the rows that the user would like swapped.
  It will make a copy of the two-d array and swap the two rows indicated by the parameters. It swaps by making
  a temp, then swapping it into the place of the other value.
  @param double[][] matrix, int rowA, int rowB
  @return double[][] a copy of the parameter matrix with the two rows swapped
  */
  // takes in a matrix and the two rows index that need to be swapped and returns the swapped matrix
  public static double[][] swapRows(double[][] matrix, int rowA, int rowB)
  {
	if (rowA == rowB) return matrix;

	double[][] matrixTemp = new double[3][4];

    for (int i = 0; i<3; i++)
    {
	  for (int j=0; j<4; j++)
	  {
		matrixTemp[i][j] = matrix[i][j];
	  }
    }

	double temp = 0.0;
	for (int i = 0; i<4; i++)
	{
      temp = matrixTemp[rowA][i];
      matrixTemp[rowA][i] = matrixTemp[rowB][i];
      matrixTemp[rowB][i] = temp;
    }
    return matrixTemp;
  } // end swapRows method



  /**
  The backSubstitution method takes in a user-provided two-dimensional array of doubles with 3 rows
  and 4 columns and will make a copy of the matrix to manipulate. Then it will calculate the
  values of x1, x2, and x3 and return them in a double[] array.
  @param double[][] sub_matrix
  @return double[] array of length 3 containing answers for x1, x2, and x3
  */
  public static double[] backSubstitution(double[][] sub_matrix) throws IOException
  {
	double[][] tempMatrix = new double[3][4];
    double[] answers = new double[3];
    for (int i = 0; i < 3; i++)
    {
		for (int j = 0; j<4; j++)
		{
			tempMatrix[i][j] = sub_matrix[i][j];
		}
	}
	System.out.println("The matrix we are performing back substitution on is " + Arrays.deepToString(tempMatrix));
    // keep answers to 2 decimal places
    answers[2] = Math.round((tempMatrix[2][3] / tempMatrix[2][2]) * 100.00) / 100.0;
    answers[1] = Math.round(((tempMatrix[1][3] - (answers[2]*tempMatrix[1][2])) / tempMatrix[1][1]) * 100.0) / 100.0;
    answers[0] = Math.round(((tempMatrix[0][3] - ((answers[2]*tempMatrix[0][2]) + (answers[1]*tempMatrix[0][1]))) / tempMatrix[0][0]) * 100.0) / 100.00;


    return answers;
  } //end backSubstitution method



  /**
  The findRelativeError method takes in a user-provided one-dimensional array of doubles called "solutions"
  that must contain the actual values of x1, x2, and x3 in that order and another 1-d array of doubles
  that contains the calculated values of x1, x2, and x3 in that order. Then it uses those values to
  computer the relative errors for each value calculated. Then it will return those errors in a 1-d
  array of doubles with a length of 3 (x1 error, x2 error, x3 error).
  The formula for relative error used is |actual - calculated| / |actual|
  @param double[] solutions, double[] computed
  @return double[] array of length 3 containing relative errors for x1, x2, and x3
  */
  public static double[] findRelativeError(double[] solutions, double[] computed)
  {

    double[] relativeErrors = new double[3];

    for (int i = 0; i <3 ; i++)
    {
		relativeErrors[i] = Math.round(((Math.abs(computed[i] - solutions[i]) / Math.abs(solutions[i])) * 100.0) * 100.0) / 100.0;
    }
    System.out.println("Here are the relative errors found for x1, x2, and x3 in percentages % " + Arrays.toString(relativeErrors));
    return relativeErrors;
  }// end findRelativeError method

}// end class