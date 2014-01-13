package com.keypuncher.awakening.pathfinding;

import java.util.*;

public class Pathfinder {

	private int[] impassable;
	public static int CLOSED = 0XFFFFFFF;

	public Pathfinder(int[] impassable) {
		this.impassable = impassable;
	}

	public Node[] calculatePath(int[][] map, int startX, int startY, int endX,
			int endY) {
		ArrayList<Node> path = new ArrayList<Node>();
		ArrayList<Node> openList = new ArrayList<Node>();
		Node start = new Node(null, startX, startY, calcF(startX, startY,
				startX, startY, endX, endY));
		openList.add(start);
		map[startX][startY] = CLOSED;

		Node p = null;
		for (int i = 0; i < 1000; i++) {
			double f = CLOSED;
			for (Node node : openList) {
				if (node.f < f) {
					p = node;
					f = node.f;
				}
			}
			if (openList.size() <= 0) {
				return retracePath(p);
			}
			if (p.x == endX && p.y == endY) {
				return retracePath(p);
			}

			map[p.x][p.y] = CLOSED;
			path.add(p);
			openList.remove(p);

			if (p.x - 1 >= 0 && passable(map[p.x - 1][p.y])) {
				openList.add(new Node(p, p.x - 1, p.y, calcF(p.x - 1, p.y,
						startX, startY, endX, endY)));
			}
			if (p.x + 1 < map.length && passable(map[p.x + 1][p.y])) {
				openList.add(new Node(p, p.x + 1, p.y, calcF(p.x + 1, p.y,
						startX, startY, endX, endY)));
			}
			if (p.y - 1 >= 0 && passable(map[p.x][p.y - 1])) {
				openList.add(new Node(p, p.x, p.y - 1, calcF(p.x, p.y - 1,
						startX, startY, endX, endY)));
			}
			if (p.y + 1 < map[0].length && passable(map[p.x][p.y + 1])) {
				openList.add(new Node(p, p.x, p.y + 1, calcF(p.x, p.y + 1,
						startX, startY, endX, endY)));
			}
		}
		return retracePath(p);
	}

	public Node[] retracePath(Node node) {
		ArrayList<Node> retracedPath = new ArrayList<Node>();

		retracedPath.add(node);
		Node buffer = node;
		int z = 0;
		while ((buffer = buffer.parent) != null && z < 100) {
			retracedPath.add(buffer);
			z++;
		}
		Node[] path = new Node[retracedPath.size()];
		for (int i = retracedPath.size() - 1; i >= 0; i--) {
			path[retracedPath.size() - i - 1] = retracedPath.get(i);
		}

		return path;
	}

	public boolean passable(int i) {
		for (int tile : impassable) {
			if (i == tile || CLOSED == i)
				return false;
		}
		return true;
	}

	public double calcF(int x, int y, int startX, int startY, int endX, int endY) {
		double G = Math.abs(x - startX) + Math.abs(y - startY);
		double H = Math.abs(x - endX) + Math.abs(y - endY);
		return G + H;
	}

}