
enum AIstate
{
  RANDOM, 
    PERFECT
}

class AI
{
  private AIstate _state = AIstate.PERFECT;

  // This function is what sets in motion the AI to pick its next move
  public int playMove()
  {
    // Randomness for testing reasons mainly
    if (_state == AIstate.RANDOM)
    {
      int rand = round(random(8.0f));
      while (gameBoard[rand] != 0)
      {
        rand = round(random(8.0f));
      }
      gameBoard[rand] = +1;
      return rand;
    }
    // Discover best possible move for AI to make
    else
    {
      // Check if memory already contains the answer 
      int memory_index;
      if (USE_MEMORY)
        memory_index = memory.Get(BoardToString(gameBoard));
      else
        memory_index = -1;


      if (memory_index != -1)
      {
        if(DEBUG_PRINT_VALUES)
          println("Played Move From Memory");

        // Set result
        gameBoard[memory_index] = +1;
        return memory_index;
      }
      // If answer not already stored, figured it out using zero sum logic

      else
      {
        // Create node based on the current board state
        Node _current = new Node(GOAL.MAX, gameBoard, 0);

        // Find best possible move
        int index = _current.FindWinningChild()._boardIndexChanged;

        // ERROR CHECK
        if (index == -1)
          return -1;

        if(DEBUG_PRINT_VALUES)
          _current.OutputChildrenScores();

        // Add Result to memory (assuming it wasn't an error)
        memory.Add(BoardToString(gameBoard), index);

        // Set result
        gameBoard[index] = +1;
        return index;
      }
    }
  }
}

enum GOAL
{
  MIN, 
    MAX
}
GOAL invert(GOAL G)
{
  if (G == GOAL.MIN)
    return GOAL.MAX;

  return GOAL.MIN;
}

class Node
{
  private GOAL _goal;
  private int  _node_gameBoard[] = new int[9];
  public  int  _score;
  public  int  _boardIndexChanged = -1;
  public  int  _depth = 0;

  public ArrayList<Node> _children = new ArrayList<Node>();

  public int GetScore()
  {
    return _score;
  }

  // As soon as the node is created, it will attempt to figure out its score
  Node(GOAL g, int temp_gameBoard[], int depth)
  {
    _goal = g; 
    _depth = depth;
    arrayCopy(temp_gameBoard, _node_gameBoard);

    // Check State of current board:
    // if game is over then score is known
    GameState result = CheckWinner(temp_gameBoard);
    switch(result)
    {
    case AIWIN:
      _score = +10 - depth;
      break;

    case PLAYERWIN:
      _score = -10 + depth;
      break;

    case DRAW:
      _score = 0;
      break;

    case NORMAL:
      // Game is not over, node will create children for all possible moves
      // and attempt to find a score that satisfies the nodes goal
      findChildren();
      _score = FindWinningChild()._score;
      //
      break;
    }
  }

  // Children nodes are just nodes that contain a possible move for the AI to make
  void findChildren()
  {
    // This function checks the current state of the board and
    // sets certain indices to false that don't need to be checked
    // in order to optimize this process
    boolean[] CheckArray = OptimizationChecks(_node_gameBoard);

    for (int i = 0; i < 9; i++)
    {
      if (CheckArray[i])
      {
        int updated_gameBoard[] = new int[9];
        arrayCopy(_node_gameBoard, updated_gameBoard);

        if (_goal == GOAL.MAX)
          updated_gameBoard[i] = +1;
        else
          updated_gameBoard[i] = -1;

        Node N = new Node(invert(_goal), updated_gameBoard, _depth + 1);
        // Remember which node it has changed in that current state
        // Useful at the end when finding best child to use for next move
        N._boardIndexChanged = i;

        _children.add(N);
      }
    }
  }

  // Find Child that meets the goal criteria of the node
  // Once the child is found, it will return the childs index in the node
  public Node FindWinningChild()
  {
    int winning_index = -1;
    int winning_value = 0;
    if (_goal == GOAL.MAX)
      winning_value = -999;
    else if (_goal == GOAL.MIN)
      winning_value = +999;

    for (int i = 0; i < _children.size(); i++)
    {
      if (
        _children.get(i).GetScore() > winning_value && _goal == GOAL.MAX ||
        _children.get(i).GetScore() < winning_value && _goal == GOAL.MIN
        )
      {
        winning_index = i;
        winning_value = _children.get(i).GetScore();
      }
    }
    return _children.get(winning_index);
  }

  // Output scores of all children
  public void OutputChildrenScores()
  {
    println("Total Children: " + _children.size());
    for (int i = 0; i < _children.size(); i++)
    {
      println("Index Changed: " + _children.get(i)._boardIndexChanged + ", Final Score: " + _children.get(i).GetScore());
    }
    println("~~~");
  }
}