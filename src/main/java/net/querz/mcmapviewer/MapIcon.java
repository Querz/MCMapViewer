package net.querz.mcmapviewer;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum MapIcon {

	PLAYER("player", 0xffffff, 0, 0),
	FRAME("frame", 0x00ff00, 8, 0),
	RED_MARKER("red_marker", 0xff0000, 16, 0),
	BLUE_MARKER("blue_marker", 0x0000ff, 24, 0),
	TARGET_X("target_x", 0xffffff, 32, 0),
	TARGET_POINT("target_point", 0xff0000, 40, 0),
	PLAYER_OFF_MAP("player_off_map", 0xffffff, 48, 0),
	PLAYER_OFF_LIMITS("player_off_limits", 0xffffff, 56, 0),
	MANSION("mansion", 0xa52a2a, 64, 0),
	MONUMENT("monument", 0x00ffff, 72, 0),
	BANNER_WHITE("white", 0xffffff, 80, 0, true),
	BANNER_ORANGE("orange", 0xffa500, 88, 0, true),
	BANNER_MAGENTA("magenta", 0xff00ff, 96, 0, true),
	BANNER_LIGHT_BLUE("light_blue", 0xadd8e6, 104, 0, true),
	BANNER_YELLOW("yellow", 0xffff00, 112, 0, true),
	BANNER_LIME("lime", 0x00ff00, 120, 0, true),
	BANNER_PINK("pink", 0xffc0cb, 0, 8, true),
	BANNER_GRAY("gray", 0x808080, 8, 8, true),
	BANNER_LIGHT_GRAY("light_gray", 0xd3d3d3, 16, 8, true),
	BANNER_CYAN("cyan", 0x00ffff, 24, 8, true),
	BANNER_PURPLE("purple", 0x800080, 32, 8, true),
	BANNER_BLUE("blue", 0x0000ff, 40, 8, true),
	BANNER_BROWN("brown", 0xa52a2a, 48, 8, true),
	BANNER_GREEN("green", 0x008000, 56, 8, true),
	BANNER_RED("red", 0xff0000, 64, 8, true),
	BANNER_BLACK("black", 0x000000, 72, 8, true),
	RED_X("red_x", 0xff0000, 80, 8);

	private String name;
	private int color;
	private WritableImage icon;
	private boolean banner;

	private static class Icons {
		private static Image icons = FileHelper.getIconFromResources("map_icons");
	}

	private static final Map<String, MapIcon> names = new HashMap<>();

	static {
		Arrays.stream(values()).forEach(v -> names.put(v.name, v));
	}

	MapIcon(String name, int color, int x, int y) {
		this(name, color, x, y, false);
	}

	MapIcon(String name, int color, int x, int y, boolean banner) {
		this.name = name;
		this.color = color;
		this.banner = banner;
		PixelReader pr = Icons.icons.getPixelReader();
		icon = new WritableImage(8 * MapView.SCALE, 8 * MapView.SCALE);
		PixelWriter pw = icon.getPixelWriter();

		// manually scale icons, because javafx is stupid and can't scale without antialiasing
		for (int ix = 0; ix < 8; ix++) {
			for (int iy = 0; iy < 8; iy++) {
				int pixel = pr.getArgb(ix + x, iy + y);
				for (int mx = 0; mx < MapView.SCALE; mx++) {
					for (int my = 0; my < MapView.SCALE; my++) {
						pw.setArgb(ix * MapView.SCALE + mx, iy * MapView.SCALE + my, pixel);
					}
				}
			}
		}
		try {
			new File("blah").mkdirs();
			ImageIO.write(SwingFXUtils.fromFXImage(icon, null), "png", new File("blah/test-" + name + ".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getColor() {
		return color;
	}

	public Image getIcon() {
		return icon;
	}

	public boolean isBanner() {
		return banner;
	}

	public String getName() {
		return name;
	}

	public static MapIcon byName(String name) {
		MapIcon mi = names.get(name);
		if (mi == null) {
			throw new IllegalArgumentException("invalid banner color \"" + name + "\"");
		}
		return mi;
	}
}
