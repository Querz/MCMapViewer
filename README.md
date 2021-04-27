# MCMapViewer

A tool to view and edit Minecraft maps.

---

## Loading

To load maps, select either the worlds `data` folder or a map file. Map files file names use the format `map_<id>.dat`, so e.g. `map_15.dat` is a valid map file name.

## Editing

Left-click on one of the map files in the list view on the left side to open it. Once a change has been made, you can not switch to a different map file until it has been saved or reloaded. An unsaved map file has a `*` next to its name.

### Banners

To add a banner to the map, right-click anywhere on the map.
A banner's color can be changed by right-clicking on the banner and then selecting a new color. When clicking the red cross, the banner is deleted.
The banner's text can be changed by right-clicking on its text. The text is always saved in JSON format.

### Other Icons

You might wonder: Why the hell does it support banners, but not the fancy other icons like on treasure maps you can acquire from Cartographers?
The answer is that those icons are unfortunately *not* saved in the map file, but per-item in the minecraft world files. You may want to look at [MCA Selector](https://github.com/Querz/mcaselector) to edit them.

## Importing

When a map file has been loaded, a custom image can be imported and squeezed into the Minecraft's map format.