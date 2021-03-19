package net.querz.mcmapviewer;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.querz.mcmapviewer.map.MapColor;
import net.querz.mcmapviewer.map.MapView;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public final class DialogHelper {

	private DialogHelper() {}

	public static void openDirectory(Stage primaryStage, FileView fileView) {
		String savesDir = getMCSavesDir();
		File file = createDirectoryChooser(savesDir).showDialog(primaryStage);
		if (file != null && file.isDirectory()) {
			File[] files = file.listFiles((dir, name) -> name.matches("^map_\\d+\\.dat$"));
			if (files != null && files.length > 0) {
				fileView.setDirectory(file);
			}
		}
	}

	public static void importImage(Stage primaryStage, MapView mapView) {
		File file = createFileChooser(null, new FileChooser.ExtensionFilter("*.png, *.jpg Files", "*.png", "*.jpg")).showOpenDialog(primaryStage);
		if (file != null) {
			try {
				BufferedImage bufImg = ImageIO.read(file);

				bufImg = scaleImage(bufImg, MapView.IMAGE_WIDTH, MapView.IMAGE_HEIGHT);

				Image img = SwingFXUtils.toFXImage(bufImg, null);
				int[] pixels = new int[MapView.IMAGE_WIDTH * MapView.IMAGE_HEIGHT];
				img.getPixelReader().getPixels(0, 0, MapView.IMAGE_WIDTH, MapView.IMAGE_HEIGHT, PixelFormat.getIntArgbPreInstance(), pixels, 0, MapView.IMAGE_WIDTH);

				byte[] closest = new byte[pixels.length];
				for (int i = 0; i < pixels.length; i++) {
					closest[i] = (byte) MapColor.getClosestColorID(pixels[i]);
				}

				mapView.setImageData(closest);
				mapView.update();



			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static DirectoryChooser createDirectoryChooser(String initialDirectory) {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		if (initialDirectory != null) {
			directoryChooser.setInitialDirectory(new File(initialDirectory));
		}
		return directoryChooser;
	}

	private static FileChooser createFileChooser(String initialDirectory, FileChooser.ExtensionFilter filter) {
		FileChooser fileChooser = new FileChooser();
		if (filter != null) {
			fileChooser.getExtensionFilters().add(filter);
		}
		if (initialDirectory != null) {
			fileChooser.setInitialDirectory(new File(initialDirectory));
		}
		return fileChooser;
	}

	public static String getMCSavesDir() {
		String appData = getMCDir();
		File saves;
		if (appData == null || !(saves = new File(appData, "saves")).exists()) {
			return getHomeDir();
		}
		return saves.getAbsolutePath();
	}

	public static String getMCDir() {
		String os = System.getProperty("os.name").toLowerCase();
		String appdataDir = null;
		if (os.contains("win")) {
			String env = System.getenv("AppData");
			File file = new File(env == null ? "" : env, ".minecraft");
			if (file.exists()) {
				appdataDir = file.getAbsolutePath();
			}
		} else {
			appdataDir = getHomeDir();
			appdataDir += "/Library/Application Support/minecraft";
		}
		return appdataDir;
	}

	public static String getHomeDir() {
		return System.getProperty("user.home");
	}

	public static BufferedImage scaleImage(BufferedImage before, int width, int height) {
		double w = before.getWidth();
		double h = before.getHeight();
		BufferedImage after = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		AffineTransform at = new AffineTransform();
		at.scale(width / w, height / h);
		AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		return scaleOp.filter(before, after);
	}
}
