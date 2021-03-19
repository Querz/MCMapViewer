package net.querz.mcmapviewer.map;

import javafx.scene.paint.Color;
import java.util.HashMap;
import java.util.Map;

public enum MapColor {

	NONE(0, 0x00000000),
	GRASS(4, 0xFF7FB238),
	SAND(8, 0xFFF7E9A3),
	WOOL(12, 0xFFC7C7C7),
	FIRE(16, 0xFFFF0000),
	ICE(20, 0xFFA0A0FF),
	METAL(24, 0xFFA7A7A7),
	PLANT(28, 0xFF007C00),
	SNOW(32, 0xFFFFFFFF),
	CLAY(36, 0xFFA4A8B8),
	DIRT(40, 0xFF976D4D),
	STONE(44, 0xFF707070),
	WATER(48, 0xFF4040FF),
	WOOD(52, 0xFF8F7748),
	QUARTZ(56, 0xFFFFFCF5),
	COLOR_ORANGE(60, 0xFFD87F33),
	COLOR_MAGENTA(64, 0xFFB24CD8),
	COLOR_LIGHT_BLUE(68, 0xFF6699D8),
	COLOR_YELLOW(72, 0xFFE5E533),
	COLOR_LIGHT_GREEN(76, 0xFF7FCC19),
	COLOR_PINK(80, 0xFFF27FA5),
	COLOR_GRAY(84, 0xFF4C4C4C),
	COLOR_LIGHT_GRAY(88, 0xFF999999),
	COLOR_CYAN(92, 0xFF4C7F99),
	COLOR_PURPLE(96, 0xFF7F3FB2),
	COLOR_BLUE(100, 0xFF334CB2),
	COLOR_BROWN(104, 0xFF664C33),
	COLOR_GREEN(108, 0xFF667F33),
	COLOR_RED(112, 0xFF993333),
	COLOR_BLACK(116, 0xFF191919),
	GOLD(120, 0xFFFAEE4D),
	DIAMOND(124, 0xFF5CDBD5),
	LAPIS(128, 0xFF4A80FF),
	EMERALD(132, 0xFF00D93A),
	PODZOL(136, 0xFF815631),
	NETHER(140, 0xFF700200),
	TERRACOTTA_WHITE(144, 0xFFD1B1A1),
	TERRACOTTA_ORANGE(148, 0xFF9F5224),
	TERRACOTTA_MAGENTA(152, 0xFF95576C),
	TERRACOTTA_LIGHT_BLUE(156, 0xFF706C8A),
	TERRACOTTA_YELLOW(160, 0xFFBA8524),
	TERRACOTTA_LIGHT_GREEN(164, 0xFF677535),
	TERRACOTTA_PINK(168, 0xFFA04D4E),
	TERRACOTTA_GRAY(172, 0xFF392923),
	TERRACOTTA_LIGHT_GRAY(176, 0xFF876B62),
	TERRACOTTA_CYAN(180, 0xFF575C5C),
	TERRACOTTA_PURPLE(184, 0xFF7A4958),
	TERRACOTTA_BLUE(188, 0xFF4C3E5C),
	TERRACOTTA_BROWN(192, 0xFF4C3223),
	TERRACOTTA_GREEN(196, 0xFF4C522A),
	TERRACOTTA_RED(200, 0xFF8E3C2E),
	TERRACOTTA_BLACK(204, 0xFF251610),
	CRIMSON_NYLIUM(208, 0xFFBD3031),
	CRIMSON_STEM(212, 0xFF943F61),
	CRIMSON_HYPHAE(216, 0xFF5C191D),
	WARPED_NYLIUM(220, 0xFF167E86),
	WARPED_STEM(224, 0xFF3A8E8C),
	WARPED_HYPHAE(228, 0xFF562C3E),
	WARPED_WART_BLOCK(232, 0xFF14B485);

	private int id;
	private int color;

	private static Map<Integer, Integer> colors = new HashMap<>();
	private static Map<Integer, Integer> ids = new HashMap<>();
	private static Map<Integer, Color> javaFXColors = new HashMap<>();

	private static final int[] mul = new int[]{180, 220, 255, 135};

	static {
		for (MapColor mc : MapColor.values()) {
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
				ids.put(newC, newId);
				javaFXColors.put(newId, new Color((float) newR / 255f, (float) newG / 255f, (float) newB / 255f, (float) a / 255f));
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

	public static int getClosestColorID(int color) {
		return ids.get(findClosestColor(color));
	}

	public static int findClosestColor(int color) {
		if (color >> 24 == 0) {
			return MapColor.NONE.color;
		}
		int r = color >> 16 & 0xFF;
		int g = color >> 8 & 0xFF;
		int b = color & 0xFF;

		int closest = 0xFFFFFFFF;
		double closestDist = Integer.MAX_VALUE;

		for (int i : colors.values()) {
			if (i >> 24 == 0) {
				continue;
			}
			int mr = i >> 16 & 0xFF;
			int mg = i >> 8 & 0xFF;
			int mb = i & 0xFF;

			double dist = Math.pow((mr - r) * 0.3, 2) + Math.pow((mg - g) * 0.59, 2) + Math.pow((mb - b) * 0.11, 2);

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
