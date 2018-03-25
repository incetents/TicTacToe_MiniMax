
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
  private color _color;
  private ParticleShape _shapetype;

  public Particle(ParticleShape ps, color c)
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

  public void SetColor(color c)
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