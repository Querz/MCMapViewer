package net.querz.mcmapviewer;

public class FrameData {

	private int id;
	private int rotation;
	private Point3i pos;

	public FrameData(int id, int rotation, Point3i pos) {
		this.id = id;
		this.rotation = rotation;
		this.pos = pos;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getRotation() {
		return rotation;
	}

	public void setRotation(int rotation) {
		this.rotation = rotation;
	}

	public Point3i getPos() {
		return pos;
	}

	public void setPos(Point3i pos) {
		this.pos = pos;
	}
}
