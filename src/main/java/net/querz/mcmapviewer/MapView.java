package net.querz.mcmapviewer;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
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

public class MapView extends Canvas {

	public static final int SCALE = 4;
	public static final int IMAGE_WIDTH = 128;
	public static final int IMAGE_HEIGHT = 128;

	private GraphicsContext context;
	private File mapFile;
	private byte[] imageData;
	private int id;
	private Point2i center;
	private byte scale;
	private int dimension;
	private boolean trackingPosition;
	private boolean unlimitedTracking;
	private boolean locked;
	private List<BannerData> banners = new ArrayList<>();
	private List<FrameData> frames = new ArrayList<>();


	public MapView(File mapFile) {
		super(IMAGE_WIDTH, IMAGE_HEIGHT);
		setScaleX(SCALE);
		setScaleY(SCALE);
		this.mapFile = mapFile;
		context = getGraphicsContext2D();
		setFocusTraversable(true);
		try {
			readFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		update();
	}

	private void readFile() throws IOException {
		Tag<?> tag = NBTUtil.readTag(mapFile);
		if (!(tag instanceof CompoundTag)) {
			throw new IOException("expected root to be CompoundTag, got " + (tag == null ? "null" : tag.getClass().getSimpleName()));
		}

		CompoundTag root = (CompoundTag) tag;
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

		id = data.getInt("ID");
		locked = data.getBoolean("locked");
		scale = data.getByte("scale");
		trackingPosition = data.getBoolean("trackingPosition");
		unlimitedTracking = data.getBoolean("unlimitedTracking");
		center = new Point2i(data.getInt("xCenter"), data.getInt("zCenter"));
		dimension = data.getInt("dimension");
		imageData = data.getByteArray("colors");
		if (imageData.length != IMAGE_WIDTH * IMAGE_HEIGHT) {
			imageData = new byte[IMAGE_WIDTH * IMAGE_HEIGHT];
		}
	}

	public void update() {

		// set background
		context.setFill(Color.BLACK);
		context.fillRect(0, 0, getWidth(), getHeight());
		PixelWriter pw = context.getPixelWriter();

		for (int i = 0; i < imageData.length; i++) {
			int x = i % IMAGE_WIDTH;
			int y = (i - x) / IMAGE_WIDTH;
			pw.setArgb(x, y, MapColor.getColor(imageData[i] & 0xFF));
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
}
