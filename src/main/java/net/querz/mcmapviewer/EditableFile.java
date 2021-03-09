package net.querz.mcmapviewer;

import javafx.beans.Observable;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;

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

	@Override
	public int hashCode() {
		return getFile().hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof EditableFile)) {
			return false;
		}
		EditableFile o = (EditableFile) other;

		try {
			return getFile().getCanonicalPath().equals(o.getFile().getCanonicalPath());
		} catch (IOException ex) {
			return false;
		}
	}
}
