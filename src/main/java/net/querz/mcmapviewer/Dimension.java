package net.querz.mcmapviewer;

public enum Dimension {

	OVERWORLD(0, "Overworld"),
	THE_END(1, "The End"),
	NETHER(-1, "Nether");

	private int id;
	private String text;

	Dimension(int id, String text) {
		this.id = id;
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

	public int getId() {
		return id;
	}
}
