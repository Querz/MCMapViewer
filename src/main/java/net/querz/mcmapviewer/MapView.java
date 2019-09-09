package net.querz.mcmapviewer;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import net.querz.nbt.CompoundTag;
import net.querz.nbt.ListTag;
import net.querz.nbt.NBTUtil;
import net.querz.nbt.Tag;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class MapView extends StackPane {

	public static final int SCALE = 4;
	public static final int IMAGE_WIDTH = 128;
	public static final int IMAGE_HEIGHT = 128;

	private Canvas background;
	private Canvas canvas;
	private GraphicsContext context;
	private Pane bannerOverlay;

	private File mapFile;
	private byte[] imageData;

	private IntegerProperty xCenter = new SimpleIntegerProperty();
	private IntegerProperty zCenter = new SimpleIntegerProperty();
	private ObjectProperty<Scale> scale = new SimpleObjectProperty<>();
	private ObjectProperty<Dimension> dimension = new SimpleObjectProperty<>();
	private BooleanProperty trackingPosition = new SimpleBooleanProperty();
	private BooleanProperty unlimitedTracking = new SimpleBooleanProperty();
	private BooleanProperty locked = new SimpleBooleanProperty();
	private List<BannerData> banners = new ArrayList<>();
	private List<FrameData> frames = new ArrayList<>();
	private CompoundTag root;

	public static final Color BANNER_TEXT_BACKGROUND = new Color(0.5019608f, 0.5019608f, 0.5019608f, 0.8f);


	public MapView(File mapFile) {
		background = new Canvas(IMAGE_WIDTH, IMAGE_HEIGHT);
		background.setScaleX(SCALE * 2);
		background.setScaleY(SCALE * 2);
		background.setTranslateX(IMAGE_WIDTH * SCALE / 2.0);
		background.setTranslateY(IMAGE_HEIGHT * SCALE / 2.0);
		canvas = new Canvas(IMAGE_WIDTH, IMAGE_HEIGHT);
		canvas.setScaleX(SCALE);
		canvas.setScaleY(SCALE);
		bannerOverlay = new Pane();
		bannerOverlay.setMinWidth(IMAGE_WIDTH * SCALE);
		bannerOverlay.setMinHeight(IMAGE_HEIGHT * SCALE);
		this.mapFile = mapFile;
		context = canvas.getGraphicsContext2D();
		try {
			readFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Group overlayGroup = new Group();
		overlayGroup.getChildren().add(bannerOverlay);
		getChildren().addAll(background, canvas, overlayGroup);

		Image image = FileHelper.getIconFromResources("map_background");
		GraphicsContext backgroundContext = background.getGraphicsContext2D();
		PixelReader pr = image.getPixelReader();
		PixelWriter pw = backgroundContext.getPixelWriter();
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				Color color = pr.getColor(x, y);
				if (color.isOpaque()) {
					pw.setColor(x, y, color);
				}
			}
		}
//		backgroundContext.drawImage(image, 0, 0);

//		update();

		scale.addListener(s -> System.out.println("scale changed to " + s));
	}

	private void readFile() throws IOException {
		Tag<?> tag = NBTUtil.readTag(mapFile);
		if (!(tag instanceof CompoundTag)) {
			throw new IOException("expected root to be CompoundTag, got " + (tag == null ? "null" : tag.getClass().getSimpleName()));
		}

		root = (CompoundTag) tag;
		System.out.println(root);
		CompoundTag data = catchClassCastException(() -> root.getCompoundTag("data"));
		if (data == null) {
			throw new IOException("unable to parse data tag");
		}

		ListTag<CompoundTag> banners = catchClassCastException(() -> data.getListTag("banners").asCompoundTagList());
		if (banners != null) {
			for (CompoundTag banner : banners) {
				BannerData bannerData = new BannerData(banner.getString("Name"), banner.getString("Color"), parsePos(banner.getCompoundTag("Pos")));
				this.banners.add(bannerData);
			}
		}

		ListTag<CompoundTag> frames = catchClassCastException(() -> data.getListTag("frames").asCompoundTagList());
		if (frames != null) {
			for (CompoundTag frame : frames) {
				FrameData frameData = new FrameData(frame.getInt("EntityId"), frame.getInt("Rotation"), parsePos(frame.getCompoundTag("Pos")));
				this.frames.add(frameData);
			}
		}

		locked.setValue(data.getBoolean("locked"));
		scale.setValue(Scale.byId(data.getByte("scale")));
		trackingPosition.setValue(data.getBoolean("trackingPosition"));
		unlimitedTracking.setValue(data.getBoolean("unlimitedTracking"));
		xCenter.setValue(data.getInt("xCenter"));
		zCenter.setValue(data.getInt("zCenter"));
		dimension.setValue(Dimension.byId(data.getInt("dimension")));
		imageData = data.getByteArray("colors");
		if (imageData.length != IMAGE_WIDTH * IMAGE_HEIGHT) {
			imageData = new byte[IMAGE_WIDTH * IMAGE_HEIGHT];
		}
	}

	private void writeFile() throws IOException {
		CompoundTag data = root.getCompoundTag("data");
		data.putBoolean("locked", locked.getValue());
		data.putByte("scale", scale.getValue().getId());
		data.putBoolean("trackingPosition", trackingPosition.getValue());
		data.putBoolean("unlimitedTracking", unlimitedTracking.getValue());
		data.putInt("xCenter", xCenter.getValue());
		data.putInt("zCenter", zCenter.getValue());
		data.putInt("dimension", dimension.getValue().getId());
		data.putByteArray("colors", imageData);

		NBTUtil.writeTag(root, mapFile);
	}

	public void update() {

		long start = System.currentTimeMillis();

		// set background
		context.clearRect(0, 0, getWidth(), getHeight());
		PixelWriter pw = context.getPixelWriter();

		for (int i = 0; i < imageData.length; i++) {
			int x = i % IMAGE_WIDTH;
			int y = (i - x) / IMAGE_WIDTH;
			pw.setArgb(x, y, MapColor.getColor(imageData[i] & 0xFF));
		}

		for (BannerData banner : banners) {
			Point3i pos = banner.getPos().mod(IMAGE_WIDTH, Integer.MAX_VALUE, IMAGE_HEIGHT);
			Label label = new Label(banner.getName());
			// do not display label until we know its height and width
			label.setVisible(false);
			label.setTextFill(Color.WHITE);
			label.setBackground(new Background(new BackgroundFill(BANNER_TEXT_BACKGROUND, new CornerRadii(0), new Insets(0))));
			bannerOverlay.getChildren().add(label);

			// run this later once we have a height and a width
			Platform.runLater(() -> {
				double xOffset = pos.getX() * SCALE - label.getWidth() / 2.0;
				double zOffset = pos.getZ() * SCALE - label.getHeight() / 2.0;

				// the label should always appear inside of the map
				if (xOffset < 0) {
					xOffset = 0;
				}
				if (xOffset > IMAGE_WIDTH * SCALE - label.getWidth() / 2.0) {
					xOffset = IMAGE_WIDTH * SCALE - label.getWidth() / 2.0;
				}
				if (zOffset < 0) {
					zOffset = 0;
				}
				if (zOffset > IMAGE_HEIGHT * SCALE - label.getHeight() / 2.0) {
					zOffset = IMAGE_HEIGHT * SCALE - label.getHeight() / 2.0;
				}

				label.setTranslateX(xOffset);
				label.setTranslateY(zOffset);

				label.setVisible(true);
			});
		}
	}

	public void save() {
		try {
			writeFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private <T> T catchClassCastException(Supplier<T> s) {
		try {
			return s.get();
		} catch (ClassCastException ex) {
			System.out.println("error parsing value: " + ex.getMessage());
			return null;
		}
	}

	private Point3i parsePos(CompoundTag posTag) {
		int x = posTag.getInt("X");
		int y = posTag.getInt("Y");
		int z = posTag.getInt("Z");
		return new Point3i(x, y, z);
	}

	public Scale getScale() {
		return scale.get();
	}

	public ObjectProperty<Scale> scaleProperty() {
		return scale;
	}

	public void setScale(Scale scale) {
		this.scale.set(scale);
	}

	public Dimension getDimension() {
		return dimension.get();
	}

	public ObjectProperty<Dimension> dimensionProperty() {
		return dimension;
	}

	public void setDimension(Dimension dimension) {
		this.dimension.set(dimension);
	}

	public boolean isTrackingPosition() {
		return trackingPosition.get();
	}

	public BooleanProperty trackingPositionProperty() {
		return trackingPosition;
	}

	public void setTrackingPosition(boolean trackingPosition) {
		this.trackingPosition.set(trackingPosition);
	}

	public boolean isUnlimitedTracking() {
		return unlimitedTracking.get();
	}

	public BooleanProperty unlimitedTrackingProperty() {
		return unlimitedTracking;
	}

	public void setUnlimitedTracking(boolean unlimitedTracking) {
		this.unlimitedTracking.set(unlimitedTracking);
	}

	public boolean isLocked() {
		return locked.get();
	}

	public BooleanProperty lockedProperty() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked.set(locked);
	}

	public int getxCenter() {
		return xCenter.get();
	}

	public IntegerProperty xCenterProperty() {
		return xCenter;
	}

	public void setxCenter(int xCenter) {
		this.xCenter.set(xCenter);
	}

	public int getzCenter() {
		return zCenter.get();
	}

	public IntegerProperty zCenterProperty() {
		return zCenter;
	}

	public void setzCenter(int zCenter) {
		this.zCenter.set(zCenter);
	}
}
