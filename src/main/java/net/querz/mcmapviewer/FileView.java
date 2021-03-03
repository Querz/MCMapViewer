package net.querz.mcmapviewer;

import javafx.collections.FXCollections;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import java.io.File;
import java.util.Arrays;
import java.util.function.Consumer;

public class FileView extends ListView<EditableFile> {

	private File directory;
	private File[] files;
	private EditableFile selected;
	private Consumer<EditableFile> onFileSelectionChanged;

	public FileView() {
		getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		setEditable(false);
		setItems(FXCollections.observableArrayList(EditableFile.extractor()));
	}

	public void reload() {
		setDirectory(directory);
	}

	public void setDirectory(File directory) {
		this.directory = directory;
		this.files = directory.listFiles((d, n) -> n.matches("map_[0-9]+\\.dat"));
		getItems().clear();
		Arrays.stream(this.files).forEach(f -> getItems().add(new EditableFile(f)));
		getSelectionModel().select(0);
		if (onFileSelectionChanged != null) {
			onFileSelectionChanged.accept(getSelectionModel().getSelectedItem());
		}
	}

	public void setOnFileSelectionChanged(Consumer<EditableFile> consumer) {
		onFileSelectionChanged = consumer;
		setOnMousePressed(e -> {
			if (getSelectionModel().getSelectedItem() != null && !getSelectionModel().getSelectedItem().equals(selected)) {
				for (EditableFile file : getItems()) {
					if (file.isEdited()) {
						return;
					}
				}
				selected = getSelectionModel().getSelectedItem();
				consumer.accept(getSelectionModel().getSelectedItem());
			}
		});
	}
}
