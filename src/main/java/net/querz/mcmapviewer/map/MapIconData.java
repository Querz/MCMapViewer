package net.querz.mcmapviewer.map;

import javafx.scene.text.Text;
import net.querz.mcmapviewer.point.Point3i;
import net.querz.nbt.io.SNBTUtil;
import net.querz.nbt.tag.CompoundTag;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapIconData {

	private String name;
	private List<Text> textElements;
	private MapIcon color;
	private Point3i pos;

	public MapIconData(String name, String color, Point3i pos) {
		setName(name);
		this.color = MapIcon.byName(color);
		this.pos = pos;
	}

	public MapIconData(String name, MapIcon color, Point3i pos) {
		setName(name);
		this.color = color;
		this.pos = pos;
	}

	public List<Text> getTextElements() {
		return textElements;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		try {
			textElements = parseJson(name);
			if (textElements.size() == 0) {
				throw new Exception();
			}
			System.out.println("parsed json text");
			this.name = name;
		} catch (Exception e) {
			textElements = new ArrayList<>();
			textElements.add(TextColor.createText(name, "", false, false, false, false));
			System.out.println("did not parse json text");
			this.name = createJSONObjectName(name);
		}
	}

	private String createJSONObjectName(String name) {
		JSONObject stringName = new JSONObject();
		stringName.put("text", name);
		return stringName.toString();
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

	private List<Text> parseJson(String json) {
		JSONObject result = new JSONObject(json);

		List<Text> textElements = new ArrayList<>();

		addTextElement(result, textElements);

		if (result.has("extra")) {
			parseExtra(result.getJSONArray("extra"), textElements);
		}

		return textElements;
	}

	private void parseExtra(JSONArray array, List<Text> textElements) {
		for (Object entry : array) {
			addTextElement((JSONObject) entry, textElements);
			if (((JSONObject) entry).has("extra")) {
				parseExtra(((JSONObject) entry).getJSONArray("extra"), textElements);
			}
		}
	}

	private void addTextElement(JSONObject map, List<Text> textElements) {
		String text = map.getString("text");
		if (text == null) {
			return;
		}
		String color = map.has("color") ? map.getString("color") : "";
		boolean bold = map.has("bold") && map.getBoolean("bold");
		boolean italic = map.has("italic") && map.getBoolean("italic");
		boolean strikethrough = map.has("strikethrough") && map.getBoolean("strikethrough");
		boolean underline = map.has("underline") && map.getBoolean("underline");

		Text textElement = TextColor.createText(text, color, bold, italic, strikethrough, underline);
		textElements.add(textElement);
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
		try {
			System.out.println("saving banner pos: " + SNBTUtil.toSNBT(pos));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return icon;
	}
}
