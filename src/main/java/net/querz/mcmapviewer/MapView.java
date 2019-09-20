package net.querz.mcmapviewer;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
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
	private Pane overlay;

	private CompoundTag root;
	private File mapFile;
	private byte[] imageData;

	private static Image backgroundImage = FileHelper.getIconFromResources("map_background");

	private IntegerProperty xCenter = new SimpleIntegerProperty();
	private IntegerProperty zCenter = new SimpleIntegerProperty();
	private ObjectProperty<Scale> scale = new SimpleObjectProperty<>();
	private ObjectProperty<Dimension> dimension = new SimpleObjectProperty<>();
	private BooleanProperty trackingPosition = new SimpleBooleanProperty();
	private BooleanProperty unlimitedTracking = new SimpleBooleanProperty();
	private BooleanProperty locked = new SimpleBooleanProperty();
	private List<MapIconData> banners = new ArrayList<>();
	private List<FrameData> frames = new ArrayList<>();

	public static final Color BANNER_TEXT_BACKGROUND = new Color(0.1f, 0.1f, 0.1f, 0.8f);

	public MapView(File mapFile) {
		// background must be 3 pixels larger on all sides
		background = new Canvas(IMAGE_WIDTH * SCALE + 6 * SCALE * 2, IMAGE_HEIGHT * SCALE + 6 * SCALE * 2);

		canvas = new Canvas(IMAGE_WIDTH * SCALE, IMAGE_HEIGHT * SCALE);

		overlay = new Pane();
		overlay.setMinWidth(IMAGE_WIDTH * SCALE + 6 * SCALE * 2);
		overlay.setMinHeight(IMAGE_HEIGHT * SCALE + 6 * SCALE * 2);

		overlay.setOnMouseClicked(this::onMouseClicked);

		getChildren().addAll(background, canvas, overlay);

		updateBackground();

		loadMapFile(mapFile);
	}

	private void onMouseClicked(MouseEvent e) {
		if (e.getButton() != MouseButton.PRIMARY) {
			return;
		}

		Point2i worldPos = getPosInWorld(e.getX(), e.getY());
		if (worldPos == null) {
			return;
		}

		System.out.println("worldPos: " + worldPos);

		if (e.getTarget() instanceof MapIconView) {
			// clicked an icon
			// TODO: open context menu to edit icon
			System.out.println("clicked icon: " + ((MapIconView) e.getTarget()).getData().getColor() + " / " + ((MapIconView) e.getTarget()).getData().getName() + " / " + ((MapIconView) e.getTarget()).getData().getPos());

			Point2i imgPos = getPosOnImg(e.getX(), e.getY());

			//TODO: when clicking the icon, make a temporary combobox appear where you can select
			//      the icon. when clicking the text, make a temporary textfield appear where you
			//      can edit the text of the map icon.
			// --> add combobox or textfield to overlay pane
			// --> keep track of combobox or textfield and remove them if this event is
			//     called again but it is not a click on the tracked combobox or textfield
		}
	}

	private Point2i getPosOnImg(double mouseX, double mouseY) {
		int imgX = (int) (mouseX - 3 * SCALE * 2) / SCALE; // skip 3 pixels of background image
		int imgY = (int) (mouseY - 3 * SCALE * 2) / SCALE;
		if (imgX < 0 || imgX > IMAGE_WIDTH || imgY < 0 || imgY > IMAGE_HEIGHT) {
			return null;
		}
		return new Point2i(imgX, imgY);
	}

	private Point2i getPosInWorld(double mouseX, double mouseY) {
		Point2i imgPos = getPosOnImg(mouseX, mouseY);
		if (imgPos == null) {
			return null;
		}
		Point2i mapPos = new Point2i(
				imgPos.getX() * (int) (Math.pow(2, getScale().getId())),
				imgPos.getY() * (int) (Math.pow(2, getScale().getId())));
		return mapPos
				.add(getxCenter(), getzCenter())
				.sub((int) (IMAGE_WIDTH * Math.pow(2, getScale().getId())) / 2,
					 (int) (IMAGE_HEIGHT * Math.pow(2, getScale().getId())) / 2);
	}

	public void loadMapFile(File mapFile) {
		this.mapFile = mapFile;
		clear();
		try {
			readFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		update();
	}

	private void clear() {
		imageData = null;
		xCenter.setValue(0);
		zCenter.setValue(0);
		scale.setValue(Scale.SCALE_0);
		dimension.setValue(Dimension.OVERWORLD);
		trackingPosition.setValue(false);
		unlimitedTracking.setValue(false);
		locked.setValue(false);
		banners.clear();
		frames.clear();
		root = null;
		canvas.getGraphicsContext2D().clearRect(0, 0, IMAGE_WIDTH * SCALE, IMAGE_HEIGHT * SCALE);
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
				MapIconData mapIconData = new MapIconData(banner.getString("Name"), banner.getString("Color"), parsePos(banner.getCompoundTag("Pos")));
				this.banners.add(mapIconData);
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
		// manually draw pixels as rectangles to avoid antialiasing
		GraphicsContext context = canvas.getGraphicsContext2D();
		context.clearRect(0, 0, IMAGE_WIDTH * SCALE, IMAGE_HEIGHT * SCALE);
		for (int i = 0; i < imageData.length; i++) {
			int x = i % IMAGE_WIDTH;
			int y = (i - x) / IMAGE_WIDTH;
			context.setFill(MapColor.getJavaFXColor(imageData[i] & 0xFF));
			context.fillRect(x * SCALE, y * SCALE, SCALE, SCALE);
		}

		List<Runnable> postUpdates = new ArrayList<>();

		for (MapIconData banner : banners) {

			double mapWidthInBlocks = IMAGE_WIDTH * Math.pow(2, scale.getValue().getId());
			double mapHeightInBlocks = IMAGE_HEIGHT * Math.pow(2, scale.getValue().getId());

			Point2i pos = banner.getPos().toPoint2i()
					.sub(xCenter.getValue(), zCenter.getValue()) // normalize banner position
					.add((int) (mapWidthInBlocks / 2), (int) (mapHeightInBlocks / 2)) // adjust to zero of image
					.mul(SCALE); // scale to image


			Label label = new Label(banner.getName());
			// do not display label until we know its height and width
			label.setVisible(false);
			label.setTextFill(Color.WHITE);
			label.setBackground(new Background(new BackgroundFill(BANNER_TEXT_BACKGROUND, new CornerRadii(0), new Insets(0))));
			label.setPadding(new Insets(0, 1, 0, 1));

			MapIconView icon = new MapIconView(new SimpleObjectProperty<>(banner));
			icon.setVisible(false);

			overlay.getChildren().addAll(icon, label);

			postUpdates.add(() -> {
				// center on icon in box
				Point2i iconPos = pos.sub((int) icon.getWidth() / 2, (int) icon.getHeight() / 2);

				if (iconPos.getX() + icon.getWidth() / 2 < 0) {
					iconPos.setX((int) -(icon.getWidth() / 2));
				} else if (iconPos.getX() > IMAGE_WIDTH * SCALE - icon.getWidth() / 2) {
					iconPos.setX((int) (IMAGE_WIDTH * SCALE - icon.getWidth() / 2));
				}
				if (iconPos.getY() + icon.getHeight() / 2 < 0) {
					iconPos.setY((int) -(icon.getHeight() / 2));
				} else if (iconPos.getY() > IMAGE_HEIGHT * SCALE - icon.getHeight() / 2) {
					iconPos.setY((int) (IMAGE_HEIGHT * SCALE - icon.getHeight() / 2));
				}

				iconPos = iconPos.add(6 * SCALE);

				icon.setTranslateX(iconPos.getX());
				icon.setTranslateY(iconPos.getY());

				icon.setVisible(true);

				Point2i labelPos = pos.sub((int) label.getWidth() / 2, (int) -(icon.getHeight() / 2));

				labelPos = labelPos.add(6 * SCALE);

				if (labelPos.getX() < 0) {
					labelPos.setX(0);
				} else if (labelPos.getX() > overlay.getWidth() - label.getWidth()) {
					labelPos.setX((int) (overlay.getWidth() - label.getWidth()));
				}
				if (labelPos.getY() < 0) {
					labelPos.setY(0);
				} else if (labelPos.getY() > overlay.getHeight() - label.getHeight()) {
					labelPos.setY((int) (overlay.getHeight() - label.getHeight()));
				}

				label.setTranslateX(labelPos.getX());
				label.setTranslateY(labelPos.getY());
				label.setVisible(true);
			});
		}

		// run this later once we have height and width of the label and icon
		Platform.runLater(() -> postUpdates.forEach(Runnable::run));
	}

	public void updateBackground() {
		GraphicsContext backgroundContext = background.getGraphicsContext2D();
		PixelReader pr = backgroundImage.getPixelReader();
		double bgSize = 6.0 / 64 * SCALE * 2 + SCALE * 2;
		// manually draw pixels as rectangles to avoid antialiasing
		for (int x = 0; x < 64; x++) {
			for (int y = 0; y < 64; y++) {
				Color color = pr.getColor(x, y);
				if (color.isOpaque()) {
					backgroundContext.setFill(color);
					backgroundContext.fillRect(x * bgSize, y * bgSize, Math.ceil(bgSize), Math.ceil(bgSize));
				}
			}
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
