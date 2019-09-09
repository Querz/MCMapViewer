package net.querz.mcmapviewer;

import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;

public class InfoPanel extends BorderPane {

	/*
	 * -------------------------------------------------------
	 * | File | About |										|
	 * -------------------------------------------------------
	 * map_0	|^|											|
	 * map_1 *	| |											|
	 * map_2	| |											|
	 * ...		| |											|
	 * 			| |											|
	 * 			| |											|
	 * 			| |											|
	 * 			| |											|
	 * 			| |					Map view				|
	 * 			| |											|
	 * 			| |											|
	 * 			| |											|
	 * 			| |											|
	 * 			| |											|
	 * 			| |											|
	 * 			| |											|
	 * 			|v|											|
	 * -------------------------------------------------------
	 * ID: 		|______|	Scale:		  		|______|v|	|
	 * X-Center:|______|	Dimension:	  		|______|v|	|
	 * Z-Center:|______|	Tracking Position:	|x|			|
	 * 						Unlimited Tracking: |x|			|
	 * 						Locked:				|x|			|
	 * -------------------------------------------------------
	 * */

	private Label idLabel = new Label("ID:");
	private Label xCenterLabel = new Label("X-Center:");
	private Label zCenterLabel = new Label("Z-Center:");
	private Label scaleLabel = new Label("Scale:");
	private Label dimensionLabel = new Label("Dimension:");
	private Label trackingPositionLabel = new Label("Tracking position:");
	private Label unlimitedTrackingLabel = new Label("Unlimited tracking:");
	private Label lockedLabel = new Label("Locked:");

	private TextField idField = new TextField();
	private TextField xCenterField = new TextField();
	private TextField zCenterField = new TextField();
	private ComboBox<Scale> scaleField = new ComboBox<>();
	private ComboBox<Dimension> dimensionField = new ComboBox<>();
	private CheckBox trackingPositionField = new CheckBox();
	private CheckBox unlimitedTrackingField = new CheckBox();
	private CheckBox lockedField = new CheckBox();

	public InfoPanel(MapView mapView) {
		// arrange fields
		GridPane grid = new GridPane();

		grid.add(pair(xCenterLabel, xCenterField), 0, 0);
		grid.add(pair(zCenterLabel, zCenterField), 0, 1);
		scaleField.getItems().addAll(Scale.values());
		grid.add(pair(scaleLabel, scaleField), 0, 2);
		dimensionField.getItems().addAll(Dimension.values());
		grid.add(pair(dimensionLabel, dimensionField), 0, 3);
		grid.add(pair(trackingPositionLabel, trackingPositionField), 1, 0);
		grid.add(pair(unlimitedTrackingLabel, unlimitedTrackingField), 1, 1);
		grid.add(pair(lockedLabel, lockedField), 1, 2);

		scaleField.valueProperty().bindBidirectional(mapView.scaleProperty());
		dimensionField.valueProperty().bindBidirectional(mapView.dimensionProperty());
		trackingPositionField.selectedProperty().bindBidirectional(mapView.trackingPositionProperty());
		unlimitedTrackingField.selectedProperty().bindBidirectional(mapView.unlimitedTrackingProperty());
		lockedField.selectedProperty().bindBidirectional(mapView.lockedProperty());
		StringConverter<Number> converter = new NumberStringConverter();
		Bindings.bindBidirectional(xCenterField.textProperty(), mapView.xCenterProperty(), converter);
		Bindings.bindBidirectional(zCenterField.textProperty(), mapView.zCenterProperty(), converter);

		setCenter(grid);
	}

	private HBox pair(Label label, Node value) {
		HBox hBox = new HBox();
		hBox.getChildren().addAll(label, value);
		return hBox;
	}
}
