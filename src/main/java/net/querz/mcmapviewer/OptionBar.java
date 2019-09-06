package net.querz.mcmapviewer;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.stage.Stage;

public class OptionBar extends MenuBar {

	private Menu file = new Menu("File");

	private MenuItem open = new MenuItem("Open");
	private MenuItem quit = new MenuItem("Quit");

	public OptionBar(Stage primaryStage) {
		getStyleClass().add("option-bar");

		file.getItems().addAll(open, new SeparatorMenuItem(), quit);

		open.setOnAction(e -> DialogHelper.openDirectory(primaryStage));
		quit.setOnAction(e -> System.exit(0));
	}
}
