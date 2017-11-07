	
import java.util.ArrayList;
	
public class AI_Turn implements Runnable  {
	
	private static final long winCutoff = 1000000000;
	private static boolean searchCutoff = false;
	
	public void run() {
		GameState currentState = new GameState(); //this is for the thread that is ran from MAIN, creates a new state from the MASTER BOARD in Main
		AI_Move move = chooseMove(currentState);	//chooseMove from our currentstate 
		MainClass.masterBoard[move.getRow()][move.getColumn()]=('X');	//if the move is legit and the optimal, then replace the MASTER with an 'X'
	}
	
	//checks every valid move, returns the best move
	private AI_Move chooseMove(GameState currentState) {
		int maxScore = Integer.MIN_VALUE;
		AI_Move bestMove = null;
		
		ArrayList<AI_Move> validMoveList= currentState.validMoves();
		for (AI_Move move : validMoveList) {
			GameState newState = currentState.clone(); //for every move in the list of available moves, copy it and test it
			newState.makeMove(move);

			// ITERATIVE DEEPENING STARTS HERE ------------------
			long singleMoveTimeLimit=(((MainClass.turnTimeLimit)*1000) / (validMoveList.size())); //how long to spend looking into one move
			int rating=iterativeDeepeningSearch(newState, singleMoveTimeLimit); //put the newstate and the time limit into our iterative deepening search below

			if (rating >= winCutoff) { //cutoff if there is a win
				return move;
			}
			if (rating > maxScore) { 
				maxScore = rating;
				bestMove = move;
			}
		}
		return bestMove;
	}
	
	private int iterativeDeepeningSearch(GameState currentState, long timeLimit) {
		long startTime = System.currentTimeMillis();
		long endTime = startTime + timeLimit;
		int depth = 1;
		int rating=0;
		searchCutoff = false;
		
		while (true) { 
			long currentTime = System.currentTimeMillis();
			if (currentTime >= endTime) {
				break;
			}
			// ALPHA BETA PRUNING CALLED HERE  --------------
			int searchResult = alphaBetaPruning(currentState, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, currentTime, endTime - currentTime);
			
			if (searchResult >= winCutoff) { //cutoff if there is a win
				return searchResult;
			}
			if (!searchCutoff) {
				rating = searchResult;
			}
			depth++; //increase the depth of the next search
		}
		return rating;
	}
	

	// perform MINIMAX with alpha-beta pruning on the test game state, and cut off if we take too long
	private int alphaBetaPruning(GameState currentState, int depth, int alpha, int beta, long startTime, long timeLimit) {
		ArrayList<AI_Move> validMoveList = currentState.validMoves();
		boolean myMove = currentState.isItTheAITurn();
		int evalRating=MainClass.eval(currentState.currentBoard);
		//MainClass.printBoard(currentState.currentBoard);
		//System.out.print(evalRating+",");
		long currentTime = System.currentTimeMillis();
		long elapsedTime = (currentTime - startTime);
		if (elapsedTime >= timeLimit) {
			searchCutoff = true; //ran out of time, go up to iterative deepening
		}
		
		if (searchCutoff || (depth == 0) || (validMoveList.size() == 0) || (evalRating >= winCutoff) || (evalRating <= -winCutoff)) {
			return evalRating; //if we run out of time, have a terminal node, run out of moves, or either player wins, then return
		}
		
		if (currentState.isAI_Turn) {
			for (AI_Move move : validMoveList) {
				GameState childState = currentState.clone();
				childState.makeMove(move);
				// ----  MAX ----
				alpha = Math.max(alpha, alphaBetaPruning(childState, depth - 1, alpha, beta, startTime, timeLimit));
				if (beta <= alpha) {
					break;
				}
			}
			return alpha;
		} 
		else {
			for (AI_Move move : validMoveList) {
				GameState childState = currentState.clone();
				childState.makeMove(move);
				// ----  MIN ----
				beta = Math.min(beta, alphaBetaPruning(childState, depth - 1, alpha, beta, startTime, timeLimit));
				if (beta <= alpha) {
					break;
				}
			}
			return beta;
		}
	}
}
