package com.wuhanqing.examples;

public class Algorithm {
	
	public static void main(String[] args) {
		
		double s = linearInterpolation(40, 50, 110, 116, 56.3909774);
		System.out.println(s);
		
	}
	
	public static double linearInterpolation(double x1, double x2, double y1, double y2, double x) {
		
		double slope = (y2 - y1) / (x2 - x1);
		double b = y1 - (slope * x1);
		double y = slope * x + b;
		return y;
	}
	
}
