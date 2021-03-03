package net.querz.mcmapviewer.map;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import net.querz.mcmapviewer.point.Point2i;

public class MapIconView extends Label {

	private ObjectProperty<MapIconData> data = new SimpleObjectProperty<>();
	private MapLabelView label;

	private Point2i pos, size, offset;
	private int scale;


	public MapIconView(ObjectProperty<MapIconData> data, MapLabelView label) {
		super(null, new ImageView(data.getValue().getColor().getIcon()));
		this.label = label;
		this.data.bindBidirectional(data);
	}

	public void setIcon(MapIcon icon) {
		setGraphic(new ImageView(icon.getIcon()));
		data.get().setIcon(icon);
	}

	public MapIconData getData() {
		return data.get();
	}

	public ObjectProperty<MapIconData> dataProperty() {
		return data;
	}

	public void setData(MapIconData data) {
		this.data.set(data);
	}

	public MapLabelView getMapLabelView() {
		return label;
	}

	public Point2i getOffset() {
		return offset;
	}

	public void translate(Point2i pos, Point2i size, Point2i offset, int scale) {
		this.pos = pos;
		this.size = size;
		this.offset = offset;
		this.scale = scale;
		Point2i iconPos = pos.sub((int) getWidth() / 2, (int) getHeight() / 2);

		if (iconPos.getX() + getWidth() / 2 < 0) {
			iconPos.setX((int) -(getWidth() / 2));
		} else if (iconPos.getX() > size.getX() - getWidth() / 2) {
			iconPos.setX((int) (size.getX() - getWidth() / 2));
		}
		if (iconPos.getZ() + getHeight() / 2 < 0) {
			iconPos.setZ((int) -(getHeight() / 2));
		} else if (iconPos.getZ() > size.getZ() - getHeight() / 2) {
			iconPos.setZ((int) (size.getZ() - getHeight() / 2));
		}

		iconPos = iconPos.add(offset);

		// adjust pos to map pixel
		iconPos = iconPos.sub(iconPos.mod(scale));

		setTranslateX(iconPos.getX());
		setTranslateY(iconPos.getZ());

		translateLabel(label, offset, label.getWidth(), label.getHeight());

		System.out.println(label.getWidth());
	}

	void translateLabel(Node label, Point2i offset, double width, double height) {
		Point2i labelPos = pos.sub((int) width / 2, (int) -(getHeight() / 2));

		Point2i overlaySize = size.add(offset.mul(2));

		labelPos = labelPos.add(offset);

		if (labelPos.getX() < 0) {
			labelPos.setX(0);
		} else if (labelPos.getX() > overlaySize.getX() - width) {
			labelPos.setX((int) (overlaySize.getX() - width));
		}
		if (labelPos.getZ() < offset.getZ() + getHeight() / 2) {
			labelPos.setZ(offset.getZ() + (int) getHeight() / 2);
		} else if (labelPos.getZ() > overlaySize.getZ() - height) {
			labelPos.setZ((int) (overlaySize.getZ() - height));
		}

		// adjust pos to map pixel
		labelPos = labelPos.sub(labelPos.mod(scale));

		label.setTranslateX(labelPos.getX());
		label.setTranslateY(labelPos.getZ());
	}

	void update() {
		if (pos != null) {
			translate(pos, size, offset, scale);
		}
	}
}
