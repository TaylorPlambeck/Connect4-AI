import java.util.ArrayList;

public class GameState  implements Cloneable {
		char[][] currentBoard;
		boolean isAI_Turn;
		
		//constructor for AI's FIRST turn
		public GameState() {
			isAI_Turn= true;
			currentBoard=new char[8][8];
			for (int i = 0; i <8; i++) {
				for (int j = 0; j <8; j++) {
					currentBoard[i][j] =MainClass.masterBoard[i][j];
					}
				}
			}
		
		//constructor for AI's next turns
		public GameState(char[][] currentBoard, boolean isAI_Turn) {
			this.currentBoard = currentBoard;
			this.isAI_Turn = isAI_Turn;
		}

		// aiMove() returns true if it is the AI's turn to move, false otherwise
		public boolean isItTheAITurn() {
			return isAI_Turn;
		}
		
		public void makeMove(AI_Move move) {
			//if position is UNOCCUPIED, then this is a legal move
			if (currentBoard[move.getRow()][move.getColumn()] =='-') {
				if (isAI_Turn) {
					currentBoard[move.getRow()][move.getColumn()] ='X';
				} else {
					currentBoard[move.getRow()][move.getColumn()] ='O';
				}
				// SWITCH PLAYERS, THIS IS GOING TO BE SWITCHING MINIMAX algorithm
				isAI_Turn = (isAI_Turn ? false : true);
			}
		}
		
		public ArrayList<AI_Move> validMoves() {
			ArrayList<AI_Move> moves = new ArrayList<AI_Move>();
			
			for (int i = 0; i <8; i++) {
				for (int j = 0; j < 8; j++) {
					if (currentBoard[i][j] =='-') {
						AI_Move move = new AI_Move(i, j);
						moves.add(move);
					}
				}
			}
			return moves;
		}

		// Create an EXACT duplicate of the current game state
		public GameState clone() {
			char[][] newPositions = new char[8][8];
			for (int i = 0; i <8; i++) {
				for (int j = 0; j <8; j++) {
					newPositions[i][j] = currentBoard[i][j];
				}
			}
			GameState newState = new GameState(newPositions, isAI_Turn);
			return newState;
		}
}
