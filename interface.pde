 
void create_gui()
{
  controlP5 = new ControlP5(this);  
  
  Radio r = controlP5.addRadio("color_method", 10, 200);
  r.add("height", 1);
  r.add("area", 2);
  r.add("slope", 3);
  r.add("mix", 4);
  
  Radio s = controlP5.addRadio("side_method", 80, 200);
  s.add("edges: cliff", 1);
  //s.add("basin", 2);
  //s.add("wrap around", 3); //these don't work well
  s.add("edges: flat", 4);
  
  //map generation
  controlP5.addSlider("land", 1, 150).linebreak();
  //controlP5.addSlider("noise_detail", 0, .1).linebreak(); //this isn't really necessary
  controlP5.addSlider("noise_scale", 0, 100).linebreak();
  //controlP5.addSlider("global_slope", 0, 1).linebreak(); //this is to start the world on a slant
  
  //control
  //controlP5.addToggle("spin");
  controlP5.addButton("generate");
  controlP5.addToggle("eroding").linebreak();
  
  //erosion
  controlP5.addSlider("uplift", 0, 1).linebreak();
  controlP5.addSlider("erodability", 0, 1).linebreak();
  controlP5.addSlider("area_exponent", 0, 2).linebreak();
  controlP5.addSlider("slope_exponent", 0, 2).linebreak();

  cp5 = createGraphics(width*6, height*6, JAVA2D);

}
void control_camera()
{
  if (spin) angle += .0002 * t_frame;
  if (key_array[LEFT]) {
    angle += speed*t_frame;
  }
  if (key_array[RIGHT]) {
    angle -= speed*t_frame;
  }
  if (key_array[UP] && (tilt > -PI/2)) {
    tilt -= speed*t_frame;
  }
  if (key_array[DOWN] && (tilt < 0)) {
    tilt += speed*t_frame;
  }
  if (key_array[34]) {
    zoom -= 100*speed*t_frame;
  }
  if (key_array[33] && (zoom < 300)) {
    zoom += 100*speed*t_frame;
  }
}
 

void keyPressed()
{
  if (keyCode == ' ') generate();
  if (keyCode == 'E') erode_all(true);
  key_array[keyCode] = true;
}
 
void keyReleased()
{
  key_array[keyCode] = false;
}

//--------------------------[colors]-------------------------------------------------
public void elevation()
{
  color_method = 1;
}

public void area()
{
  color_method = 2;
}

public void slope()
{
  color_method = 3;
}

public void mix()
{
  color_method = 4;
}
