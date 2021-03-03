package net.querz.mcmapviewer.map;

public enum Dimension {

	OVERWORLD(0, "minecraft:overworld", "Overworld"),
	THE_END(1, "minecraft:the_end", "The End"),
	NETHER(-1, "minecraft:nether", "Nether");

	private int id;
	private String textID;
	private String text;

	Dimension(int id, String textID, String text) {
		this.id = id;
		this.textID = textID;
		this.text = text;
	}

	@Override
	public String toString() {
		return text;
	}

	public static Dimension byId(int id) {
		for (Dimension dimension : values()) {
			if (id == dimension.id) {
				return dimension;
			}
		}
		throw new IllegalArgumentException("invalid dimension: " + id);
	}

	public static Dimension byTextId(String id) {
		for (Dimension dimension : values()) {
			if (dimension.textID.equals(id)) {
				return dimension;
			}
		}
		throw new IllegalArgumentException("invalid dimension: " + id);
	}

	public int getId() {
		return id;
	}

	public String getTextID() {
		return textID;
	}
}
