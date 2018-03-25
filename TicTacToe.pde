
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
void setup()
{
  frameRate(60);
  size(800, 800); 

  memory = new Memory("TicTacToeBrain.txt");

  _XShape = new Particle(ParticleShape.X, color(255, 0, 0, 255));
  _OShape = new Particle(ParticleShape.O, color(30, 70, 255, 255));
}

void draw()
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

void keyPressed()
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
void dispose()
{
  // Output Monkey Mode State
  game.OutputMonkeyState();
  // Save Data to text file after closing
  memory.SaveData();
} 