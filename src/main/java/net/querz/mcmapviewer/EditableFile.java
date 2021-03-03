package net.querz.mcmapviewer;

import javafx.beans.Observable;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.util.Callback;

import java.io.File;

public class EditableFile {

	private SimpleBooleanProperty edited = new SimpleBooleanProperty(false);
	private SimpleObjectProperty<File> file = new SimpleObjectProperty<>();

	public EditableFile(File file) {
		this.file.set(file);
	}

	public void setEdited(boolean edited) {
		this.edited.set(edited);
	}

	public boolean isEdited() {
		return edited.get();
	}

	public File getFile() {
		return file.get();
	}

	public static Callback<EditableFile, Observable[]> extractor() {
		return param -> new Observable[]{param.file, param.edited};
	}

	@Override
	public String toString() {
		return file.get().getName() + (edited.get() ? " *" : "");
	}
}
