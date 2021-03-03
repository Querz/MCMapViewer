package net.querz.mcmapviewer;

import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.stage.Stage;
import net.querz.mcmapviewer.map.MapView;

public class OptionBar extends MenuBar {

	private Menu file = new Menu("File");

	private MenuItem open = new MenuItem("Open");
	private MenuItem save = new MenuItem("Save");
	private MenuItem reload = new MenuItem("Reload");
	private MenuItem quit = new MenuItem("Quit");

	private Label about = new Label("About");

	public OptionBar(Stage primaryStage, FileView fileView, MapView mapView) {
		getStyleClass().add("option-bar");

		file.getItems().addAll(open, save, reload, new SeparatorMenuItem(), quit);

		open.setOnAction(e -> DialogHelper.openDirectory(primaryStage, fileView));
		reload.setOnAction(e -> fileView.reload());
		save.setOnAction(e -> mapView.save());
		quit.setOnAction(e -> System.exit(0));

		open.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCodeCombination.SHORTCUT_DOWN));
		save.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCodeCombination.SHORTCUT_DOWN));
		reload.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCodeCombination.SHORTCUT_DOWN));
		quit.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCodeCombination.SHORTCUT_DOWN));

		about.setOnMouseClicked(e -> new AboutDialog(primaryStage).showAndWait());
		Menu aboutMenu = new Menu();
		aboutMenu.setGraphic(about);

		getMenus().addAll(file, aboutMenu);

	}
}
