# Connect4-AI


<img src="https://user-images.githubusercontent.com/28762594/32518428-5d652b2e-c3be-11e7-96a8-df8653665f97.jpg" height="559" width="627">

  My project is has been written using Java and compiled with Eclipse Neon.  The project is a simple console program.  The AI performs extremely well.  If it gets the first move it will be quite aggressive and you will have to play defensively.  If you take the first move, the AI knows when to be aggressive and knows when to be defensive.  If you make any mistake and you were given the first turn, the AI will punish you and win.
  
  Upon execution, you will be asked to input the player that will move first.  Next, you will input the number of seconds that you want the AI to spend deciding its move.  
When it is the human player’s turn, they input their move in the form ‘a1’.  It is not case sensitive, and it also chceks to see if the move is valid, in that the square lies on the 8x8 grid and it is not occupied by any player.  The program prints out the board after every turn confirmation.

  When it is the AI’s turn, it first starts off by looking for any threats.  It searches for any move that it should take no matter what.  For example, if there is a 3 in a row for either player, the AI doesn’t need to search, it needs to either block or win.  This is for obvious moves that the AI doesn’t need to look ahead for.  Of course, this cuts down on the AI’s turn time significantly.
  
  If there is no imminent threat to a loss, the AI uses a different way to calculate its move, and takes the full time allotted for its turn.  This method is performed by using a MINIMAX algorithm with Alpha Beta pruning, enclosed by an Iterative Deepening search.  This is where the turn time limit comes into play.  The AI will search through all possibilities at the starting depth of one, save the best option, and proceed to the next depth level.  States will be trimmed based on the alpha and beta values, and states will be judged with an evaluation function, described below.

  The evaluation function isn’t too extensive, but it does prescribe 6 important weights that are all taken into account when deciding the “value” of any given board.  

  1.	Square Location:  Certain squares are worth more than others, simply because spaces in the middle are more likely to have possible wins than those squares on the edges or the corners.  The breakdown for the value of the 8x8 board is seen below:
int[][] evaluationTable =
      { {3,  4,  5,  7,  7,  5,  4, 3}, 
	{4,  6,  8, 10, 10,  8,  6, 4}, 
{5,  8, 11, 13, 13, 11,  8, 5}, 	
{6, 10, 14, 16, 16, 14, 10, 6}, 			
{6, 10, 14, 16, 16, 14, 10, 6}, 				
{5,  8, 11, 13, 13, 11,  8, 5}, 				
{4,  6,  8, 10, 10,  8,  6, 4}, 				
{3,  4,  5,  7,  7,  5,  4, 3}};

  2.	2 in a line:
    a.	2 in a line with one space on one side: The lowest weight that is not a single space.  Applies a very small weight.
    b.	2 in a line with one space on BOTH sides: This is a serious threat. Large weight.  Almost as good as a 3.A.
        Note that 2 in a row that is surrounded on both sides is worth nothing!
  3.	3 in a line:
    a.	3 in a row with one space on one side: Considered pretty good. Moderate weight.
    b.	3 in a row with one space on BOTH sides: Considered to be almost as good as a win. Heavy weight.
      Note that 3 in a row that is surrounded on both sides is worth nothing!

  4.	Four in a line: A massive weight is given if a player has a winning group.  Overwrites all other weights.  


  These weights are what the AI uses to decide the worth of the board.  I kept them relatively simple, but feel that I could have gotten more into the the real strategy of the game to improve them.  Also, this game does not include diagonal wins, and also doesn’t have the “gravity” feature that the game Connect 4 has.  The makes the evaluation simpler, and doesn’t really seem to slow down the algorithm.  The algorithm appears to search up to depth 4 or even higher during the later game.  I think the ability of the AI is what speaks to the worth of my program.  I feel like the only possible way to improve this program would be to run thousands of games and take data using different weights, this would allow machine learning to calculate the weights to their ideal value, and also be able to change weight based on which player goes first.  
