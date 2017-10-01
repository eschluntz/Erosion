import processing.core.*; 
import processing.xml.*; 

import controlP5.*; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class erosion_sim extends PApplet {



//erosion constants
public float uplift = .06f;
public float erodability = .1f;
public float area_exponent = .5f;
public float slope_exponent = 1;

//map generation
int t_size = 50;
float boxBase = 10;
float speed = .003f;

//METHODS
public int color_method = 1; //1 = height, 2 = area, 3 = slope, 4 = rgb
public int routing_method = 1; //1 = steepest descent, 2 = distributed
public int side_method = 1; //1 = zero height, 2 = zero slope, 3 = wrap around

//control variables
public float noise_detail = .04f;
public float noise_scale = 10;
public float global_slope = .01f;
public int land = t_size;
public boolean eroding = false;

int len = t_size * t_size;
float max_height = 1;
float min_height = 0;

//viewing
public boolean spin = false;
public float zoom = -700;
public float angle = PI /4;
public float tilt = -PI /4;

//time
float t_frame;
float t_old;

//data
boolean[] key_array = new boolean[256];
zone[][] landscape;
spot[] sorted_spots;

//GUI
ControlP5 controlP5;
PGraphics cp5;

public void setup()
{
  size (1280, 720, P3D);
  noStroke();
  background(53, 117, 250);

  landscape = new zone[500][500];
  sorted_spots = new spot[250000];

    //GUI
  create_gui();
  generate();
}

public void draw()
{
  float h;
  float xd = boxBase * t_size / 2;
  float yd = boxBase * t_size / 2;

  //GUI fix
  background(0);
  beginRecord(cp5);
  controlP5.draw();
  endRecord();
  fill(100);
  background(129);
  image(cp5, 0, 0);

  //camera
  control_camera();

  pushMatrix();
  translate(width/2 -400 * zoom/1600, height/2, zoom);
  rotateX(tilt);
  rotateY(angle);

  //drawing landscape
  beginShape(QUADS);
  
  for (int x = 0; x < t_size - 1 ; x++) {
    for (int y = 0; y < t_size -1; y++) {

      landscape[x][y].set_color(); 
      vertex(boxBase * (x + 0) -xd, -boxBase * landscape[x+0][y+0].z, boxBase * (y + 0) - yd);
      //set_color(height_map[x][y+1]);
      vertex(boxBase * (x + 0) -xd, -boxBase *  landscape[x+0][y+1].z, boxBase * (y + 1) - yd);
      //set_color(height_map[x+1][y+1]);
      vertex(boxBase * (x + 1) -xd, -boxBase *  landscape[x+1][y+1].z, boxBase * (y + 1) - yd);
      //set_color(height_map[x+1][y]);
      vertex(boxBase * (x + 1) -xd, -boxBase *  landscape[x+1][y+0].z, boxBase * (y + 0) - yd);
    }
  }

  //drawing base
  fill(200, 200, 200);
  vertex(-xd, 0, - yd);
  vertex(-xd, 0, + yd);
  vertex(+xd, 0, + yd);
  vertex(+xd, 0, - yd);
  endShape();

  //timing
  t_frame = millis() - t_old;
  t_old = millis();
  popMatrix();
  if (eroding) erode_all(true);

}

/*
To implement:
[] diffusive flow distribution 
[DONE] fix height color
[DONE] add more color schemes
[] landslides
[DONE] different side methods
[DONE] speed up

Bugs:
[DONE] growing side
[DONE] negative height
[] wavering?

*/
 
public void create_gui()
{
  controlP5 = new ControlP5(this);  
  
  RadioButton r = controlP5.addRadio("color_method", 10, 200);
  r.add("height", 1);
  r.add("area", 2);
  r.add("slope", 3);
  r.add("mix", 4);
  
  RadioButton s = controlP5.addRadio("side_method", 80, 200);
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
  controlP5.addToggle("spin");
  controlP5.addButton("generate");
  controlP5.addToggle("eroding").linebreak();
  
  //erosion
  controlP5.addSlider("uplift", 0, 1).linebreak();
  controlP5.addSlider("erodability", 0, 1).linebreak();
  controlP5.addSlider("area_exponent", 0, 2).linebreak();
  controlP5.addSlider("slope_exponent", 0, 2).linebreak();

  cp5 = createGraphics(width*6, height*6, JAVA2D);

}
public void control_camera()
{
  if (spin) angle += .0002f * t_frame;
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
  if (key_array[79]) {
    zoom -= 100*speed*t_frame;
  }
  if (key_array[73] && (zoom < 300)) {
    zoom += 100*speed*t_frame;
  }
}
 

public void keyPressed()
{
  if (keyCode == ' ') generate();
  if (keyCode == 'E') erode_all(true);
  key_array[keyCode] = true;
}
 
public void keyReleased()
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

// basic class to store x,y,z variables of each point
class spot
{
  int x;
  int y;
  float z;
  
  public void debug()
  {
    println("x: " + x + " y: " + y + " z: " + z);
  }
  
  public void reset()
  {
    z = landscape[x][y].z;
  }
  
  spot(int xi, int yi, float zi)
  {
    x = xi;
    y = yi;
    z = zi;
  }
}



//-----------------------[erode all]-----------------------------------------------------------------------
//finds the run off area for each node
public void erode_all(boolean remove)
{
  //give everything area 1 [done]
  // sort from highest to lowest [done]
  //starting from highest point, distribute its area to lower neighbors additively [done]
  //store slopes as my location -> where my flow is going (edges have height 0) [done]
  //edges take all flow [done]
  //sorting array
  
  
  //updating spots
  for (int i = len - 1; i >= 0; i--) {
    sorted_spots[i].reset();
  } 
  
  //sorting locations from shortest to tallest
  //quick sort
  boolean swapped = true;
  while (swapped) {
    swapped = false;
    for (int i = 1; i < len; i++) {
      if (sorted_spots[i - 1].z > sorted_spots[i].z) {
        //swap
        spot temp = sorted_spots[i];
        sorted_spots[i] = sorted_spots[i-1];
        sorted_spots[i-1] = temp;
       
        swapped = true;
      }
    }
  }
  
  //println("done sorting");
  //recording highest and lowest points for coloring
  max_height = sorted_spots[len -1].z;
  min_height = sorted_spots[0].z;
  
  //setting all areas to 1
  for (int x = 0; x < t_size; x++) {
    for (int y = 0; y < t_size; y++) {
      //resetting area
      landscape[x][y].area = 1;
    }
  }
  
  //println("done setting all areas to 1");
  
  //distributing flow and finding slope
  for (int i = len - 1; i >= 0; i--) {
    
    /*println("x: " + landscape[sorted_spots[i].x][sorted_spots[i].y].x + 
    " y: " + landscape[sorted_spots[i].x][sorted_spots[i].y].y + "z: " + 
    landscape[sorted_spots[i].x][sorted_spots[i].y].z + " area: " + 
    landscape[sorted_spots[i].x][sorted_spots[i].y].area + " slope: " +  
    landscape[sorted_spots[i].x][sorted_spots[i].y].slope
    );*/
    landscape[sorted_spots[i].x][sorted_spots[i].y].find_next();
  } 
  
  //println("done routing flow");
  
  if (remove) {
  //now that area and slope has been found, we can erode
    for (int x = 0; x < t_size; x++) {
      for (int y = 0; y < t_size; y++) {
        landscape[x][y].erode();
      }
    }
    //println("done individual eroding");
  }
}


//class that is each point of land
class zone
{
  //-----------------------[fields]-----------------------------------------------------------------------
  //coordinates in array
  int x;
  int y;
  float z;
//color
  int cr;
  int cg;
  int cb;
//upstream area
  int area;
  float slope;
//pathfinding
  int next_x;
  int next_y;
  
  
  //-----------------------[constructor]-----------------------------------------------------------------------
  zone(int nx, int ny) {
    x = nx;
    y = ny;
    
    next_x = -1;
    next_y = -1;
    
    //setting height
    z = x * global_slope + noise_scale * noise(noise_detail * x, noise_detail * y);
    
    area = 0;
    
    set_color();
  }
  
//-------------------------[set_color]--------------------------------------------- 
  public void set_color()
  {
    //setting color
    switch(color_method)
    {
      //height colored
      case 1:
        cr = PApplet.parseInt(255 * (z - min_height) / (max_height - min_height));
        cg = cr;
        cb = cr;
        break;
      
      //area colored
      case 2:
        cr = PApplet.parseInt(100*sqrt(area));
        cg = cr;
        cb = cr;
        break;
        
      //slope colored
      case 3:
        cr = PApplet.parseInt(255 * slope);
        cg = cr;
        cb = cr;
        break;
        
      //rgb height, area, slope colored
      case 4:
        cr = PApplet.parseInt(255 * z / max_height);
        cg = PApplet.parseInt(100*sqrt(area));
        cb = PApplet.parseInt(255 * slope);
        break;
    }
    
    fill(cr,cg,cb);
  }
  //----------------------[erode]---------------------------------------------------------------
  
  public void erode()
  {
    this.z += uplift - erodability * pow(this.area, area_exponent) * pow(this.slope, slope_exponent);
    if (this.z < 0) this.z = 0;
    set_color();
    //check for landslides 
  }
   
  //----------------------[bound]---------------------------------------------------------
  
  //give it x y coords, it gives height, but deals with edges
  public spot bound(int i, int j)
  {
    spot s = new spot(i, j, 0);
    
    //dealing with sides
    if ((i  == -1) || (j  == -1) || (i == t_size) || (j == t_size))
    {
      switch(side_method)
      {

        //valley
        case 2:
          return new spot(-1, -1, this.z);
        
        //wrap around
        case 3:
          if (i < 0) i = t_size -1;
          if (j < 0) j = t_size -1;
          if (i == t_size) i = 0;
          if (j == t_size) j = 0;
          
          return new spot(i, j, landscape[i][j].z);
          
        //slightly lower  
        case 4:
          return new spot(-1, -1, .99f *this.z);
          
        //mountain
        default:
          return new spot(-1, -1, 0);
        
      }
    }
    
    //not on edge, just returning height
    else {
      return new spot(i,j,landscape[i][j].z);
    }
    
  }
  //-----------------------[find next]-----------------------------------------------------------------------
  public void find_next()
  {
    spot low = new spot(-1,-1, z);
    
  //looking for lower point
    for (int i = x - 1; i < x + 2; i++) {
      for (int j = y - 1; j < y + 2; j++) {

        if (bound(i,j).z < low.z)
        {
          low = bound(i,j);
        }
      }
    }
      
    //setting slope
    this.slope = landscape[x][y].z - low.z;
    if (0 != (low.x-x)*(low.y-y)) //only returns true if both dx and dy are not zero
    {
      this.slope = this.slope/1.4142f; //diagonal distance
    }
        
    //giving flow
    if (low.x != -1) //checking if in local min
    {
      landscape[low.x][low.y].area += this.area;
    }
  }
}


//-----------------------[generate]-----------------------------------------------------------------------
public void generate()
{
  noiseSeed(millis());
  
  //setting permanent variables to control variables
  t_size = land;
  max_height = 2 * global_slope * t_size + noise_scale;
  len = t_size * t_size;
  //initializing all cells
  int i = 0;
  for (int x = 0; x < t_size; x++) {
    for (int y = 0; y < t_size; y++) {
      landscape[x][y] = new zone(x,y);
      sorted_spots[i] = new spot(x,y,landscape[x][y].z);
      i++;
    }
  }
  
  erode_all(false);
}

    
  static public void main(String args[]) {
    PApplet.main(new String[] { "--bgcolor=#F0F0F0", "erosion_sim" });
  }
}
