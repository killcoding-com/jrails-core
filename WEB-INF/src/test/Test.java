package test;

import net.rails.ext.AbsGlobal;
import net.rails.support.Support;

public class Test {

	public static AbsGlobal g;
	
	static{
		g = new GlobalUnit();
	}
	
	public static void main(String[] args) {
		System.out.println(Support.env().getEnv());
		
	}
	
}
