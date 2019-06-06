
public class Ball {

	int x;
	int y;

	public Ball(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public boolean checkCoords(int x, int y) {
		if(this.x == x && this.y == y)
			return true;
		
		if((this.x-5 <= x && this.x+50 >= x) && (this.y-5 <= y && this.y+5 >= y))
			return true;
		
		return false;
	}
	
}
