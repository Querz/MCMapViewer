package net.querz.mcmapviewer;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MapIconData {

	private String name;
	private MapIcon color;
	private Point3i pos;

	private static ScriptEngine engine;

	static {
		ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
		engine = scriptEngineManager.getEngineByName("javascript");
	}

	public MapIconData(String name, String color, Point3i pos) {
		try {
			this.name = parseJson(name);
		} catch (Exception e) {
			this.name = name;
		}
		this.color = MapIcon.byName(color);
		this.pos = pos;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public MapIcon getColor() {
		return color;
	}

	public void setColor(MapIcon color) {
		this.color = color;
	}

	public Point3i getPos() {
		return pos;
	}

	public void setPos(Point3i pos) {
		this.pos = pos;
	}

	private String parseJson(String json) throws Exception {
		String script = "Java.asJSONCompatible(" + json + ")";
		Object result = engine.eval(script);
		if (!(result instanceof Map)) {
			throw new IOException("could not parse json");
		}

		StringBuilder sb = new StringBuilder();

		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) result;

		sb.append(map.get("text"));
		if (map.containsKey("extra")) {
			parseExtra(map.get("extra"), sb);
		}

		return sb.toString();
	}

	private void parseExtra(Object object, StringBuilder sb) {
		for (Map<String, Object> entry : (List<Map<String, Object>>) object) {
			sb.append(entry.get("text"));
			if (entry.containsKey("extra")) {
				parseExtra(entry.get("extra"), sb);
			}
		}
	}
}
