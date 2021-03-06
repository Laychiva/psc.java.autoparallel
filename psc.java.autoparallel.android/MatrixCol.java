/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2012-2013  Members of the project group APT
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package psc.java.autoparallel.android;

import java.util.ArrayList;
import java.util.List;


/**
 * A Matrix specified for the invariant module, stored by COLUMN so that deletColumn has good complexity.
 * @author Manuel Gieseking, Dennis-Michael Borde, Yann Thierry-Mieg
 */
public class MatrixCol {

	/**
	 * Returns the identity matrix with given col and row count.
	 * @param rows - the row count of the identity matrix.
	 * @param cols - the col count of the identity matrix.
	 * @return the identity matrix with the given col and row count.
	 */
	public static MatrixCol identity(int rows, int cols) {
		MatrixCol result = new MatrixCol(rows, cols);
		for (int i = 0; i < rows && i < cols; ++i) {
			result.set(i,i, 1);			
		}
		return result;
	}
	public static MatrixCol sumProd (int alpha, MatrixCol ta, int beta, MatrixCol tb) {
		if (ta.getColumnCount() != tb.getColumnCount() || ta.getRowCount() != tb.getRowCount()) {
			throw new IllegalArgumentException("Matrices should be homogeneous dimensions for sum-product operation.");
		}
		MatrixCol mat = new MatrixCol(ta.getRowCount(), ta.getColumnCount());
		for (int col=0,cole=ta.getColumnCount(); col < cole ; col++) {
			mat.setColumn(col, SparseIntArray.sumProd(alpha, ta.getColumn(col), beta, tb.getColumn(col)));
		}
		return mat;
	}
	private int iCols;

	private int iRows;

	private final List<SparseIntArray> lCols;

	/**
	 * Constructor for a new Matrix with the given col and row count.
	 * @param rows - the row count of the resulting matrix.
	 * @param cols - the col count of the resulting matrix.
	 */
	public MatrixCol(int rows, int cols) {
		this.iRows = rows;
		this.iCols = cols;
		this.lCols = new ArrayList<>(this.iCols);

		for (int col = 0 ; col < iCols ; col++) {
			SparseIntArray toadd = new SparseIntArray();
			lCols.add(toadd);
		}
	}
	
	/**
	 * Constructor for a new Matrix with the values from the given array.
	 * @param src - the template to create the matrix from.
	 */
	public MatrixCol(int[][] src) {
		this.iRows = src.length;
		this.iCols = src[0].length;
		this.lCols = new ArrayList<>(this.iCols);

		for (int col = 0; col < this.iCols; ++col) {
			final SparseIntArray toadd = new SparseIntArray();
			for (int row = 0; row < this.iRows; ++row) {
				int val = src[row][col];
				if (val != 0) {
					toadd.put(row,val);
				}
			}
			lCols.add(toadd);
		}
	}

	/**
	 * build a copy of a MatrixCol
	 * @param ori
	 */
	public MatrixCol (MatrixCol ori) {
		iRows = ori.iRows;
		iCols = ori.iCols;
		lCols = new ArrayList<>(iCols);
		for (SparseIntArray a : ori.lCols) {
			lCols.add(a.clone());
		}
	}
	
	
	/**
	 * Add a new empty row.
	 */
	public void addRow() {
		iRows ++;
	}
	
	/**
	 * Appends a given column to this matrix. That means adding the given column from the right side to this matrix.
	 * @param column - the column to append.
	 */
	public void appendColumn(SparseIntArray column) {
		assert column.size()==0 || iRows > column.keyAt(column.size()-1);
		lCols.add(column);
		this.iCols++;
	}

	public void clear(int rowCount, int colCount) {
		int missing = colCount - lCols.size();
		if (missing > 0) {
			for (int i = 0; i < missing; i++) {
				lCols.add(new SparseIntArray());
			}
		} else {
			for (int i = 0; i < missing; i++) {
				lCols.remove(lCols.size()-1);
			}
		}
		iCols = colCount;
		iRows = rowCount;
		for (SparseIntArray s : lCols) {
			s.clear();
		}
	}

//	/**
//	 * Returns the row with the given index in this matrix. Changes to this row will have affect to this matrix.
//	 * @param i - the index of the wished row of this matrix.
//	 * @return the row with the given index in this matrix.
//	 */
//	public List<Integer> getRow(int i) {
//	List<Integer> result = new ArrayList<>(iRows);
//	for (List<Integer> row : lCols) {
//		result.add(row.get(i));
//	}
//	return result;
////		return this.lCols.get(i);
//	}

	/**
	 * Deletes the column with the given index from this matrix.
	 * @param j - the index of the column which should be deleted.
	 */
	public void deleteColumn(int j) {
		lCols.remove(j);
		this.iCols -= 1;
	}
	public void deleteRow(int row) {
		for (SparseIntArray col : getColumns()) {
			col.deleteAndShift(row);
		}
		iRows -= 1;
	}
	

	public void deleteRows(final List<Integer> todel) {
		if (getColumnCount() >= 1000)
			getColumns().parallelStream().unordered().forEach(col -> col.deleteAndShift(todel));
		else
			getColumns().stream().unordered().forEach(col -> col.deleteAndShift(todel));
			
		iRows -= todel.size();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MatrixCol other = (MatrixCol) obj;
		if (iCols != other.iCols)
			return false;
		if (iRows != other.iRows)
			return false;
		if (lCols == null) {
			if (other.lCols != null)
				return false;
		} else if (!lCols.equals(other.lCols))
			return false;
		return true;
	}
	
	/** Exchange back to explicit form if required.
	 * @return an explicit verion of this matrix, mat[i] giving a column at index i.
	 */
	public int [][] explicit () {
		int  [][] mat = new int[iRows][iCols];
		for (int col = 0; col < this.iCols; ++col) {
			SparseIntArray arr = lCols.get(col);
			for (int i = 0 ; i < arr.size() ; i++) {				
				int row = arr.keyAt(i);
				int val = arr.valueAt(i);
				mat [row][col] =  val;
			}
		}
		return mat;
	}



	public int get (int row, int col) {
		return lCols.get(col).get(row,0);
	}

	/**
	 * Returns the column with the given index of this matrix.
	 * @param i - the index of the wished column of this matrix.
	 * @return a copy of the column with the given index of this matrix.
	 */
	public SparseIntArray getColumn(int i) {
		return lCols.get(i);
	}

	/**
	 * Returns the count of cols of this matrix.
	 * @return the count of cols of this matrix.
	 */
	public int getColumnCount() {
		return this.iCols;
	}
	
	/**
	 * Use with care !! Basically, just consider this is readonly.
	 * @return our very own storage !
	 */
	public List<SparseIntArray> getColumns () {
		return lCols;
	}

	/**
	 * Returns a row which has at least one component different from zero. It also returns the index of the column
	 * where a component not equal to zero was found. If such a row does not exists, then null.
	 * @return the index of the column with a none zero component and the addicted row or null if not existent.
	 */
	public int[] getNoneZeroRow() {
		// optimize to prefer to return 1
		for (int tcol = 0; tcol < getColumnCount(); tcol++) {
			if (lCols.get(tcol).size()==0) {
				continue;
			} else {
				return new int [] {lCols.get(tcol).keyAt(0) , tcol};
			}
		}
		return null;
	}
	/**
	 * Returns the count of rows of this matrix.
	 * @return the count of rows of this matrix.
	 */
	public int getRowCount() {
		return this.iRows;
	}
	
	
	/**
	 * Checks if this matrix only contains of components equal to zero.
	 * @return true if this matrix has just components equal to zero.
	 */
	public boolean isZero() {
		for (SparseIntArray row : this.lCols) {
			if (row.size() != 0) {
				return false;
			}
		}
		return true;
	}

	public void set (int row, int col, int val) {
//		System.out.println("row : " + row +" , col : " + col + " iRows : " + iRows + ", iCols : " + iCols ) ;
		if (row < 0 || col < 0 || row >= iRows || col >= iCols)
			throw new IllegalArgumentException();
		if (val != 0) {
			SparseIntArray column = lCols.get(col);
			if (column.size()== 0 || column.keyAt(column.size()-1) < row) {
				column.append(row, val);
			} else {
				column.put(row,val);
			}
		} else {
			lCols.get(col).delete(row);
		}
	}

	public SparseIntArray setColumn(int i,SparseIntArray v) {
		return lCols.set(i,v);
	}

	@Override
	public String toString() {
		return "Matrix{" + "lCols=" + lCols + '}';
	}

	/**
	 * Transpose the Matrix in a new copy.
	 */
	public MatrixCol transpose() {
		MatrixCol tr = new MatrixCol(iCols, iRows);
		transposeTo(tr,false);
		return tr;
	}
	
	public void transposeTo(MatrixCol tr) {
		transposeTo(tr, true);
	}
	
	public void transposeTo(MatrixCol tr, boolean clear) {
		if (clear)
			tr.clear(getColumnCount(),getRowCount());
		for (int tcol = 0; tcol < iCols; tcol++) {
			SparseIntArray col = lCols.get(tcol);
			for (int k =0 ; k < col.size() ; k++) {
				int trow = col.keyAt(k);
				int val = col.valueAt(k);
				tr.set(tcol, trow, val);
			}
		}		
	}
	
}