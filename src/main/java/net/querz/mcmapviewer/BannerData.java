package net.querz.mcmapviewer;

public class BannerData {

	private String name;
	private BannerColor color;
	private Point3i pos;

	public BannerData(String name, String color, Point3i pos) {
		this.name = name;
		this.color = BannerColor.byName(color);
		this.pos = pos;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BannerColor getColor() {
		return color;
	}

	public void setColor(BannerColor color) {
		this.color = color;
	}

	public Point3i getPos() {
		return pos;
	}

	public void setPos(Point3i pos) {
		this.pos = pos;
	}
}
