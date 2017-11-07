
public class AI_Move {
		private int row;
		private int column;
		
		// Constructor
		public AI_Move(int row, int column) {
			this.row = row;
			this.column = column;
		}
		// return the move's column
		public int getColumn() {
			return column;
		}
		// return the move's row
		public int getRow() {
			return row;
		}
		
		// set the move's column
		public void setColumn(int c) {
			column=c;
		}
		// set the move's row
		public void setRow(int r) {
			row=r;
		}
				

		// compares two moves, return true if equivalent, else false 
		public boolean equals(AI_Move other) {
			if ((other.getRow() == row) && (other.getColumn() == column)) {
				return true;
			} 
			else {
				return false;
			}
		}
}
