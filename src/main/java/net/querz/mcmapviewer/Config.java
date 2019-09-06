package net.querz.mcmapviewer;

import java.io.File;

public final class Config {

	private Config() {}

	private static File mapDir = null;

	public static File getMapDir() {
		return mapDir;
	}

	public static void setMapDir(File mapDir) {
		Config.mapDir = mapDir;
	}
}
