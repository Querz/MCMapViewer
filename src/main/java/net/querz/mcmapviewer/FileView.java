package net.querz.mcmapviewer;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import java.io.File;
import java.util.function.Consumer;

public class FileView extends ListView<File> {

	private File directory;
	private File[] files;
	private File selected;
	private Consumer<File> onFileSelectionChanged;

	public FileView() {
		getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		setEditable(false);
		setCellFactory(p -> new ListCell<File>() {
			@Override
			protected void updateItem(File item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
				} else {
					setText(item.getName());
				}
			}
		});
	}

	public void setDirectory(File directory) {
		this.directory = directory;
		this.files = directory.listFiles((d, n) -> n.matches("map_[0-9]+\\.dat"));
		getItems().clear();
		getItems().addAll(files);
		getSelectionModel().select(0);
		if (onFileSelectionChanged != null) {
			onFileSelectionChanged.accept(getSelectionModel().getSelectedItem());
		}
	}

	public void setOnFileSelectionChanged(Consumer<File> consumer) {
		onFileSelectionChanged = consumer;
		setOnMousePressed(e -> {
			if (getSelectionModel().getSelectedItem() != null && !getSelectionModel().getSelectedItem().equals(selected)) {
				selected = getSelectionModel().getSelectedItem();
				consumer.accept(getSelectionModel().getSelectedItem());
			}
		});
	}
}
