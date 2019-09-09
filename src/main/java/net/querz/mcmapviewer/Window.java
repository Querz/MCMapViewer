package net.querz.mcmapviewer;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.io.File;

public class Window extends Application {

	/*
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
	public void start(Stage primaryStage) throws Exception {
		MapView mv = new MapView(new File("/Users/rb/IdeaProjects/MCMapViewer/maps/map_1.dat"));

		primaryStage.setTitle("MCMapViewer");
		BorderPane pane = new BorderPane();
		pane.setTop(new OptionBar(primaryStage));
		pane.setCenter(mv);
		pane.setBottom(new InfoPanel(mv));

		Scene scene = new Scene(pane, mv.getWidth() * mv.getScaleX(), mv.getHeight() * mv.getScaleY());
		primaryStage.setOnCloseRequest(e -> System.exit(0));
		primaryStage.setScene(scene);
		primaryStage.show();
	}
}
