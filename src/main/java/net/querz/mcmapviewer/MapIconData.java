package net.querz.mcmapviewer;

import javafx.scene.text.Text;
import net.querz.nbt.CompoundTag;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapIconData {

	private String name;
	private List<Text> textElements;
	private MapIcon color;
	private Point3i pos;

	private static ScriptEngine engine;

	static {
		ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
		engine = scriptEngineManager.getEngineByName("javascript");
	}

	public MapIconData(String name, String color, Point3i pos) {
		setName(name);
		this.color = MapIcon.byName(color);
		this.pos = pos;
	}

	public List<Text> getTextElements() {
		return textElements;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		try {
			textElements = parseJson(name);
			if (textElements.size() == 0) {
				throw new Exception();
			}
			System.out.println("parsed json text");
		} catch (Exception e) {
			textElements = new ArrayList<>();
			textElements.add(TextColor.createText(name, "", false, false, false, false));
			System.out.println("did not parse json text");
		}
	}

	public MapIcon getColor() {
		return color;
	}

	public void setIcon(MapIcon color) {
		this.color = color;
	}

	public Point3i getPos() {
		return pos;
	}

	public void setPos(Point3i pos) {
		this.pos = pos;
	}

	@SuppressWarnings("unchecked")
	private List<Text> parseJson(String json) throws Exception {
		String script = "Java.asJSONCompatible(" + json + ")";
		Object result = engine.eval(script);
		if (!(result instanceof Map)) {
			throw new IOException("could not parse json");
		}

		List<Text> textElements = new ArrayList<>();

		Map<String, Object> map = (Map<String, Object>) result;

		addTextElement(map, textElements);

		if (map.containsKey("extra")) {
			parseExtra(map.get("extra"), textElements);
		}

		return textElements;
	}

	@SuppressWarnings("unchecked")
	private void parseExtra(Object object, List<Text> textElements) {
		for (Map<String, Object> entry : (List<Map<String, Object>>) object) {
			addTextElement(entry, textElements);
			if (entry.containsKey("extra")) {
				parseExtra(entry.get("extra"), textElements);
			}
		}
	}

	private void addTextElement(Map<String, Object> map, List<Text> textElements) {
		String text = (String) map.get("text");
		if (text == null) {
			return;
		}
		String color = (String) map.getOrDefault("color", "");
		boolean bold = (boolean) map.getOrDefault("bold", false);
		boolean italic = (boolean) map.getOrDefault("italic", false);
		boolean strikethrough = (boolean) map.getOrDefault("strikethrough", false);
		boolean underline = (boolean) map.getOrDefault("underline", false);



		Text textELement = TextColor.createText(text, color, bold, italic, strikethrough, underline);
		textElements.add(textELement);
	}

	public CompoundTag toTag() {
		CompoundTag icon = new CompoundTag();

		icon.putString("Color", color.getName());
		icon.putString("Name", name);
		CompoundTag pos = new CompoundTag();
		pos.putInt("X", this.pos.getX());
		pos.putInt("Y", this.pos.getY());
		pos.putInt("Z", this.pos.getZ());
		icon.put("Pos", pos);
		return icon;
	}
}
