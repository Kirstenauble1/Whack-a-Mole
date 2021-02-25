package common;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the board used in a Whack-A-Mole game
 * 
 * Project: project02-0610-hit-or-miss
 * File: WAMBoard.java, Created Apr 9, 2019
 * 
 * @author Giovanni Melchionne <gtm7712@rit.edu>
 * @author Kirsten Auble <kca2085@rit.edu>
 *
 */
public class WAMBoard {

	private final int rows;
	private final int cols;
	private boolean[][] board;
	private int timeLeft;
	private int score;
	private String status;

	private List<Observer<WAMBoard>> observers;


	/**
	 * Add an observer
	 * 
	 * @param observer observer to add
	 */
	public void addObserver(Observer<WAMBoard> observer) {
		observers.add(observer);
	}


	/**
	 * Alert observers
	 */
	private void alertObservers(int row, int col) {
		for (Observer<WAMBoard> obs : observers) {
			obs.update(this, row, col);
		}
	}


	/**
	 * Initializes the WAMBoard
	 * 
	 * @param rows Number of rows in the board
	 * @param cols number of cols in the board
	 */
	public WAMBoard(int rows, int cols) {
		this.rows = rows;
		this.cols = cols;

		observers = new ArrayList<>();
		this.board = new boolean[rows][cols];
		this.status = "";
	}


	/** Returns the number of rows in the board */
	public int getRows() {
		return rows;
	}


	/** Returns the number of columns in the board */
	public int getCols() {
		return cols;
	}


	/** Returns the board */
	public boolean[][] getBoard() {
		return board;
	}


	/**
	 * Gets the status of the specified position on the board
	 * 
	 * @param row The row on the board
	 * @param col The column on the board
	 * @return True if the mole is UP
	 */
	public boolean getSpot(int row, int col) {
		return board[row][col];
	}


	/**
	 * Sets the specified position on the board to be up or down
	 * 
	 * @param row The row on the board
	 * @param col The column on the board
	 * @param up  True if the mole is UP
	 */
	public void updateSpot(int row, int col, boolean up) {
		board[row][col] = up;
		alertObservers(row, col);
	}


	public void updateScore(int score) {
		this.score = score;
	}


	public void setStatus(String status) {
		this.status = status;
	}


	public String getStatus() {
		return this.status;
	}


	public int getScore() {
		return this.score;
	}


   /**
	* The only way I could find to access the time from the GUI is to
	* update time left from the server -> client -> board -> GUI
	* 
	* @param time
	*/
	public void updateTimeLeft(int time) {
		this.timeLeft = time;
	}


	public int getTimeLeft() {
		return timeLeft;
	}
}
