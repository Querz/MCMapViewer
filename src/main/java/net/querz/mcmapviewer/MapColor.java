package net.querz.mcmapviewer;

import javafx.scene.paint.Color;
import java.util.HashMap;
import java.util.Map;

public enum MapColor {

	AIR(0, 0x00000000),
	GRASS(4, 0xff597d27),
	SAND(8, 0xffaea473),
	CLOTH(12, 0xff8c8c8c),
	TNT(16, 0xffb40000),
	ICE(20, 0xff7070b4),
	IRON(24, 0xff757575),
	FOLIAGE(28, 0xff005700),
	SNOW(32, 0xffb4b4b4),
	CLAY(36, 0xff737681),
	DIRT(40, 0xff6a4c36),
	STONE(44, 0xff4f4f4f),
	WATER(48, 0xff2d2db4),
	WOOD(52, 0xff645432),
	QUARTZ(56, 0xffb4b1ac),
	ADOBE(60, 0xff985924),
	MAGENTA(64, 0xff7d3598),
	LIGHT_BLUE(68, 0xff486c98),
	YELLOW(72, 0xffa1a124),
	LIME(76, 0xff599011),
	PINK(80, 0xffaa5974),
	GRAY(84, 0xff353535),
	SILVER(88, 0xff6c6c6c),
	CYAN(92, 0xff35596c),
	PURPLE(96, 0xff592c7d),
	BLUE(100, 0xff24357d),
	BROWN(104, 0xff483524),
	GREEN(108, 0xff485924),
	RED(112, 0xff6c2424),
	BLACK(116, 0xff111111),
	GOLD(120, 0xffb0a836),
	DIAMOND(124, 0xff409a96),
	LAPIS(128, 0xff345ab4),
	EMERALD(132, 0xff009928),
	OBSIDIAN(136, 0xff5b3c22),
	NETHERRACK(140, 0xff4f0100),
	WHITE_STAINED_HARDENED_CLAY(144, 0xff937c71),
	ORANGE_STAINED_HARDENED_CLAY(148, 0xff703919),
	MAGENTA_STAINED_HARDENED_CLAY(152, 0xff693d4c),
	LIGHT_BLUE_STAINED_HARDENED_CLAY(156, 0xff4f4c61),
	YELLOW_STAINED_HARDENED_CLAY(160, 0xff835d19),
	LIME_STAINED_HARDENED_CLAY(164, 0xff485225),
	PINK_STAINED_HARDENED_CLAY(168, 0xff703637),
	GRAY_STAINED_HARDENED_CLAY(172, 0xff281c18),
	SILVER_STAINED_HARDENED_CLAY(176, 0xff5f4b45),
	CYAN_STAINED_HARDENED_CLAY(180, 0xff3d4040),
	PURPLE_STAINED_HARDENED_CLAY(184, 0xff56333e),
	BLUE_STAINED_HARDENED_CLAY(188, 0xff352b40),
	BROWN_STAINED_HARDENED_CLAY(192, 0xff352318),
	GREEN_STAINED_HARDENED_CLAY(196, 0xff35391d),
	RED_STAINED_HARDENED_CLAY(200, 0xff642a20),
	BLACK_STAINED_HARDENED_CLAY(204, 0xff1a0f0b);

	private int id;
	private int color;

	private static Map<Integer, Integer> colors = new HashMap<>();
	private static Map<Integer, Color> javaFXColors = new HashMap<>();

	private static final int[] mul = new int[]{180, 220, 255, 235};

	static {
		for (MapColor mc : MapColor.values()) {
			//TODO: make this smaller
			int a = mc.color >> 24 & 0xFF;
			int r = mc.color >> 16 & 0xFF;
			int g = mc.color >> 8 & 0xFF;
			int b = mc.color & 0xFF;
			for (int i = 0; i < 4; i++) {
				int newId = mc.id + i;
				int newR = r * mul[i] / 255;
				int newG = g * mul[i] / 255;
				int newB = b * mul[i] / 255;

				int newC = a << 24;
				newC |= newR << 16;
				newC |= newG << 8;
				newC |= newB;

				colors.put(newId, newC);
				javaFXColors.put(newId, new Color((float) newR / 255f, (float) newG / 255f, (float) newB / 255f, (float) a / 255f));

				System.out.println(newR + " / " + newG + " / " + newB);
			}
		}
	}

	MapColor(int id, int color) {
		this.id = id;
		this.color = color;
	}

	public static int getColor(int id) {
		return colors.getOrDefault(id, 0x00000000);
	}

	public static Color getJavaFXColor(int id) {
		return javaFXColors.getOrDefault(id, Color.TRANSPARENT);
	}

	public static int findClosestColor(int color) {
		int r = color >> 16 & 0xFF;
		int g = color >> 8 & 0xFF;
		int b = color & 0xFF;

		int closest = 0x00000000;
		int closestDist = Integer.MAX_VALUE;

		for(int i : colors.values()) {
			int mr = i >> 16 & 0xFF;
			int mg = i >> 8 & 0xFF;
			int mb = i & 0xFF;

			int dr = Math.abs(mr - r);
			int dg = Math.abs(mg - g);
			int db = Math.abs(mb - b);

			int dist = dr + dg + db;

			if (dist < closestDist) {
				if (dist == 0) {
					return i;
				}
				closestDist = dist;
				closest = i;
			}
		}
		return closest;
	}
}
