package net.querz.mcmapviewer;

public enum BannerColor {

	WHITE("white", 0xffffff),
	ORANGE("orange", 0xffa500),
	MAGENTA("magenta", 0xff00ff),
	LIGHT_BLUE("light_blue", 0xadd8e6),
	YELLOW("yellow", 0xffff00),
	LIME("lime", 0x00ff00),
	PINK("pink", 0xffc0cb),
	GRAY("gray", 0x808080),
	LIGHT_GRAY("light_gray", 0xd3d3d3),
	CYAN("cyan", 0x00ffff),
	PURPLE("purple", 0x800080),
	BLUE("blue", 0x0000ff),
	BROWN("brown", 0xa52a2a),
	GREEN("green", 0x008000),
	RED("red", 0xff0000),
	BLACK("black", 0x000000);

	private String name;
	private int color;

	BannerColor(String name, int color) {
		this.name = name;
		this.color = color;
	}

	public int getColor() {
		return color;
	}

	public static BannerColor byName(String name) {
		for (BannerColor bc : values()) {
			if (bc.name.equals(name)) {
				return bc;
			}
		}
		throw new IllegalArgumentException("invalid banner color \"" + name + "\"");
	}
}
