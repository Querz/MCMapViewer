package net.querz.mcmapviewer;

import javafx.application.Platform;
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
		Arrays.sort(this.files, this::compareMapFiles);
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
		Arrays.sort(this.files, this::compareMapFiles);
		getItems().clear();
		Arrays.stream(this.files).forEach(f -> getItems().add(new EditableFile(f)));
		getSelectionModel().select(0);
		if (onFileSelectionChanged != null) {
			onFileSelectionChanged.accept(getSelectionModel().getSelectedItem());
		}
	}

	private int compareMapFiles(File a, File b) {
		String aName = a.getName();
		String sa = aName.substring(4, aName.length() - 4);
		String bName = b.getName();
		String sb = bName.substring(4, bName.length() - 4);
		return Integer.compare(Integer.parseInt(sa), Integer.parseInt(sb));
	}

	public void setOnFileSelectionChanged(Consumer<EditableFile> consumer) {
		onFileSelectionChanged = consumer;
		getSelectionModel().selectedItemProperty().addListener((v, o, n) -> {
			if (getSelectionModel().getSelectedItem() != null && !getSelectionModel().getSelectedItem().equals(selected)) {
				for (EditableFile file : getItems()) {
					if (file.isEdited()) {
						Platform.runLater(() -> getSelectionModel().select(file));
						return;
					}
				}
				selected = getSelectionModel().getSelectedItem();
				consumer.accept(getSelectionModel().getSelectedItem());
			}
		});
	}
}
