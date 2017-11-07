
/*  
 
Connect 4 AI with Alpha/Beta Pruning and Iterative Deepening
	Taylor Plambeck
	August 28th, 2017
	CS 420 Artificial Intelligence

 */

import java.util.Scanner;

/* This is what this Main class does:
 *  -Houses the master board, which is the the board that is saved after any player confirms their turn
 *  -manages the turns for both AI and Human player
 *  -accepts inputs for the timeLimit and first player
 *  -houses functions to print the master board, evaluate a given board, and check to see if someone has won the game
 */
public class MainClass {

	static Scanner input = new Scanner(System.in); //create a input scanner, to be used for option select AND custom table
	static long turnTimeLimit;
	static int firstPlayer;
	static boolean aiFirstTurnFlag=true;
	
	static char[][] masterBoard = new char[8][8]; // [row][col]; row,col: 0-7
	
//	static char[][] masterBoard= {
//	        {'-','-','-','-','-','-','-','-',},
//	        {'-','-','-','-','-','-','-','-',}, 
//	        {'-','-','-','-','-','-','-','-',},
//	        {'-','-','-','-','-','-','-','-',},
//	        {'-','-','-','-','-','-','-','-',},
//	        {'-','-','-','-','-','-','-','-',},
//	        {'-','-','-','-','-','-','-','-',},
//	        {'-','-','-','-','-','-','-','-',}
//	 };

	
	public static void main(String[] args) {
		//this is where the basic structure of the game is controlled		
		System.out.println("The game is Four in a Line! You will be playing as O. The AI will play as X.");
		System.out.println("\nWho goes first? Input number and press Enter");
		System.out.println("[1] User goes first");
		System.out.println("[2] AI goes first");
		firstPlayer=input.nextInt();
		System.out.println("How long is the time limit for a turn?");
		System.out.println("Input time in seconds and press Enter");
		turnTimeLimit=input.nextLong();
		
		initializeBoard(); // initialize board with this, or use the hardcoded boards above
		
		if(firstPlayer==1) {
			//if the User goes first, we play in this loop
			while(goalCheck(masterBoard)==-1) {
				printBoard(masterBoard);
				humanTurn();
				if(goalCheck(masterBoard)!=-1) {
					break; //checks to see if the humanPlayer's last turn ends the game, as AI wouldn't need the next turn below
				}
				printBoard(masterBoard);
				AiTurn(masterBoard);
			}
		}
		else if(firstPlayer==2) {
			//if the AI goes first, we play in this loop
			while(goalCheck(masterBoard)==-1) {
				printBoard(masterBoard);
				AiTurn(masterBoard);
				if(goalCheck(masterBoard)!=-1) {
					break; //checks to see if the AI's last turn ends the game, as humanPlayer wouldn't need the next turn below
				}
				printBoard(masterBoard);
				humanTurn();
			}
		}		
		printBoard(masterBoard); //prints the final winning board/Draw board
		// we come here when the goalCheck finds the game is over, either
			// 0  Game ENDS in DRAW
			// 1  AI WINS
			// 2  Human Wins
		if(goalCheck(masterBoard)==0) {
			System.out.println("\n--------------------------------------------");
			System.out.println("  *** Game Over - Game ends in a DRAW! ***  ");
			System.out.println("--------------------------------------------");
		}
		else if (goalCheck(masterBoard)==1) {
			System.out.println("\n--------------------------------");
			System.out.println("  *** Game Over - AI WINS! ***  ");
			System.out.println("--------------------------------");
		}
		else {
			System.out.println("\n--------------------------------");
			System.out.println("  *** Game Over - YOU WIN! ***  ");
			System.out.println("--------------------------------");
		}
	} // END OF MAIN   ------------------------------------------------------------------------------------------
	
	
	// this is a helper function to check if the ends of 2 and 3 ina lines are blank. we do this by manipulating the indexes,
	// this index is passed here and replaced if it would yield an OutofBoundsException
	public static int endTestEvalHelper(int testPoint) {
		if (testPoint==-1) {
			return 0;
		}
		else if (testPoint==8) {
			return 7;
		}
		else {
			return testPoint;
		}
	}
	
	//receives Board and returns an integer weight for a rating of the state in terms of AI player
	public static int eval(char[][] boardPositions) {
		// This table has values that depict the "worth" of the corresponding square on the board. The worth of a square is calculated by how
		// many possible lines in a row a player can get from the square. We do not count diagonals, and every square starts with a value of 1.
		// This simply gives a map for the AI to prefer some squares over others, simply because of their likelihood to be helpful in the long run
		// These weights are significant only when put against each other. Getting 2 in a line is much more valuable than getting a second high value square
		int[][] evaluationTable = { {3,  4,  5,  7,  7,  5,  4, 3}, //sum of all spaces in this row is 38
									{4,  6,  8, 10, 10,  8,  6, 4}, //56
									{5,  8, 11, 13, 13, 11,  8, 5}, //74
									{6, 10, 14, 16, 16, 14, 10, 6}, //92
									{6, 10, 14, 16, 16, 14, 10, 6}, //92
									{5,  8, 11, 13, 13, 11,  8, 5}, //74
									{4,  6,  8, 10, 10,  8,  6, 4}, //56
									{3,  4,  5,  7,  7,  5,  4, 3}};//38
		int sumSpaces=0;
		// Calculate the Weights of the Preferred Spaces
		for (int i = 0; i <8; i++) {
			for (int j = 0; j<8; j++) {
				if (boardPositions[i][j]=='X') {
					sumSpaces+=evaluationTable[i][j]; //if X has 'better' spots, then sum becomes more POSITIVE
				}
				else if (boardPositions[i][j]=='O') {
					sumSpaces-=evaluationTable[i][j]; //if O has 'better' spots, then sum becomes more NEGATIVE
				}
			}
		}
		// Calculate the weights for lines. This includes 2,3 and 4 squares in a line.
		// Weights from 2 or 3 lined squares will only count if they are an actual threat. If a player has a 3 in a row that is blocked on BOTH sides,
		// then that should not count towards the weight of that square. Thus, the AI only cares about threats that are PLAYABLE!
		// These weights will be added to the sumSpaces above, and returned in the final value
        
		int weightValue_WIN;
        int weightValue_threeInALine_twoSidesOpen;
        int weightValue_threeInALine_oneSideOpen;
        int weightValue_twoInALine_twoSidesOpen;
        int weightValue_twoInALine_oneSideOpen;
        weightValue_WIN=100000;
        weightValue_threeInALine_twoSidesOpen=10000;
        weightValue_threeInALine_oneSideOpen=5000;
        weightValue_twoInALine_twoSidesOpen=2500;
        weightValue_twoInALine_oneSideOpen=1000;
        
        int aiScore=0;
        int humanScore=0;
        int fourWeight=0;
        int threeWeight=0;
        int twoWeight=0;
        int finalWeight=0;
        int endTester1=0;
        int endTester2=0;
        
        for(int i=7;i>=0;--i) {
            for(int j=0;j<=7;++j) {
                if(boardPositions[i][j]=='-') 
                	continue; //skip the nested checks below because there is no player in this space. move to next iteration of j/i loops
//THIS CHECKS FOR 4 INA LINE   --------------------------------------------------------------------
                int numberInALine=4;
        //Checking cells to the right
                if(j<=4) {
                    for(int k=0;k<numberInALine;++k) { 
                            if(boardPositions[i][j+k]=='X') aiScore++;
                            else if(boardPositions[i][j+k]=='O') humanScore++;
                            else break; 
                    }
                    if(aiScore==numberInALine) fourWeight+=weightValue_WIN; //AI WINS
                    else if (humanScore==numberInALine) fourWeight-=weightValue_WIN; //HUMAN PLAYER WINS
                    aiScore = 0; //reset tally
                    humanScore = 0;
                } 
        //Checking cells upwards
                if(i>=3) {
                    for(int k=0;k<numberInALine;++k) {
                            if(boardPositions[i-k][j]=='X') aiScore++;
                            else if(boardPositions[i-k][j]=='O') humanScore++;
                            else break;
                    }
                    if(aiScore==numberInALine) fourWeight+=weightValue_WIN; //AI WINS
                    else if (humanScore==numberInALine) fourWeight-=weightValue_WIN; //HUMAN PLAYER WINS
                    aiScore = 0; //reset tally
                    humanScore = 0;
                } 
//THIS CHECKS FOR 3 INA LINE   --------------------------------------------------------------------
                numberInALine=3;
        //Checking cells to the right
                if(j<=5) {
                    for(int k=0;k<numberInALine;++k) { 
                            if(boardPositions[i][j+k]=='X') aiScore++;
                            else if(boardPositions[i][j+k]=='O') humanScore++;
                            else break; 
                    }
                    //aiScore and humanScore are 3 if the player has 3 ina line, but ONLY add weight if it is a viable threat AND not blocked on both sides
                    endTester1=endTestEvalHelper(j-1); // finds index of the squares that surround the 3 in a line. Helper fxn takes care of OutOfBounds
                    endTester2=endTestEvalHelper(j+3);
                    if ( (boardPositions[i][endTester1]=='-')  &&  (boardPositions[i][endTester2]=='-') ) {
                    	//both are EMPTY! DANK!
                    	if(aiScore==numberInALine) {
                           	threeWeight+=weightValue_threeInALine_twoSidesOpen; // having a 3 ina line AND 1 blank on each end is essentially as good as a win
                        }
                    	else if (humanScore==numberInALine) {
                           	threeWeight-=weightValue_threeInALine_twoSidesOpen; // Human player has 3 ina line, and 1 blank on each end
                        }
                    }
                    else if ( (boardPositions[i][endTester1]=='-')  |  (boardPositions[i][endTester2]=='-') ) {
                    		//Only one is empty!
                    	if(aiScore==numberInALine) {
                           	threeWeight+=weightValue_threeInALine_oneSideOpen; // having a 3 ina line AND 1 blank on ONE end is pretty good
                        }
                    	else if (humanScore==numberInALine) {
                           	threeWeight-=weightValue_threeInALine_oneSideOpen; // Human player has 3 ina line, and 1 blank on ONE end
                        }
                    }
                    aiScore = 0; //reset tally
                    humanScore = 0;
                    endTester1=0; //reset endTests
                    endTester2=0;
                } //end of checking rightwards for 3 in a Line
                
        //Checking cells upwards
                if(i>=2) {
                    for(int k=0;k<numberInALine;++k) {
                            if(boardPositions[i-k][j]=='X') aiScore++;
                            else if(boardPositions[i-k][j]=='O') humanScore++;
                            else break;
                    }
                  //aiScore and humanScore are 3 if the player has 3 ina line, but ONLY add weight if it is a viable threat AND not blocked on both sides
                    endTester1=endTestEvalHelper(i+1); // finds index of the squares that surround the 3 in a line. Helper fxn takes care of OutOfBounds
                    endTester2=endTestEvalHelper(i-3);
                    if ( (boardPositions[endTester1][j]=='-')  &&  (boardPositions[endTester2][j]=='-') ) {
                    	//both are EMPTY! DANK!
                    	if(aiScore==numberInALine) {
                           	threeWeight+=weightValue_threeInALine_twoSidesOpen; // having a 3 ina line AND 1 blank on each end is essentially as good as a win
                        }
                    	else if (humanScore==numberInALine) {
                           	threeWeight-=weightValue_threeInALine_twoSidesOpen; // Human player has 3 ina line, and 1 blank on each end
                        }
                    }
                    else if ( (boardPositions[endTester1][j]=='-')  |  (boardPositions[endTester2][j]=='-') ) {
                    		//Only one is empty!
                    	if(aiScore==numberInALine) {
                           	threeWeight+=weightValue_threeInALine_oneSideOpen; // having a 3 ina line AND 1 blank on ONE end is pretty good
                        }
                    	else if (humanScore==numberInALine) {
                           	threeWeight-=weightValue_threeInALine_oneSideOpen; // Human player has 3 ina line, and 1 blank on ONE end
                        }
                    }
                    aiScore = 0; //reset tally
                    humanScore = 0;
                    endTester1=0; //reset endTests
                    endTester2=0;
                } 
//THIS CHECKS FOR 2 INA LINE   --------------------------------------------------------------------
                numberInALine=2;
        //Checking cells to the right
                if(j<=6) {
                    for(int k=0;k<numberInALine;++k) { 
                            if(boardPositions[i][j+k]=='X') aiScore++;
                            else if(boardPositions[i][j+k]=='O') humanScore++;
                            else break; 
                    }
                  //aiScore and humanScore are 2 if the player has 2 ina line, but ONLY add weight if it is a viable threat AND not blocked on both sides
                    endTester1=endTestEvalHelper(j-1); // finds index of the squares that surround the 2 in a line. Helper fxn takes care of OutOfBounds
                    endTester2=endTestEvalHelper(j+2);
                    if ( (boardPositions[i][endTester1]=='-')  &&  (boardPositions[i][endTester2]=='-') ) {
                    	//both are EMPTY! DANK!
                    	if(aiScore==numberInALine) {
                           	threeWeight+=weightValue_twoInALine_twoSidesOpen; // having a 2 ina line AND 1 blank on each end is like a 3 single, but not quite as good
                        }
                    	else if (humanScore==numberInALine) {
                           	threeWeight-=weightValue_twoInALine_twoSidesOpen; // Human player has 2 ina line, and 1 blank on each end
                        }
                    }
                    else if ( (boardPositions[i][endTester1]=='-')  |  (boardPositions[i][endTester2]=='-') ) {
                    		//Only one is empty!
                    	if(aiScore==numberInALine) {
                           	threeWeight+=weightValue_twoInALine_oneSideOpen; // having a 2 ina line AND 1 blank on ONE end is ALRIGHT
                        }
                    	else if (humanScore==numberInALine) {
                           	threeWeight-=weightValue_twoInALine_oneSideOpen; // Human player has 2 ina line, and 1 blank on ONE end
                        }
                    }
                    aiScore = 0; //reset tally
                    humanScore = 0;
                    endTester1=0; //reset endTests
                    endTester2=0;
                } 
        //Checking cells upwards
                if(i>=1) {
                    for(int k=0;k<numberInALine;++k) {
                            if(boardPositions[i-k][j]=='X') aiScore++;
                            else if(boardPositions[i-k][j]=='O') humanScore++;
                            else break;
                    }
                    //aiScore and humanScore are 3 if the player has 2 ina line, but ONLY add weight if it is a viable threat AND not blocked on both sides
                    endTester1=endTestEvalHelper(i+1); // finds index of the squares that surround the 2 in a line. Helper fxn takes care of OutOfBounds
                    endTester2=endTestEvalHelper(i-2);
                    if ( (boardPositions[endTester1][j]=='-')  &&  (boardPositions[endTester2][j]=='-') ) {
                    	//both are EMPTY! DANK!
                    	if(aiScore==numberInALine) {
                           	threeWeight+=weightValue_twoInALine_twoSidesOpen; // having a 2 ina line AND 1 blank on each end is like a 3 single, but not quite as good
                        }
                    	else if (humanScore==numberInALine) {
                           	threeWeight-=weightValue_twoInALine_twoSidesOpen; // Human player has 2 ina line, and 1 blank on each end
                        }
                    }
                    else if ( (boardPositions[endTester1][j]=='-')  |  (boardPositions[endTester2][j]=='-') ) {
                    		//Only one is empty!
                    	if(aiScore==numberInALine) {
                           	threeWeight+=weightValue_twoInALine_oneSideOpen; // having a 2 ina line AND 1 blank on ONE end is ALRIGHT
                        }
                    	else if (humanScore==numberInALine) {
                           	threeWeight-=weightValue_twoInALine_oneSideOpen; // Human player has 2 ina line, and 1 blank on ONE end
                        }
                    }
                    aiScore = 0; //reset tally
                    humanScore = 0;
                    endTester1=0; //reset endTests
                    endTester2=0;
                }  
            } //end of j loop
        } //end of i loop
        
        finalWeight=sumSpaces+fourWeight+threeWeight+twoWeight;
        
		//if finalWeight is:
		//NEGATIVE=human has better spots and more paired spaces.
		//POSITIVE=AI has better spots and more paired spaces.
		//ZERO=Both are the exact same (board is empty, or we have a draw)
		return finalWeight; //passed finalWeight to node class, saved in the "eval" integer
	}

	//manages the turn for the AI. If it is an obvious threat, that move is made. Otherwise, the AI_Turn class is created and the search algorithm is 
	// performed using iterative deepening and alpha beta pruning
	public static void AiTurn(char[][] board) {
		System.out.println("\nAI's turn. Calculating move..."); //let user know we are calculating the move

		if( winningThreatChecker(board).getRow()!=420    &&    winningThreatChecker(board).getColumn()!=420 ) {
			// if masterBoard has O with a x2x or a x3 then we need to move there. Like, don't even bother searching
			// this is a HARDCODE. IF O has a x2x or a x3, that is considered a UNWINNABLE THREAT AND MUST BE DEALT WITH, we don't bother with the algorithm thread
			masterBoard[winningThreatChecker(board).getRow()][winningThreatChecker(board).getColumn()]='X';	
		}
		else {
			// if the return of the winningThreatChcker is NULL, then there are no imminent threats
			// then we can casually search through thousands of possibilities for the best move to win.
			AI_Turn aiPlayer= new AI_Turn(); //create TURN class for AI, the entire turn will be managed from there
			//Start AI thread	
			try {
				Thread aiThread = new Thread(aiPlayer);
				aiThread.start();
				while(true) {
					if (!aiThread.isAlive()) {
						break;
					} else {
						Thread.sleep(100);
					}
				}
			} catch (Exception ex) {
				System.out.println(ex);
			}
		} //end of AI thread
	} //end of AI's turn
	
	//receives masterBoard. if we have a game-winning threat, return the move to block it. (Called before AI's search thread, and may replace the search itself)
	public static AI_Move winningThreatChecker(char[][] board) {
        //this table is to determine which spots are more valuable when we choose to block a x2x. 
		int[][] evaluationTable = { {3,  4,  5,  7,  7,  5,  4, 3}, 
									{4,  6,  8, 10, 10,  8,  6, 4}, 
									{5,  8, 11, 13, 13, 11,  8, 5}, 
									{6, 10, 14, 16, 16, 14, 10, 6}, 
									{6, 10, 14, 16, 16, 14, 10, 6}, 
									{5,  8, 11, 13, 13, 11,  8, 5}, 
									{4,  6,  8, 10, 10,  8,  6, 4}, 
									{3,  4,  5,  7,  7,  5,  4, 3}};     
        int aiScore=0;
        int humanScore=0;
        int endTester1=0;
        int endTester2=0;
        int rowContainer =420;
        int columnContainer =420;
        int numberInALine;
        
		char[][] boardPositions=new char[8][8]; //create a copy of the masterBoard
		for (int i = 0; i <8; i++) {
			for (int j = 0; j <8; j++) {
				boardPositions[i][j]=board[i][j];
			}
		}
		// THIS is 4 different loops that check for threats
        for(int i=7;i>=0;--i) {
            for(int j=0;j<=7;++j) {
                if(boardPositions[i][j]=='-') 
                	continue; //skip the nested checks below because there is no player in this space. move to next iteration of j/i loops
                
//THIS CHECKS FOR 2 INA LINE w/ 1 blank space on BOTH sides   ---------   *** HUMAN ***   -----------------------------------------------------------
                numberInALine=2;
        //Checking cells to the right
                if(j<=6) {
                    for(int k=0;k<numberInALine;++k) { 
                            if(boardPositions[i][j+k]=='O') humanScore++;
                            	else break; 
                    }
                  //humanScore is 2 if the player has 2 ina line
                    if(humanScore==2) {
	                    endTester1=endTestEvalHelper(j-1); // finds index of the squares that surround the 2 in a line. Helper fxn takes care of OutOfBounds
	                    endTester2=endTestEvalHelper(j+2);
	                    if ( (boardPositions[i][endTester1]=='-')  &&  (boardPositions[i][endTester2]=='-') ) {
	                    	//both are EMPTY, thus we need to have the AI fill one no matter what. 
	                    	if( evaluationTable[i][endTester1] >= evaluationTable[i][endTester2] ) {
	                    		rowContainer=i;
	                    		columnContainer=endTester1;
	                    	}
	                    	else {
	                    		rowContainer=i;
	                    		columnContainer=endTester2;
	                    	}
	                    }
                    }
                    humanScore = 0;
                    endTester1=0; //reset endTests
                    endTester2=0;
                } 
        //Checking cells upwards
                if(i>=1) {
                    for(int k=0;k<numberInALine;++k) {
                            if(boardPositions[i-k][j]=='O') humanScore++;
                            	else break;
                    }
                  //humanScore is 2 if the player has 2 ina line
                    if(humanScore==2) {
	                    endTester1=endTestEvalHelper(i+1); // finds index of the squares that surround the 2 in a line. Helper fxn takes care of OutOfBounds
	                    endTester2=endTestEvalHelper(i-2);
	                    if ( (boardPositions[endTester1][j]=='-')  &&  (boardPositions[endTester2][j]=='-') ) {
	                    	//both spots around the 2 ina line are empty. thus, we need to block one for sure. the rating of which to choose is based on space worth
	                    	if( evaluationTable[endTester1][j] >= evaluationTable[endTester2][j] ) {
	                    		rowContainer=endTester1;
	                    		columnContainer=j;
	                    	}
	                    	else {
	                    		rowContainer=endTester2;
		                    	columnContainer=j;
	                    	}
	                    }
                    }
                    humanScore = 0;
                    endTester1=0; //reset endTests
                    endTester2=0;
                }
                
            } //end of HUMAN x2x j loop
        } //end of HUMAN x2x loop
        
//CHECK 2 INA LINE w/ 1 blank space on BOTH sides   ----------------------   *** AI ***   ---------------------------------------
                numberInALine=2;
                for(int i=7;i>=0;--i) {
                    for(int j=0;j<=7;++j) {
                        if(boardPositions[i][j]=='-') 
                        	continue; //skip the nested checks below because there is no player in this space. move to next iteration of j/i loops
        //Checking cells to the right
                if(j<=6) {
                    for(int k=0;k<numberInALine;++k) { 
                            if(boardPositions[i][j+k]=='X') aiScore++;
                            	else break; 
                    }
                  //humanScore is 2 if the player has 2 ina line
                    if(aiScore==2) {
	                    endTester1=endTestEvalHelper(j-1); // finds index of the squares that surround the 2 in a line. Helper fxn takes care of OutOfBounds
	                    endTester2=endTestEvalHelper(j+2);
	                    if ( (boardPositions[i][endTester1]=='-')  &&  (boardPositions[i][endTester2]=='-') ) {
	                    	//both are EMPTY, thus we need to have the AI fill one no matter what. 
	                    	if( evaluationTable[i][endTester1] >= evaluationTable[i][endTester2] ) {
	                    		rowContainer=i;
	                    		columnContainer=endTester1;
	                    	}
	                    	else {
	                    		rowContainer=i;
	                    		columnContainer=endTester2;
	                    	}
	                    }
                    }
                    aiScore = 0; //reset tally
                    endTester1=0; //reset endTests
                    endTester2=0;
                } 
        //Checking cells upwards
                if(i>=1) {
                    for(int k=0;k<numberInALine;++k) {
                            if(boardPositions[i-k][j]=='X') aiScore++;
                            	else break;
                    }
                  //humanScore is 2 if the player has 2 ina line
                    if(aiScore==2) {
	                    endTester1=endTestEvalHelper(i+1); // finds index of the squares that surround the 2 in a line. Helper fxn takes care of OutOfBounds
	                    endTester2=endTestEvalHelper(i-2);
	                    if ( (boardPositions[endTester1][j]=='-')  &&  (boardPositions[endTester2][j]=='-') ) {
	                    	//both spots around the 2 ina line are empty. thus, we need to block one for sure. the rating of which to choose is based on space worth
	                    	if( evaluationTable[endTester1][j] >= evaluationTable[endTester2][j] ) {
	                    		rowContainer=endTester1;
	                    		columnContainer=j;
	                    	}
	                    	else {
	                    		rowContainer=endTester2;
		                    	columnContainer=j;
	                    	}
	                    }
                    }
            		aiScore = 0; //reset tally
                    endTester1=0; //reset endTests
                    endTester2=0;
                }
                
            } //end of AI x2x j loop
        } //end of AI x2x i loop
//THIS CHECKS FOR 3 INA LINE w/ 1 blank space on ONE side   ----------------   *** HUMAN ***   --------------------------------------------------
                numberInALine=3;
                for(int i=7;i>=0;--i) {
                    for(int j=0;j<=7;++j) {
                        if(boardPositions[i][j]=='-') 
                        	continue; //skip the nested checks below because there is no player in this space. move to next iteration of j/i loops
        //Checking cells to the right
                if(j<=5) {
                    for(int k=0;k<numberInALine;++k) { 
                    	if(boardPositions[i][j+k]=='O') humanScore++;
                        	else break; 
                    }
                  //humanScore is 3 if the player has 2 ina line
                    if(humanScore==3) {
	                    endTester1=endTestEvalHelper(j-1); // finds index of the squares that surround the 3 in a line. Helper fxn takes care of OutOfBounds
	                    endTester2=endTestEvalHelper(j+3);
	                    if ( (boardPositions[i][endTester1]=='-') ) {
	                    	rowContainer=i;
	                    	columnContainer=endTester1;
	                    }
	                    else if ( (boardPositions[i][endTester2]=='-') ) {
	                    	rowContainer=i;
	                    	columnContainer=endTester2;
	                    }
                    }
                    humanScore = 0;
                    endTester1=0; //reset endTests
                    endTester2=0;
                } //end of checking rightwards for 3 in a Line
                
        //Checking cells upwards
                if(i>=2) {
                    for(int k=0;k<numberInALine;++k) {
                            if(boardPositions[i-k][j]=='O') humanScore++;
                            	else break;
                    }
                  //humanScore is 3 if the player has 3 ina line  
                    if(humanScore==3) {
	                    endTester1=endTestEvalHelper(i+1); // finds index of the squares that surround the 3 in a line. Helper fxn takes care of OutOfBounds
	                    endTester2=endTestEvalHelper(i-3);
	                    if ( (boardPositions[endTester1][j]=='-') ) {
	                    	rowContainer=endTester1;
	                    	columnContainer=j;
	                    }
	                    else if ( (boardPositions[endTester2][j]=='-') ) {
	                    	rowContainer=endTester2;
	                    	columnContainer=j;
	                    }
                    }
                    humanScore = 0;
                    endTester1=0; //reset endTests
                    endTester2=0;
                } 
                
            } //end of HUMAN x3 j loop
        } //end of HUMAN x3 i loop
        
//THIS CHECKS FOR 3 INA LINE w/ 1 blank space on ONE side   --------   *** AI ***   -----------------------------------------------------------
                numberInALine=3;
                for(int i=7;i>=0;--i) {
                    for(int j=0;j<=7;++j) {
                        if(boardPositions[i][j]=='-') 
                        	continue; //skip the nested checks below because there is no player in this space. move to next iteration of j/i loops
        //Checking cells to the right
                if(j<=5) {
                    for(int k=0;k<numberInALine;++k) { 
                    	if(boardPositions[i][j+k]=='X') aiScore++;
                        	else break; 
                    }
                  //humanScore is 3 if the player has 2 ina line
                    if(aiScore==3) {
	                    endTester1=endTestEvalHelper(j-1); // finds index of the squares that surround the 3 in a line. Helper fxn takes care of OutOfBounds
	                    endTester2=endTestEvalHelper(j+3);
	                    if ( (boardPositions[i][endTester1]=='-') ) {
	                    	rowContainer=i;
	                    	columnContainer=endTester1;
	                    }
	                    else if ( (boardPositions[i][endTester2]=='-') ) {
	                    	rowContainer=i;
	                    	columnContainer=endTester2;
	                    }
                    }
            		aiScore = 0; //reset tally
                    endTester1=0; //reset endTests
                    endTester2=0;
                } //end of checking rightwards for 3 in a Line
                
        //Checking cells upwards
                if(i>=2) {
                    for(int k=0;k<numberInALine;++k) {
                            if(boardPositions[i-k][j]=='X') aiScore++;
                            	else break;
                    }
                  //humanScore is 3 if the player has 3 ina line  
                    if(aiScore==3) {
	                    endTester1=endTestEvalHelper(i+1); // finds index of the squares that surround the 3 in a line. Helper fxn takes care of OutOfBounds
	                    endTester2=endTestEvalHelper(i-3);
	                    if ( (boardPositions[endTester1][j]=='-') ) {
	                    	rowContainer=endTester1;
	                    	columnContainer=j;
	                    }
	                    else if ( (boardPositions[endTester2][j]=='-') ) {
	                    	rowContainer=endTester2;
	                    	columnContainer=j;
	                    }
                    }
            		aiScore = 0; //reset tally
                    endTester1=0; //reset endTests
                    endTester2=0;
                } 
                
            } //end of AI x3 j loop
        } //end of AI x3 i loop
        AI_Move threatBlock_Move=new AI_Move(rowContainer,columnContainer);
		return threatBlock_Move;
	}
	
	
	
		
	public static void humanTurn() {
		String playerMove;
		System.out.println("\nYour turn. Input your move in the form 'a1'.");
		playerMove=input.next();
		char usersMoveRowCHAR=Character.toLowerCase(playerMove.charAt(0)); //turn first char of input string lowercase, save in variable usersMoveRowCHAR
		int  usersMoveRowINT =0; //this will be used below to save the actual row integer that the letter corresponds to
		int usersMoveCol=(Character.getNumericValue(playerMove.charAt(1))-1); //save the second char in the string to the Column #, no need to convert

		//switch statement converts the CHAR input to the actual row integer
		switch (usersMoveRowCHAR) {
			case 'a': {
				usersMoveRowINT=0;
				break;
			}
			case 'b': {
				usersMoveRowINT=1;		
				break;
			}
			case 'c': {
				usersMoveRowINT=2;		
				break;
			}
			case 'd': {
				usersMoveRowINT=3;	
				break;
			}
			case 'e': {
				usersMoveRowINT=4;	
				break;
			}
			case 'f': {
				usersMoveRowINT=5;	
				break;
			}
			case 'g': {
				usersMoveRowINT=6;	
				break;
			}
			case 'h': {
				usersMoveRowINT=7;	
				break;
			}
			default:
				usersMoveRowINT=420;
				break;
		} //end switch
		
		if(usersMoveRowINT==420 | (usersMoveCol!=0 && usersMoveCol!=1 && usersMoveCol!=2 && usersMoveCol!=3 && usersMoveCol!=4 && usersMoveCol!=5 
				&& usersMoveCol!=6 && usersMoveCol!=7)) {
			//if we have an invalid move, then 
			System.out.println("\nInvalid input. Try a different move.");
			humanTurn(); //reset turn
		}
		else {
			//valid input
			if(masterBoard[usersMoveRowINT][(usersMoveCol)]=='-') {
				masterBoard[usersMoveRowINT][(usersMoveCol)]='O'; //fill the Master board with the User's move
			}
			else {
				System.out.println("\nInvalid move. Spot is occupied by a player.");
				humanTurn(); //reset turn
			}
		}
	} //end of humanTurn fxn
	
	
	// goalCheck recieves a board and Returns
	//-1  Game Unfinished
	// 0  Game ENDS in DRAW
	// 1  AI WINS
	// 2  Human Wins
	public static int goalCheck(char[][] board) {
        int aiScore=0;
        int humanScore=0;
        for(int i=7;i>=0;--i) {
            for(int j=0;j<=7;++j) {
                if(board[i][j]=='-') 
                	continue; //discard checks and move to next iteration                
// THIS CHECKS FOR 4 INA LINE
                //Checking cells to the right
                if(j<=4) {
                    for(int k=0;k<4;++k) { 
                            if(board[i][j+k]=='X') aiScore++;
                            else if(board[i][j+k]=='O') humanScore++;
                            else break; 
                    }
                    if(aiScore==4) return 1; //AI WINS
                    else if (humanScore==4) return 2; //HUMAN PLAYER WINS
                    aiScore = 0; //reset tally
                    humanScore = 0;
                } 
                //Checking cells upwards
                if(i>=3) {
                    for(int k=0;k<4;++k) {
                            if(board[i-k][j]=='X') aiScore++;
                            else if(board[i-k][j]=='O') humanScore++;
                            else break;
                    }
                    if(aiScore==4) return 1; //AI WINS
                    else if (humanScore==4) return 2; //HUMAN PLAYER WINS
                    aiScore = 0; //reset tally
                    humanScore = 0;
                } 
            } //end of j loop
        } //end of i loop
        
        for(int y=0;y<7;++y) {
        	for(int x=0;x<7;++x) {
                if(board[y][x]=='-') 
                	return -1;  //Game has NOT ended yet
            }
        }
        return 0;//Game ends in DRAW
    } //end goalCheck
	
	

	public static void initializeBoard() {
		for(int j=0;j<8;j++) {
			for(int k=0;k<8;k++) {
				masterBoard[j][k]='-'; //set Master board with dashes
			}
		}
	} //end of initialize board
	
	
	
	public static void printBoard(char[][] board) {
		int row=0; 
		System.out.println("\n  1 2 3 4 5 6 7 8"); //print out top row, print out the contents of the rest of the board
		System.out.println("A "+board[row][0]+" "+board[row][1]+" "+board[row][2]+" "+board[row][3]+" "+board[row][4]+" "+board[row][5]+" "+board[row][6]+" "+board[row++][7]);	
		System.out.println("B "+board[row][0]+" "+board[row][1]+" "+board[row][2]+" "+board[row][3]+" "+board[row][4]+" "+board[row][5]+" "+board[row][6]+" "+board[row++][7]);	
		System.out.println("C "+board[row][0]+" "+board[row][1]+" "+board[row][2]+" "+board[row][3]+" "+board[row][4]+" "+board[row][5]+" "+board[row][6]+" "+board[row++][7]);	
		System.out.println("D "+board[row][0]+" "+board[row][1]+" "+board[row][2]+" "+board[row][3]+" "+board[row][4]+" "+board[row][5]+" "+board[row][6]+" "+board[row++][7]);	
		System.out.println("E "+board[row][0]+" "+board[row][1]+" "+board[row][2]+" "+board[row][3]+" "+board[row][4]+" "+board[row][5]+" "+board[row][6]+" "+board[row++][7]);	
		System.out.println("F "+board[row][0]+" "+board[row][1]+" "+board[row][2]+" "+board[row][3]+" "+board[row][4]+" "+board[row][5]+" "+board[row][6]+" "+board[row++][7]);	
		System.out.println("G "+board[row][0]+" "+board[row][1]+" "+board[row][2]+" "+board[row][3]+" "+board[row][4]+" "+board[row][5]+" "+board[row][6]+" "+board[row++][7]);	
		System.out.println("H "+board[row][0]+" "+board[row][1]+" "+board[row][2]+" "+board[row][3]+" "+board[row][4]+" "+board[row][5]+" "+board[row][6]+" "+board[row++][7]);	
		//System.out.println("\n THE EVAL OF THE BOARD ABOVE IS: "+eval(board)); //print out top row, print out the contents of the rest of the board
	} //end of print board
	
	
} //end of main class





