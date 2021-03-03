package net.querz.mcmapviewer.map;

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
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import net.querz.mcmapviewer.EditableFile;
import net.querz.mcmapviewer.InfoPanel;
import net.querz.mcmapviewer.io.FileHelper;
import net.querz.mcmapviewer.point.Point2i;
import net.querz.mcmapviewer.point.Point3i;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.IntTag;
import net.querz.nbt.tag.ListTag;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.tag.StringTag;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class MapView extends StackPane {

	public static final int SCALE = 3;
	public static final int IMAGE_WIDTH = 128;
	public static final int IMAGE_HEIGHT = 128;

	private Canvas background;
	private Canvas canvas;
	private Pane overlay;

	private CompoundTag root;
	private EditableFile mapFile;
	private byte[] imageData;

	private static Image backgroundImage = FileHelper.getIconFromResources("map_background");

	private IntegerProperty dataVersion = new SimpleIntegerProperty();
	private IntegerProperty xCenter = new SimpleIntegerProperty();
	private IntegerProperty zCenter = new SimpleIntegerProperty();
	private ObjectProperty<Scale> scale = new SimpleObjectProperty<>();
	private ObjectProperty<Dimension> dimension = new SimpleObjectProperty<>();
	private BooleanProperty trackingPosition = new SimpleBooleanProperty();
	private BooleanProperty unlimitedTracking = new SimpleBooleanProperty();
	private BooleanProperty locked = new SimpleBooleanProperty();
	private List<MapIconData> banners = new ArrayList<>();
	private List<FrameData> frames = new ArrayList<>();

	private InfoPanel infoPanel;

	private MapIconView draggedIcon;
	private Point2i draggedOffsetOnIcon;
	private static final DataFormat ICON_DATA_FORMAT = new DataFormat("mcmap-icon");

	public static final Color BANNER_TEXT_BACKGROUND = new Color(0.1f, 0.1f, 0.1f, 0.8f);

	public MapView() {
		getStyleClass().add("map-view");

		// background must be 3 pixels larger on all sides
		background = new Canvas(IMAGE_WIDTH * SCALE + 6 * SCALE * 2, IMAGE_HEIGHT * SCALE + 6 * SCALE * 2);

		canvas = new Canvas(IMAGE_WIDTH * SCALE, IMAGE_HEIGHT * SCALE);

		overlay = new Pane();
		overlay.setMinWidth(IMAGE_WIDTH * SCALE + 6 * SCALE * 2);
		overlay.setMinHeight(IMAGE_HEIGHT * SCALE + 6 * SCALE * 2);
		overlay.setMaxWidth(IMAGE_WIDTH * SCALE + 6 * SCALE * 2);
		overlay.setMaxHeight(IMAGE_HEIGHT * SCALE + 6 * SCALE * 2);

		overlay.setOnMouseClicked(this::onMouseClicked);
		overlay.setOnDragDetected(this::onDragDetected);
		overlay.setOnDragOver(this::onDragOver);
		overlay.setOnDragDone(this::onDragDone);
		overlay.setOnDragDropped(this::onDragDropped);

		getChildren().addAll(background, canvas, overlay);

		updateBackground();
	}

	public void setInfoPanel(InfoPanel infoPanel) {
		this.infoPanel = infoPanel;
	}

	private void onDragDetected(MouseEvent e) {
		if (e.getButton() != MouseButton.PRIMARY) {
			return;
		}
		System.out.println(e.getTarget().getClass());

		if (e.getTarget() instanceof MapIconView) {
			System.out.println("dragging icon");
			draggedIcon = ((MapIconView) e.getTarget());
			draggedOffsetOnIcon = new Point2i((int) (e.getX() - draggedIcon.getTranslateX()), (int) (e.getY() - draggedIcon.getTranslateY()));
			System.out.println("d: " + draggedOffsetOnIcon);

			Dragboard db = startDragAndDrop(TransferMode.MOVE);
			db.setDragView(new WritableImage(1, 1));
			ClipboardContent cbc = new ClipboardContent();
			cbc.put(ICON_DATA_FORMAT, true);
			db.setContent(cbc);
			e.consume();
			mapFile.setEdited(true);
		}
	}

	private void onDragOver(DragEvent e) {
		if (e.getDragboard().hasContent(ICON_DATA_FORMAT)) {
			e.acceptTransferModes(TransferMode.MOVE);
			if (draggedIcon != null) {
				Point2i pos = new Point2i((int) e.getX() - 6 * SCALE, (int) e.getY() - 6 * SCALE);
				draggedIcon.translate(pos, new Point2i(IMAGE_WIDTH * SCALE, IMAGE_HEIGHT * SCALE), new Point2i(6 * SCALE, 6 * SCALE), SCALE);

				Point2i world = getPosInWorld(e.getX(), e.getY());
				System.out.println(world);
				if (world != null) {
					draggedIcon.getData().setPos(world.toPoint3i());
				}
			}
		}
	}

	private void onDragDone(DragEvent e) {
		System.out.println("drag done");
		if (e.getDragboard().hasContent(ICON_DATA_FORMAT)) {
			System.out.println("drag done really");
			e.acceptTransferModes(TransferMode.MOVE);
			draggedIcon = null;
			draggedOffsetOnIcon = null;
			e.consume();
		}
	}

	private void onDragDropped(DragEvent e) {
		System.out.println("drag dropped");
		if (e.getDragboard().hasContent(ICON_DATA_FORMAT)) {
			System.out.println("drag dropped really");
			e.acceptTransferModes(TransferMode.MOVE);
			draggedIcon = null;
			draggedOffsetOnIcon = null;
			e.consume();
		}
	}

	private void onMouseClicked(MouseEvent e) {
		System.out.println("click");

		if (e.getButton() != MouseButton.SECONDARY) {
			return;
		}

		if (mapFile == null) {
			return;
		}

		if (e.getTarget() instanceof MapIconView) {
			// clicked an icon
			System.out.println("clicked icon: " + ((MapIconView) e.getTarget()).getData().getColor() + " / " + ((MapIconView) e.getTarget()).getData().getName() + " / " + ((MapIconView) e.getTarget()).getData().getPos());

			GridPane iconGrid = new GridPane();
			int x = 0, y = 0;
			for (MapIcon mapIcon : MapIcon.values()) {
				if (!mapIcon.isBanner()) {
					continue;
				}
				Label iconOption = new Label("", new ImageView(mapIcon.getIcon()));
				iconOption.setOnMouseClicked(a -> {
					((MapIconView) e.getTarget()).setIcon(mapIcon);
					overlay.getChildren().remove(iconGrid);
				});
				iconGrid.add(iconOption, x, y);
				x++;
				if (x > 6) {
					x = 0;
					y++;
				}
			}

			Label iconOption = new Label("", new ImageView(MapIcon.RED_X.getIcon()));
			iconOption.setOnMouseClicked(a -> {
				banners.remove(((MapIconView) e.getTarget()).getData());
				overlay.getChildren().remove(iconGrid);
				overlay.getChildren().remove(((MapIconView) e.getTarget()).getMapLabelView());
				overlay.getChildren().remove(e.getTarget());
			});
			iconGrid.add(iconOption, x, y);

			iconGrid.widthProperty().addListener((i, o, n) -> {
				double translateX = e.getX() - n.doubleValue() / 2;
				iconGrid.setTranslateX(translateX > 0 ? translateX : 0);
			});
			iconGrid.heightProperty().addListener((i, o, n) -> {
				double translateY = e.getY() - n.doubleValue() / 2;
				iconGrid.setTranslateY(translateY > 0 ? translateY : 0);
			});
			iconGrid.setBackground(new Background(new BackgroundFill(BANNER_TEXT_BACKGROUND, new CornerRadii(0), new Insets(0))));

			overlay.getChildren().add(iconGrid);

			iconGrid.setOnMouseExited(a -> Platform.runLater(() -> overlay.getChildren().remove(iconGrid)));
			mapFile.setEdited(true);

		} else if (e.getTarget() instanceof Text && ((Text) e.getTarget()).getParent() instanceof MapLabelView || e.getTarget() instanceof  MapLabelView) {
			MapLabelView parent = e.getTarget() instanceof MapLabelView ? (MapLabelView) e.getTarget() : (MapLabelView) ((Text) e.getTarget()).getParent();

			TextField text = new TextField(parent.getData().getName());
			text.setOnKeyReleased(k -> {
				switch (k.getCode()) {
					case ENTER:
						parent.getData().setName(text.getText());
						parent.update();
					case ESCAPE:
						overlay.getChildren().remove(text);
				}
			});
			text.setMinSize(200, 0);
			text.setTranslateX(parent.getTranslateX());
			text.setTranslateY(parent.getTranslateY());
			text.setPrefSize(parent.getWidth(), parent.getHeight());
			text.setPadding(new Insets(0, 1, 0, 1));
			text.setBackground(new Background(new BackgroundFill(BANNER_TEXT_BACKGROUND, new CornerRadii(0), new Insets(0))));
			text.setFont(Font.font("Monospaced", 16));
			text.setStyle("-fx-text-fill: white;");
			text.setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
			overlay.getChildren().add(text);
			Platform.runLater(text::requestFocus);

			text.widthProperty().addListener((i, o, n) -> parent.getIcon().translateLabel(text, parent.getIcon().getOffset(), text.getWidth(), text.getHeight()));
			mapFile.setEdited(true);
		} else if (e.getTarget() == overlay) {
			Point2i posInWorld = getPosInWorld(e.getX(), e.getY());
			if (posInWorld == null) {
				return;
			}
			// create new banner
			MapIconData mapIconData = new MapIconData("Banner", MapIcon.BANNER_WHITE, posInWorld.toPoint3i());
			this.banners.add(mapIconData);
			update();
			mapFile.setEdited(true);
		}

		System.out.println(e.getTarget().getClass().getName());
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
				imgPos.getZ() * (int) (Math.pow(2, getScale().getId())));
		return mapPos
				.add(getxCenter(), getzCenter())
				.sub((int) (IMAGE_WIDTH * Math.pow(2, getScale().getId())) / 2,
					 (int) (IMAGE_HEIGHT * Math.pow(2, getScale().getId())) / 2);
	}

	public void loadMapFile(EditableFile mapFile) {
		infoPanel.setLoading(true);
		this.mapFile = mapFile;
		clear();
		try {
			readFile();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			infoPanel.setLoading(false);
		}
		update();
	}

	private void clear() {
		dataVersion.setValue(0);
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



		NamedTag tag = NBTUtil.read(mapFile.getFile());
		if (tag == null || !(tag.getTag() instanceof CompoundTag)) {
			throw new IOException("expected root to be CompoundTag, got " + (tag == null ? "null" : tag.getClass().getSimpleName()));
		}

		root = (CompoundTag) tag.getTag();
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

		dataVersion.setValue(root.getInt("DataVersion"));
		locked.setValue(data.getBoolean("locked"));
		scale.setValue(Scale.byId(data.getByte("scale")));
		trackingPosition.setValue(data.getBoolean("trackingPosition"));
		unlimitedTracking.setValue(data.getBoolean("unlimitedTracking"));
		xCenter.setValue(data.getInt("xCenter"));
		zCenter.setValue(data.getInt("zCenter"));
		if (data.get("dimension").getID() == IntTag.ID) {
			dimension.setValue(Dimension.byId(data.getInt("dimension")));
		} else if (data.get("dimension").getID() == StringTag.ID) {
			dimension.setValue(Dimension.byTextId(data.getString("dimension")));
		}
		imageData = data.getByteArray("colors");
		if (imageData.length != IMAGE_WIDTH * IMAGE_HEIGHT) {
			imageData = new byte[IMAGE_WIDTH * IMAGE_HEIGHT];
		}
	}

	public void writeFile() throws IOException {
		CompoundTag data = root.getCompoundTag("data");
		root.putInt("DataVersion", dataVersion.getValue());
		data.putBoolean("locked", locked.getValue());
		data.putByte("scale", scale.getValue().getId());
		data.putBoolean("trackingPosition", trackingPosition.getValue());
		data.putBoolean("unlimitedTracking", unlimitedTracking.getValue());
		// TODO: look up which dataversion supports string ids and which int ids
		data.putString("dimension", dimension.getValue().getTextID());
		data.putByteArray("colors", imageData);

		Point2i offset = new Point2i(xCenter.get() - data.getInt("xCenter"), zCenter.get() - data.getInt("zCenter"));

		ListTag<CompoundTag> icons = new ListTag<>(CompoundTag.class);
		banners.forEach(b -> icons.add(b.toTag(offset)));
		data.put("banners", icons);

		data.putInt("xCenter", xCenter.getValue());
		data.putInt("zCenter", zCenter.getValue());

		NBTUtil.write(root, mapFile.getFile());

		mapFile.setEdited(false);
	}

	public void update() {
		overlay.getChildren().clear();

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

		// calculate the location of the top-left corner of the map
		Point2i nil = new Point2i(xCenter.getValue(), zCenter.getValue())
				.sub(IMAGE_WIDTH * (int) Math.pow(2, scale.getValue().getId()) / 2, IMAGE_HEIGHT * (int) Math.pow(2, scale.getValue().getId()) / 2);

		for (MapIconData banner : banners) {

			Point2i pos = banner.getPos().toPoint2i()
					.sub(nil) // pos of banner on normalized map
					.div((int) Math.pow(2, scale.getValue().getId())) // scale map to image
					.mul(SCALE); // scale image to display

			MapLabelView label = new MapLabelView(new SimpleObjectProperty<>(banner));
			// do not display label until we know its height and width
			label.setVisible(false);
			label.setBackground(new Background(new BackgroundFill(BANNER_TEXT_BACKGROUND, new CornerRadii(0), new Insets(0))));
			label.setPadding(new Insets(0, 1, 0, 1));

			MapIconView icon = new MapIconView(new SimpleObjectProperty<>(banner), label);
			label.assignIcon(icon);
			icon.setVisible(false);

			overlay.getChildren().addAll(icon, label);

			postUpdates.add(() -> {
				// center on icon in box

				icon.translate(pos, new Point2i(IMAGE_WIDTH * SCALE, IMAGE_HEIGHT * SCALE), new Point2i(6 * SCALE, 6 * SCALE), SCALE);
				icon.setVisible(true);
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
		Point3i pos = new Point3i(x, y, z);
		System.out.println("parsed pos: " + pos);
		return pos;
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

	public IntegerProperty dataVersionProperty() {
		return dataVersion;
	}

	public int getDataVersion() {
		return dataVersion.get();
	}

	public void setDataVersion(int dataVersion) {
		this.dataVersion.set(dataVersion);
	}

	public void showMapIcons(boolean show) {
		overlay.setVisible(show);
	}

	public EditableFile getMapFile() {
		return mapFile;
	}
}
