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
		EditableFile selected = getSelectionModel().getSelectedItem();
		File[] files = directory.listFiles((d, n) -> n.matches("map_[0-9]+\\.dat"));
		if (files == null) {
			this.files = new File[0];
		}
		this.files = files;
		Arrays.sort(this.files);
		getItems().clear();
		Arrays.stream(this.files).forEach(f -> getItems().add(new EditableFile(f)));
		getSelectionModel().select(selected);
		if (onFileSelectionChanged != null) {
			onFileSelectionChanged.accept(getSelectionModel().getSelectedItem());
		}
	}

	public void setDirectory(File directory) {
		File[] files = directory.listFiles((d, n) -> n.matches("map_[0-9]+\\.dat"));
		if (files == null) {
			this.files = new File[0];
		}
		this.files = files;
		this.directory = directory;
		Arrays.sort(this.files);
		getItems().clear();
		Arrays.stream(this.files).forEach(f -> getItems().add(new EditableFile(f)));
		getSelectionModel().select(0);
		if (onFileSelectionChanged != null) {
			onFileSelectionChanged.accept(getSelectionModel().getSelectedItem());
		}
	}

	public void setOnFileSelectionChanged(Consumer<EditableFile> consumer) {
		onFileSelectionChanged = consumer;
		getSelectionModel().selectedItemProperty().addListener((v, o, n) -> {
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
