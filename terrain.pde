
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
  void set_color()
  {
    //setting color
    switch(color_method)
    {
      //height colored
      case 1:
        cr = int(255 * (z - min_height) / (max_height - min_height));
        cg = cr;
        cb = cr;
        break;
      
      //area colored
      case 2:
        cr = int(100*sqrt(area));
        cg = cr;
        cb = cr;
        break;
        
      //slope colored
      case 3:
        cr = int(255 * slope);
        cg = cr;
        cb = cr;
        break;
        
      //rgb height, area, slope colored
      case 4:
        cr = int(255 * z / max_height);
        cg = int(100*sqrt(area));
        cb = int(255 * slope);
        break;
    }
    
    fill(cr,cg,cb);
  }
  //----------------------[erode]---------------------------------------------------------------
  
  void erode()
  {
    this.z += uplift - erodability * pow(this.area, area_exponent) * pow(this.slope, slope_exponent);
    if (this.z < 0) this.z = 0;
    set_color();
    //check for landslides 
  }
   
  //----------------------[bound]---------------------------------------------------------
  
  //give it x y coords, it gives height, but deals with edges
  spot bound(int i, int j)
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
          return new spot(-1, -1, .99 *this.z);
          
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
  void find_next()
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
      this.slope = this.slope/1.4142; //diagonal distance
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

    
