
// basic class to store x,y,z variables of each point
class spot
{
  int x;
  int y;
  float z;
  
  void debug()
  {
    println("x: " + x + " y: " + y + " z: " + z);
  }
  
  void reset()
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
void erode_all(boolean remove)
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
  
  println("done sorting");
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
  
  println("done setting all areas to 1");
  
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
  
  println("done routing flow");
  
  if (remove) {
  //now that area and slope has been found, we can erode
    for (int x = 0; x < t_size; x++) {
      for (int y = 0; y < t_size; y++) {
        landscape[x][y].erode();
      }
    }
    println("done individual eroding");
  }
}

