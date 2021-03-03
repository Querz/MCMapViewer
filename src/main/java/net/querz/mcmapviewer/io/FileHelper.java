package net.querz.mcmapviewer.io;

import javafx.scene.image.Image;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class FileHelper {

	public static Image getIconFromResources(String name) {
		return new Image(Objects.requireNonNull(FileHelper.class.getClassLoader().getResourceAsStream(name + ".png")));
	}

	public static Attributes getManifestAttributes() throws IOException {
		String className = FileHelper.class.getSimpleName() + ".class";
		String classPath = FileHelper.class.getResource(className).toString();
		if (!classPath.startsWith("jar")) {
			throw new IOException("application not running in jar file");
		}
		URL url = new URL(classPath);
		JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
		Manifest manifest = jarConnection.getManifest();
		return manifest.getMainAttributes();
	}
}
