package net.querz.mcmapviewer;

public enum Scale {

	SCALE_0(0),
	SCALE_1(1),
	SCALE_2(2),
	SCALE_3(3),
	SCALE_4(4);

	private int id;

	Scale(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "" + id;
	}

	public static Scale byId(int id) {
		for (Scale scale : values()) {
			if (id == scale.id) {
				return scale;
			}
		}
		throw new IllegalArgumentException("invalid scale: " + id);
	}

	public byte getId() {
		return (byte) id;
	}
}
