package net.querz.mcmapviewer;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public enum BannerColor {

	WHITE("white", 0xffffff, 80, 0),
	ORANGE("orange", 0xffa500, 88, 0),
	MAGENTA("magenta", 0xff00ff, 96, 0),
	LIGHT_BLUE("light_blue", 0xadd8e6, 104, 0),
	YELLOW("yellow", 0xffff00, 112, 0),
	LIME("lime", 0x00ff00, 120, 0),
	PINK("pink", 0xffc0cb, 0, 8),
	GRAY("gray", 0x808080, 8, 8),
	LIGHT_GRAY("light_gray", 0xd3d3d3, 16, 8),
	CYAN("cyan", 0x00ffff, 24, 8),
	PURPLE("purple", 0x800080, 32, 8),
	BLUE("blue", 0x0000ff, 40, 8),
	BROWN("brown", 0xa52a2a, 48, 8),
	GREEN("green", 0x008000, 56, 8),
	RED("red", 0xff0000, 64, 8),
	BLACK("black", 0x000000, 72, 8),
	BURIED_TREASURE("red_x", 0xff0000, 80, 8),
	PLAYER("player", 0xffffff, 0, 0),
	FRAME("frame", 0x00ff00, 8, 0),
	RED_MARKER("red_marker", 0xff0000, 16, 0),
	BLUE_MARKER("blue_marker", 0x0000ff, 24, 0),
	TARGET_X("target_x", 0xffffff, 32, 0);
	//TODO: add missing icons

	private String name;
	private int color;
	private WritableImage icon;

	private static class Icons {

		private static Image icons;

		static {
			icons = FileHelper.getIconFromResources("map_icons");

			System.out.println(icons.getWidth() + " / " + icons.getHeight());

			try {
				ImageIO.write(SwingFXUtils.fromFXImage(icons, null), "png", new File("test-icons.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	BannerColor(String name, int color) {
		this.name = name;
		this.color = color;
	}

	BannerColor(String name, int color, int x, int y) {
		this.name = name;
		this.color = color;
		icon = new WritableImage(8, 8);
		PixelReader pr = Icons.icons.getPixelReader();
		PixelWriter pw = icon.getPixelWriter();
		for (int ix = 0; ix < 8; ix++) {
			for (int iy = 0; iy < 8; iy++) {
				pw.setArgb(ix, iy, pr.getArgb(ix + x, iy + y));
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

	public static BannerColor byName(String name) {
		for (BannerColor bc : values()) {
			if (bc.name.equals(name)) {
				return bc;
			}
		}
		throw new IllegalArgumentException("invalid banner color \"" + name + "\"");
	}
}
