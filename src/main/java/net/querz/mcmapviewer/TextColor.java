package net.querz.mcmapviewer;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public enum TextColor {

	WHITE("white", 0xFFFFFF),
	GRAY("gray", 0xAAAAAA),
	DARK_GRAY("dark_gray", 0x555555),
	BLACK("black", 0x000000),
	DARK_BLUE("dark_blue", 0x0000AA),
	BLUE("blue", 0x5555FF),
	DARK_GREEN("dark_green", 0x00AA00),
	GREEN("green", 0x55FF55),
	DARK_AQUA("dark_aqua", 0x00AAAA),
	AQUA("aqua", 0x55FFFF),
	DARK_RED("dark_red", 0xAA0000),
	RED("red", 0xFF5555),
	DARK_PURPLE("dark_purple", 0xAA00AA),
	LIGHT_PURPLE("light_purple", 0xFF55FF),
	GOLD("gold", 0xFFAA00),
	YELLOW("yellow", 0xFFFF55);

	private String name;
	private Color color;

	TextColor(String name, int color) {
		this.name = name;

		this.color = new Color(
				(double) (color >> 16) / 255,
				(double) (color >> 8 & 0xFF) / 255,
				(double) (color & 0xFF) / 255,
				1
		);

	}

	public static TextColor byName(String name) {
		for (TextColor tc : values()) {
			if (tc.name.equals(name)) {
				return tc;
			}
		}
		return WHITE;
	}

	public static Text createText(String text, String color, boolean bold, boolean italic, boolean strikethrough, boolean underline) {
		System.out.printf("creating text: %s c:%s b:%s i:%s s:%s u:%s\n", text, color, bold, italic, strikethrough, underline);
		TextColor tc = byName(color);
		Text tx = new Text(text);
		tx.setFill(tc.color);
		tx.setStrikethrough(strikethrough);
		tx.setUnderline(underline);
		tx.setFont(Font.font("Monospaced", 16));
		if (bold) {
			tx.setStyle("-fx-font-weight: bold;");
		}
		if (italic) {
			tx.setStyle("-fx-font-style: italic;");
		}
		return tx;
	}
}
