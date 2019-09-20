package net.querz.mcmapviewer;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class MapIconEditor extends VBox {

	public MapIconEditor(MapIconData data, int x, int y) {

		Label l = new Label("testing 123");
		TextField t = new TextField("foo bar");

		getChildren().addAll(l, t);

		setBackground(new Background(new BackgroundFill(Color.BLACK, new CornerRadii(0), new Insets(0))));

		setTranslateX(x);
		setTranslateY(y);
	}
}
