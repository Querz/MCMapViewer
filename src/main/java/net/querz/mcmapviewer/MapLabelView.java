package net.querz.mcmapviewer;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.HBox;

public class MapLabelView extends HBox {

	private ObjectProperty<MapIconData> data = new SimpleObjectProperty<>();
	private MapIconView icon;

	public MapLabelView(ObjectProperty<MapIconData> data) {
		getChildren().addAll(data.getValue().getTextElements());
		this.data.bindBidirectional(data);
		setMinWidth(10);
		widthProperty().addListener((i, o, n) -> icon.update());
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

	void assignIcon(MapIconView icon) {
		this.icon = icon;
	}

	public MapIconView getIcon() {
		return icon;
	}

	public void update() {
		getChildren().setAll(data.getValue().getTextElements());
	}
}
