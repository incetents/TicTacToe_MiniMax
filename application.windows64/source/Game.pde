
GameState state = GameState.NORMAL;

boolean MonkeyMode = false;

class Game
{
  private AI _ai = new AI();
  public boolean isPlayerTurn = true;

  // returns index of box that mouse is in
  int checkMouseBox()
  {
    int result = ((mouseX*3) / width) + ((mouseY*3) / height) * 3;
    // Clamp mouse values
    return max(min(result, 8), 0);
  }

  // Monkey Moves possible (basically attempts to play every single possible move in tic tac toe)
  int[] _MonkeyMoves = new int[] {0, 4, 5, 0, 1};
  int   _TotalMonkeyMoves = 5;
  int   _MonkeyTurnIndex = 0;
  void NextMonkeyMove()
  {
    _MonkeyTurnIndex++;
  }
  void NextMonkeyState()
  {
    _MonkeyMoves[0]++;
    for (int i = 0; i < _TotalMonkeyMoves - 1; i++)
    {
      if (_MonkeyMoves[i] >= 9)
      {
        _MonkeyMoves[i] = 0;
        _MonkeyMoves[i+1]++;
      }
    }
    if (_MonkeyMoves[_TotalMonkeyMoves - 1] == 9)
    {
      _MonkeyMoves[_TotalMonkeyMoves - 1] = 0;
      MonkeyMode = false;
      println("MONKEY MODE COMPLETE");
    }
  }
  boolean CheckMonkeyStateHasDuplicateMoves()
  {
    for (int a = 0; a < 5; a++)
    {
      for (int b = a + 1; b < 5; b++)
      {
        if (_MonkeyMoves[a] == _MonkeyMoves[b])
          return true;
      }
    }
    return false;
  }
  int GetCurrentMonkeyMove()
  {
    int offset = 0;
    while (gameBoard[(_MonkeyMoves[_MonkeyTurnIndex] + offset) % 9] != 0)
    {
      offset++;
    }
    return (_MonkeyMoves[_MonkeyTurnIndex] + offset) % 9;
  }
  void OutputMonkeyState()
  {
    print("MonkeyMode State: ");
    for (int i = 0; i < game._TotalMonkeyMoves; i++)
    {
      print(game._MonkeyMoves[i] + " ");
    }
    println();
  }

  // Simulate Player Turn (used for lots of quick input
  void monkeyMove()
  {
    OutputMonkeyState();

    isPlayerTurn = false;

    // Play Monkey Value
    gameBoard[GetCurrentMonkeyMove()] = -1;
    // Increase Monkey Meter
    NextMonkeyMove();

    updateWinner();
  }

  // Player Turn
  void playerMove()
  {
    // If you click in a box, it adds an X there
    if (mousePressed && gameBoard[checkMouseBox()] == 0)
    {
      if (!FlipGraphics)
        AddSpray(new PVector(mouseX, mouseY), 1, 0, 0); // EFFECT
      else
        AddSpray(new PVector(mouseX, mouseY), 0, 0, 1); // EFFECT

      isPlayerTurn = false;
      gameBoard[checkMouseBox()] = -1;

      updateWinner();
    }
  }
  // AI Turn
  void enemyMove()
  {
    int index = _ai.playMove();
    float x = (index % 3) * width/3.0f + width/6.0f;
    float y = ((float)index / 3.0f) * height/3.0f + height/6.0f;

    if (!FlipGraphics)
      AddSpray(new PVector(x, y), 0, 0, 1); // EFFECT
    else
      AddSpray(new PVector(x, y), 1, 0, 0); // EFFECT

    isPlayerTurn = true;

    updateWinner();
  }

  // Resets all necessary dataa for the game
  void reset()
  {
    state = GameState.NORMAL;
    for (int i = 0; i < 9; i++)
    {
      gameBoard[i] = 0;
    }
    isPlayerTurn = true;
    resetGraphicsGrow();
    ClearAllSprays();
    _MonkeyTurnIndex = 0;
    if (MonkeyMode)
    {
      NextMonkeyState();

      // Keep increasing monkey meter if two array values are duplicates
      while (CheckMonkeyStateHasDuplicateMoves())
      {
        println("Duplicate Skip");
        NextMonkeyState();
      }
    }
  }

  // Check if the game state has changed based on the board,
  // if so, then change the state variable
  void updateWinner()
  {
    state = CheckWinner(gameBoard);
  }
}