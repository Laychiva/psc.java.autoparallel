
public class TestCode {
	public int x;
	public String string = "hello";
	static int y = 3;


	// not read only
	// not threadSafe since it modifies a field
	public void update1() {
		x=x+20;
	}
	// not threadSafe since it modifies a field
	// not read only
	public void update12132() {
		string = string + "ahaha";
	}
	// not read only
	// use postfix expression
	// but modiflocal
	// not threadSafe since it modifies a field
	public void update11() {
		x++;
		y = y + 1;
	}
	// not read only
	// use prefix expression
	// not threadSafe since it modifies a field
	public void update14() {
		--x;
	}
	// not read only
	//using assignment operator
	// not threadSafe since it modifies a field
	public void update12() {
		x+=1;
	}
	// not read only
	//using assignment operator
	// not threadSafe since it modifies a field
	public void update13() {
		x=+1;
	}
	// not read only
	// not threadSafe since it modifies a field
	public int updatenested(int a) {
		x = x + a;
		return x;
	}
	//parallelizable
	//threadSafe
	public int norednomodif() {
		return x + string.length();
	}
	//readonly method call a not readonly -> not read only
	// not threadSafe since it modifies a field
	public int NoReadNoModif() {
		x = updatenested(10);
		return x;
	}
	// not read only
	// not threadSafe since it modifies a field
	public int sum(int a) {
		a = 10;
		x = updatenested(a);
		return x;
	}
	//not readonly
	// not threadSafe since it modifies a field
	public int update2() {
		x = x + string.length();
		return x;
	}
	//Not read only
	// not threadSafe since it modifies a field
	public int update22() {
		int y = 3;
		x = x + y;
		return x;
	}
	//ModifLocal
	// threadSafe
	public int update232() {
		int y = 3;
		y++;
		return y;
	}
	//ModifLocal
	//ThreadSafe
	public boolean update2222() {
		boolean y = true;
		y = false;
		return y;
	}
	//ModifLocal
	//ThreadSafe
	public int update2212() {
		int y = 4;
		--y;
		return y;
	}
	//ModifLocal
	//ThreadSafe
	public int update3() {
		int y = 3;
		y = y + string.length();
		return y;
	}
}
