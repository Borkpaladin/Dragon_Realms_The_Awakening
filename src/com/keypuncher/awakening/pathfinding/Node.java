package com.keypuncher.awakening.pathfinding;

public class Node {
	public Node parent;
	public int x, y;
	public double f;

	public Node(Node parent, int x, int y, double f) {
		this.x = x;
		this.y = y;
		this.f = f;
		this.parent = parent;
	}

}
