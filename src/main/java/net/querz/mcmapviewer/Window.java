package net.querz.mcmapviewer;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import net.querz.mcmapviewer.map.MapView;

import java.io.IOException;
import java.net.URL;

public class Window extends Application {

	/*
	* File	Icons
	* Open	Item frame
	* Save
	* Quit
	*
	*
	*
	*
	* -------------------------------------------------------
	* | File | About |										|
	* -------------------------------------------------------
	* map_0		|^|											|
	* map_1 *	| |											|
	* map_2		| |											|
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
	* X-Center:	 |______|	Tracking Position:	|x|			|
	* Z-Center:	 |______|	Unlimited Tracking: |x|			|
	* Scale:	 |______|v|	Locked:				|x|			|
	* Dimension: |______|v|									|
	* -------------------------------------------------------
	* */

	@Override
	public void start(Stage primaryStage) {
		MapView mv = new MapView();
		InfoPanel info = new InfoPanel(mv);
		mv.setInfoPanel(info);
		FileView fileView = new FileView();
		fileView.setOnFileSelectionChanged(mv::loadMapFile);

		primaryStage.setTitle("MCMapViewer");

		SplitPane split = new SplitPane(fileView, mv);

		BorderPane pane = new BorderPane();
		pane.setTop(new OptionBar(primaryStage, fileView, mv));
		pane.setCenter(split);
		pane.setBottom(info);

		Scene scene = new Scene(pane, 560, 600);

		URL cssRes = Window.class.getClassLoader().getResource("style.css");
		if (cssRes != null) {
			String styleSheet = cssRes.toExternalForm();
			scene.getStylesheets().add(styleSheet);
		}

		primaryStage.setScene(scene);
		primaryStage.show();
	}
}
