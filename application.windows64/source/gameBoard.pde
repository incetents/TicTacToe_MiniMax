
// +1 = AI, -1 = Player, 0 = empty
int gameBoard[] = new int[9];

enum GameState
{
  NORMAL, 
    PLAYERWIN, 
    AIWIN, 
    DRAW
}

// Given 3 board indices (assuming they are already in a row/column/diagonal)
// check if they are all 0's or X's
GameState CheckLineWinner(int board[], int index1, int index2, int index3)
{
  int total = board[index1] + board[index2] + board[index3];

  if (total == 3)
    return GameState.AIWIN;
  else if (total == -3)
    return GameState.PLAYERWIN;

  return GameState.NORMAL;
}

boolean CheckTie(int board[])
{
  for (int i = 0; i < 9; i++)
  {
    if (board[i] == 0)
      return false;
  }
  return true;
}

GameState CheckWinner(int board[])
{
  // Result Temp
  GameState result;

  // Check horizontal lines
  for (int i = 0; i < 3; i++)
  {
    result = CheckLineWinner(board, 0 + i*3, 1 + i*3, 2 + i*3);
    if (result != GameState.NORMAL)
      return result;
  }
  // check vertical lines
  for (int i = 0; i < 3; i++)
  {
    result = CheckLineWinner(board, i, i+3, i+6);
    if (result != GameState.NORMAL)
      return result;
  }
  // check diagonal
  result = CheckLineWinner(board, 0, 4, 8);
  if (result != GameState.NORMAL)
    return result;

  result = CheckLineWinner(board, 2, 4, 6);
  if (result != GameState.NORMAL)
    return result;

  // Tie Check
  if (CheckTie(board))
    return GameState.DRAW;

  // Game is still normal
  return GameState.NORMAL;
}

// Convert data of board into a string
// This is useful for the memory class as it needs to use this result for the hashmap
String BoardToString(int board[])
{
  String result = "";
  for (int i = 0; i < 9; i++)
  {
    if (board[i] == 0)
      result = result + "0";
      
    else if (board[i] == -1)
      result = result + "1";
      
    else if (board[i] == +1)
      result = result + "2";
  }
  return result;
}

// Check if current board is Empty
boolean CheckEmptyBoard()
{
  int empty = 0;
  for (int i = 0; i < 9; i++)
  {
    if (gameBoard[i] == 0)
      empty++;
  }
  return empty == 9;
}