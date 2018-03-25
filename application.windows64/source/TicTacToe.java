import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.io.FileWriter; 
import java.io.BufferedWriter; 
import java.util.Map; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class TicTacToe extends PApplet {


// Save Data to TEXT
boolean USE_MEMORY = true;

// Debug Print Values
boolean DEBUG_PRINT_VALUES = false;

// Class holding generic game setting data
Game game = new Game();

// Wrapper class to handle loading and exporting of text file
// to help the AI make smart moves
Memory memory = null;

// Generic Setup stuff for processing
public void setup()
{
  frameRate(60);
   

  memory = new Memory("TicTacToeBrain.txt");

  _XShape = new Particle(ParticleShape.X, color(255, 0, 0, 255));
  _OShape = new Particle(ParticleShape.O, color(30, 70, 255, 255));
}

public void draw()
{
  // Generic Settings
  background(0);
  fill(255);
  textSize(64);

  // Graphic portion
  drawBoardLines(color(255));
  drawGamePieces();
  for (int i = 0; i < _Spray.size(); i++)
  {
    _Spray.get(i).Update();
    _Spray.get(i).Draw();
  }

  // PROMPT PLAYER / AI FOR MOVE
  if (state == GameState.NORMAL)
  {
    if (game.isPlayerTurn)
    {
      if (MonkeyMode)
        game.monkeyMove();
      else
        game.playerMove();
        
    } else
    {
      game.enemyMove();
    }
  }

  // OUTPUT WINNER
  else if (state == GameState.PLAYERWIN)
  {
    text("PLAYER WINS", width/2, height/2);
  } else if (state == GameState.AIWIN)
  {
    text("AI WINS", width/2, height/2);
  } else if (state == GameState.DRAW)
  {
    text("DRAW", width/2, height/2);
  }

  // Monkey Mode Auto-Reset
  if (MonkeyMode && state != GameState.NORMAL)
  {
    game.reset();
  }

  // Reminder 1
  textSize(18);
  textAlign(LEFT, TOP);
  if (CheckEmptyBoard())
  {
    if (!FlipGraphics)
      text("Press 1 To Play as O", 20, 20);
    else
      text("Press 2 To Play As X", 20, 20);
  }

  // Reminder 2
  textSize(12);
  text("Press 3 To Toggle MonkeyMode", 20, height -30);

  // Reminder 3
  textSize(18);
  textAlign(CENTER, CENTER);
  text("Press 'R' to reset", width/2, height - 20.0f);
}

public void keyPressed()
{
  // Shortcut for resetting
  if (key == 'r' || key == 'R')
  {
    game.reset();
  }
  // Flip Graphics
  if (key == '1')
    FlipXO(true);
  else if (key == '2')
    FlipXO(false);

  // Toggle MonkeyMode
  if (key == '3' && CheckEmptyBoard())
    MonkeyMode = true;
  else if (key == '3' && MonkeyMode)
  {
    // Output Monkey Mode State
    //game.OutputMonkeyState();
    MonkeyMode = false;
  }
}

// Run this code when it is about to exit
public void dispose()
{
  // Output Monkey Mode State
  game.OutputMonkeyState();
  // Save Data to text file after closing
  memory.SaveData();
} 

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
public GOAL invert(GOAL G)
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
  public void findChildren()
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

GameState state = GameState.NORMAL;

boolean MonkeyMode = false;

class Game
{
  private AI _ai = new AI();
  public boolean isPlayerTurn = true;

  // returns index of box that mouse is in
  public int checkMouseBox()
  {
    int result = ((mouseX*3) / width) + ((mouseY*3) / height) * 3;
    // Clamp mouse values
    return max(min(result, 8), 0);
  }

  // Monkey Moves possible (basically attempts to play every single possible move in tic tac toe)
  int[] _MonkeyMoves = new int[] {0, 4, 5, 0, 1};
  int   _TotalMonkeyMoves = 5;
  int   _MonkeyTurnIndex = 0;
  public void NextMonkeyMove()
  {
    _MonkeyTurnIndex++;
  }
  public void NextMonkeyState()
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
  public boolean CheckMonkeyStateHasDuplicateMoves()
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
  public int GetCurrentMonkeyMove()
  {
    int offset = 0;
    while (gameBoard[(_MonkeyMoves[_MonkeyTurnIndex] + offset) % 9] != 0)
    {
      offset++;
    }
    return (_MonkeyMoves[_MonkeyTurnIndex] + offset) % 9;
  }
  public void OutputMonkeyState()
  {
    print("MonkeyMode State: ");
    for (int i = 0; i < game._TotalMonkeyMoves; i++)
    {
      print(game._MonkeyMoves[i] + " ");
    }
    println();
  }

  // Simulate Player Turn (used for lots of quick input
  public void monkeyMove()
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
  public void playerMove()
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
  public void enemyMove()
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
  public void reset()
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
  public void updateWinner()
  {
    state = CheckWinner(gameBoard);
  }
}

// Shapes that are drawn on screen
Particle _XShape;
Particle _OShape;
ArrayList<Emitter> _Spray = new ArrayList<Emitter>();
public void AddSpray(PVector position, float r, float g, float b)
{
  _Spray.add(new Emitter(position, 10, 8f, r, g, b));
}
public void ClearAllSprays()
{
  _Spray.clear();
}

// Scale values for the shapes on the board
// first array one is just scale amount, second one is time
float[] graphicsGrow = new float[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
float[] graphicsGrowt = new float[]{0, 0, 0, 0, 0, 0, 0, 0, 0};

public void resetGraphicsGrow()
{
  for (int i = 0; i < 9; i++)
  {
    graphicsGrow[i] = 0.0f;
    graphicsGrowt[i] = 0.0f;
  }
}

// Increase Grow value of the array
public void GrowValue(int index)
{
  // Update time
  graphicsGrowt[index] += 0.08f;
  graphicsGrowt[index] = min(graphicsGrowt[index], 1.0f);
  // Update Size
  graphicsGrow[index] = lerp(0.0f, 1.0f, sqrt(graphicsGrowt[index]));
}

// GRAPHICS
public void drawBoardLines(int c)
{
  // Draw Lines
  strokeWeight(20.0f);
  stroke(c);
  for (float x = 0; x <= 3; x++)
  {
    line(
      width * x/3.0f, 0, 
      width * x/3.0f, height
      );
  }
  for (float y = 0; y <= 3; y++)
  {
    line(
      0, height * y/3.0f, 
      width, height * y/3.0f
      );
  }
}

// GRAPHICS
public void drawGamePieces()
{
  tint(255, 255);
  float w_scaled = width  /3.0f; 
  float h_scaled = height /3.0f;
  float w_scaled_half = w_scaled * 0.5f;
  float h_scaled_half = h_scaled * 0.5f;

  for (int i = 0; i < 9; i++)
  {
    if (gameBoard[i] == -1)
    {
      GrowValue(i);
      _XShape.Draw(w_scaled * (i%3) + w_scaled_half, h_scaled * (i/3) + h_scaled_half, graphicsGrowt[i], graphicsGrowt[i] * 180.0f);
    } else if (gameBoard[i] == +1)
    {
      GrowValue(i);
      _OShape.Draw(w_scaled * (i%3) + w_scaled_half, h_scaled * (i/3) + h_scaled_half, graphicsGrowt[i], graphicsGrowt[i] * 180.0f);
    }
  }
}

// Flip Graphics
boolean FlipGraphics = false;

// Flip Graphics
public void FlipXO(boolean state)
{
  if (CheckEmptyBoard())
  {
    if (state != FlipGraphics)
    {
      Particle Temp = _XShape;
      _XShape = _OShape;
      _OShape = Temp;
    }

    FlipGraphics = state;
  }
}





public class Memory
{
  private String _directory;
  private HashMap<String, Integer> mem = new HashMap<String, Integer>();
  private int mem_size = 0;
  private PrintWriter output;

  // Find all values from the text file and store it into the hash
  // only lines that are 10 characters long should be accepted logically
  public Memory(String directory)
  {
    _directory = directory;
    String[] lines = null;
    try
    {
      lines = loadStrings(directory);
    }
    finally
    {
      if (lines == null)
        return;

      for (int i = 0; i < lines.length; i++)
      {
        if (lines[i].length() != 10)
          continue;

        String hash = lines[i].substring(0, 9);
        int data = PApplet.parseInt(lines[i].substring(9, 10));
        Add(hash, data);
      }
    }
  }

  // Add value to hashmap
  private void Add(String hash, int data)
  {
    mem.put(hash, data);
    mem_size++;
  }
  // Return value of hasmap with error check
  private int Get(String hash)
  {
    int value = -1;
    try
    {
      value = mem.get(hash);
    }
    finally
    {
      return value;
    }
  }

  // Saves all data of the hashmap into the same text file it was reading from (overwrite)
  private void SaveData()
  {
    if (USE_MEMORY)
    {
      String[] _Results = new String[mem_size];
      int index = 0;

      for (Map.Entry me : mem.entrySet())
      {
        _Results[index] = me.getKey().toString() + me.getValue().toString();
        index++;
      }
      println("Total Strings Saved: " + mem_size);
      _Results = sort(_Results);

      output = createWriter(_directory);
      for (int i = 0; i < _Results.length; i++)
      {
        output.println(_Results[i]);
      }
      output.flush();
      output.close();
    }
  }
}

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
public boolean[] OptimizationChecks(int[] board)
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
public boolean CheckEdgeColumnsMatch()
{
  return Match_0_2 && Match_3_5 && Match_6_8;
}
// If 1st and 3rd row are the same, ignore the bottom row
public boolean CheckEdgeRowsMatch()
{
  return Match_0_6 && Match_1_7 && Match_2_8;
}
// If top left blocks in an L shape are the same as the bottom right ones in an L shape,
public boolean CheckForwardDiagonalMatch()
{
  return Match_0_8 && Match_1_7 && Match_3_5;
}
// If top right blocks in an L shape are the same as the bottom left ones in an L shape,
public boolean CheckBackwardDiagonalMatch()
{
  return Match_1_7 && Match_2_6 && Match_3_5;
}

// Check if player is alone in the board, if true, return his index;
public int CheckPlayerAlone(int[] board, int ignore_index)
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

// THIS CLASS IS ONLY FOR GRAPHICAL EFFECTS

class Emitter
{
  private Particle _Triangle = new Particle(ParticleShape.TRI, color(255));
  private PVector[] _positions;
  private PVector[] _velocities;
  private int _amount;
  private float _r, _g, _b;
  private float _t = 0.0f;
  public Emitter(PVector position, int amount, float speed, float r, float g, float b)
  {
    _r = r;
    _g = g;
    _b = b;
    _amount = amount;
    _positions = new PVector[_amount];
    _velocities = new PVector[_amount];
    for (int i = 0; i < _amount; i++)
    {
      _positions[i] = new PVector(position.x, position.y);
      _velocities[i] = new PVector(random(-1, 1), random(-1, 0));
      _velocities[i].normalize();
      _velocities[i].x *= speed + random(-speed*0.5f, speed*0.5f);
      _velocities[i].y *= speed + random(-speed*0.5f, speed*0.5f);
    }
  }

  public void Update()
  {
    for (int i = 0; i < _amount; i++)
    {
      _positions[i].x += _velocities[i].x * 0.5f;
      _positions[i].y += _velocities[i].y;
      // gravity
      _velocities[i].y += 0.49f;
    }
    _t++;
  }
  public void Draw()
  {
    for (int i = 0; i < _amount; i++)
    {
      float alpha = 255.0f - _t * 3.5f;
      // API glitch fix
      if(alpha < 1)
        alpha = 1.0f;
      
      _Triangle.SetColor(color(_r*255, _g*255, _b*255, alpha));
      _Triangle.Draw(_positions[i].x, _positions[i].y, 0.1f, _velocities[i].mag() * 45.0f);
    }
  }
}

enum ParticleShape
{
  X, 
    O, 
    TRI
}

class Particle
{
  private PShape _shape;
  private int _color;
  private ParticleShape _shapetype;

  public Particle(ParticleShape ps, int c)
  {
    _shapetype = ps;
    _color = c;
    CreateShape();
  }
  private void CreateShape()
  {
    _shape = createShape();
    SetColor(_color);
    switch(_shapetype)
    {
    case TRI:
      _shape.beginShape(TRIANGLES);
      _shape.noStroke();

      _shape.vertex(-66, 66);
      _shape.vertex(+66, 66);
      _shape.vertex(0, -100);
      _shape.endShape();
      break;
    case O:
      _shape.beginShape(QUADS);
      _shape.noStroke();

      // EDGES (FAKE TRIS)
      _shape.vertex(-80, 60);
      _shape.vertex(-50, 100);
      _shape.vertex(-50, 60);
      _shape.vertex(-50, 60);

      _shape.vertex(-80, -60);
      _shape.vertex(-50, -100);
      _shape.vertex(-50, -60);
      _shape.vertex(-50, -60);

      _shape.vertex(+80, -60);
      _shape.vertex(+50, -100);
      _shape.vertex(+50, -60);
      _shape.vertex(+50, -60);

      _shape.vertex(+80, +60);
      _shape.vertex(+50, +100);
      _shape.vertex(+50, +60);
      _shape.vertex(+50, +60);

      // QUADS
      _shape.vertex(-50, 100);
      _shape.vertex(-50, 60);
      _shape.vertex(+50, 60);
      _shape.vertex(+50, 100);

      _shape.vertex(-50, -100);
      _shape.vertex(-50, -60);
      _shape.vertex(+50, -60);
      _shape.vertex(+50, -100);

      _shape.vertex(-80, -60);
      _shape.vertex(-80, 60);
      _shape.vertex(-50, 60);
      _shape.vertex(-50, -60);

      _shape.vertex(+80, -60);
      _shape.vertex(+80, 60);
      _shape.vertex(+50, 60);
      _shape.vertex(+50, -60);
      _shape.endShape();

      break;
    case X:
      _shape.beginShape();
      _shape.noStroke();

      // Bottom left
      _shape.vertex(-100, 75);
      _shape.vertex(-100, 100);
      _shape.vertex(-75, 100);

      // Bottom
      _shape.vertex(0, 25);

      // Bottom Right
      _shape.vertex(75, 100);
      _shape.vertex(100, 100);
      _shape.vertex(100, 75);

      // Right
      _shape.vertex(25, 0);

      // Top Right
      _shape.vertex(+100, -75);
      _shape.vertex(+100, -100);
      _shape.vertex(+75, -100);

      // Top
      _shape.vertex(0, -25);

      // Top left
      _shape.vertex(-75, -100);
      _shape.vertex(-100, -100);
      _shape.vertex(-100, -75);

      // Left
      _shape.vertex(-25, 0);
      _shape.endShape(CLOSE);
      break;
    }
  }

  public void SetColor(int c)
  {
    _color = c;
    _shape.setFill(_color);
    //_shape.setStroke(color(0));
  }

  public void Draw(float x, float y, float scale, float rotation)
  {
    pushMatrix(); 
    translate(x, y);
    scale(scale);
    rotate(rotation * (PI / 180.0f));
    shape(_shape, 0, 0);
    popMatrix();
  }
}

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
public GameState CheckLineWinner(int board[], int index1, int index2, int index3)
{
  int total = board[index1] + board[index2] + board[index3];

  if (total == 3)
    return GameState.AIWIN;
  else if (total == -3)
    return GameState.PLAYERWIN;

  return GameState.NORMAL;
}

public boolean CheckTie(int board[])
{
  for (int i = 0; i < 9; i++)
  {
    if (board[i] == 0)
      return false;
  }
  return true;
}

public GameState CheckWinner(int board[])
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
public String BoardToString(int board[])
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
public boolean CheckEmptyBoard()
{
  int empty = 0;
  for (int i = 0; i < 9; i++)
  {
    if (gameBoard[i] == 0)
      empty++;
  }
  return empty == 9;
}
  public void settings() {  size(800, 800); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "TicTacToe" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
