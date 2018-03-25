
// Shapes that are drawn on screen
Particle _XShape;
Particle _OShape;
ArrayList<Emitter> _Spray = new ArrayList<Emitter>();
void AddSpray(PVector position, float r, float g, float b)
{
  _Spray.add(new Emitter(position, 10, 8f, r, g, b));
}
void ClearAllSprays()
{
  _Spray.clear();
}

// Scale values for the shapes on the board
// first array one is just scale amount, second one is time
float[] graphicsGrow = new float[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
float[] graphicsGrowt = new float[]{0, 0, 0, 0, 0, 0, 0, 0, 0};

void resetGraphicsGrow()
{
  for (int i = 0; i < 9; i++)
  {
    graphicsGrow[i] = 0.0f;
    graphicsGrowt[i] = 0.0f;
  }
}

// Increase Grow value of the array
void GrowValue(int index)
{
  // Update time
  graphicsGrowt[index] += 0.08f;
  graphicsGrowt[index] = min(graphicsGrowt[index], 1.0f);
  // Update Size
  graphicsGrow[index] = lerp(0.0f, 1.0f, sqrt(graphicsGrowt[index]));
}

// GRAPHICS
void drawBoardLines(color c)
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
void drawGamePieces()
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
void FlipXO(boolean state)
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