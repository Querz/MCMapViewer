package net.querz.mcmapviewer;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class MapIconView extends Label {

	private ObjectProperty<MapIconData> data = new SimpleObjectProperty<>();

	public MapIconView(ObjectProperty<MapIconData> data) {
		super(null, new ImageView(data.getValue().getColor().getIcon()));
		this.data.bindBidirectional(data);
	}

	public void setIcon(MapIcon icon) {
		setGraphic(new ImageView(icon.getIcon()));
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
}
