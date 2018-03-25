
// Bools that check if certain spots in the board have the same value
boolean Match_0_8 = false;
boolean Match_1_7 = false;
boolean Match_2_6 = false;
boolean Match_3_5 = false;

boolean Match_0_6 = false;
boolean Match_2_8 = false;

boolean Match_0_2 = false;
boolean Match_6_8 = false;

// Global function doing all the optimization
boolean[] OptimizationChecks(int[] board)
{
  // Create array of values to check for future values
  // This array is what helps designates what values the AI can choose minimally with the same results
  boolean[] CheckArray = new boolean[9];
  for (int i = 0; i < 9; i++)
  {
    CheckArray[i] = (board[i] == 0);
  }

  // If player is alone in center, corner and adjacent pieces
  // are the only pieces that the AI should attempt to check
  int p_index = CheckPlayerAlone(board, -1);

  if (p_index == 4)
  {
    CheckArray[0] = true;
    CheckArray[1] = true;
    CheckArray[2] = false;
    CheckArray[3] = false;
    CheckArray[5] = false;
    CheckArray[6] = false;
    CheckArray[7] = false;
    CheckArray[8] = false;
    return CheckArray;
  }

  // If player is in the top left or bottom right corner
  // Bottom left L can be ignored (assuming center piece is ignored)
  p_index = CheckPlayerAlone(board, 4);
  if (p_index == 0 || p_index == 8)
  {
    CheckArray[1] = false;
    CheckArray[2] = false;
    CheckArray[5] = false;
  }
  // If player is in the top right or bottom left corner
  // Bottom right L can be ignored (assuming center piece is ignored)
  else if (p_index == 2 || p_index == 6)
  {
    CheckArray[5] = false;
    CheckArray[7] = false;
    CheckArray[8] = false;
  }

  // SYMMETRY CHEKCS
  // -----------------------------

  // Set the match values
  Match_0_8 = board[0] == board[8];
  Match_1_7 = board[1] == board[7];
  Match_2_6 = board[2] == board[6];
  Match_3_5 = board[3] == board[5];

  Match_0_6 = board[0] == board[6];
  Match_2_8 = board[2] == board[8];

  Match_0_2 = board[0] == board[2];
  Match_6_8 = board[6] == board[8];

  // If column 1 == column 3 (ignore column 3)
  if (CheckEdgeColumnsMatch())
  {
    CheckArray[2] = false;
    CheckArray[5] = false;
    CheckArray[8] = false;
  }

  // If row 1 == row 3 (ignore row 3)
  if (CheckEdgeRowsMatch())
  {
    CheckArray[6] = false;
    CheckArray[7] = false;
    CheckArray[8] = false;
  }

  // If top left L == bottom right L (ignore bottom right L)
  if (CheckForwardDiagonalMatch())
  {
    CheckArray[5] = false;
    CheckArray[7] = false;
    CheckArray[8] = false;
  }

  // If top right L == bottom left L (ignore top right L)
  if (CheckBackwardDiagonalMatch())
  {
    CheckArray[1] = false;
    CheckArray[2] = false;
    CheckArray[5] = false;
  }

  // Finally return boolean array
  return CheckArray;
}


// If 1st and 3rd column are the same, ignore the 3rd column
boolean CheckEdgeColumnsMatch()
{
  return Match_0_2 && Match_3_5 && Match_6_8;
}
// If 1st and 3rd row are the same, ignore the bottom row
boolean CheckEdgeRowsMatch()
{
  return Match_0_6 && Match_1_7 && Match_2_8;
}
// If top left blocks in an L shape are the same as the bottom right ones in an L shape,
boolean CheckForwardDiagonalMatch()
{
  return Match_0_8 && Match_1_7 && Match_3_5;
}
// If top right blocks in an L shape are the same as the bottom left ones in an L shape,
boolean CheckBackwardDiagonalMatch()
{
  return Match_1_7 && Match_2_6 && Match_3_5;
}

// Check if player is alone in the board, if true, return his index;
int CheckPlayerAlone(int[] board, int ignore_index)
{
  int index = -1;
  int p = 1;
  for (int i = 0; i < 9; i++)
  {
    if (i == ignore_index)
      continue;

    if (board[i] == +1)
      return -1;
    else if (board[i] == -1)
    {
      index = i;
      p--;
      if (p < 0)
        return -1;
    }
  }
  return index;
}