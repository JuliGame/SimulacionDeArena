package net.juligame.classes.logic.ZEscapeAPI;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiSelectableFlags;
import imgui.type.ImBoolean;

import java.awt.*;
import java.io.File;
import java.io.FileFilter;
import java.util.List;
import java.util.*;

public class FilePicker {

    private static final Map<Object, FilePicker> filePickers = new HashMap<>();

    public String rootFolder;
    public String currentFolder;
    public String selectedFile;
    public List<String> allowedExtensions;
    public boolean onlyAllowFolders;

    private FilePicker() {
        // Private constructor
    }

    public static FilePicker getFolderPicker(Object o, String startingPath) {
        return getFilePicker(o, startingPath, null, true);
    }

    public static FilePicker getFilePicker(Object o, String startingPath, String searchFilter, boolean onlyAllowFolders) {
        if (new File(startingPath).exists()) {
            startingPath = new File(startingPath).getParent();
        } else if (startingPath == null || !new File(startingPath).isDirectory()) {
            startingPath = System.getProperty("user.dir");
            if (startingPath == null || startingPath.isEmpty()) {
                startingPath = System.getProperty("user.home");
            }
        }

        if (filePickers.containsKey(o)){
            return filePickers.get(o);
        }

        filePickers.put(o, new FilePicker());

        FilePicker fp = filePickers.get(o);
        fp.rootFolder = startingPath;
        fp.currentFolder = startingPath;
        fp.onlyAllowFolders = onlyAllowFolders;

        if (searchFilter != null) {
            if (fp.allowedExtensions != null) {
                fp.allowedExtensions.clear();
            } else {
                fp.allowedExtensions = new ArrayList<>();
            }
            fp.allowedExtensions.addAll(Arrays.asList(searchFilter.split("\\|")));
        }

        return fp;
    }

    public static void removeFilePicker(Object o) {
        filePickers.remove(o);
    }

    public boolean draw() {
        ImGui.text("Current Folder: " + currentFolder);
        boolean result = false;

        if (ImGui.beginChildFrame(1, 400, 400)) {
            File directory = new File(currentFolder);
            if (directory.exists()) {
                if (directory.getParentFile() != null && !currentFolder.equals(rootFolder)) {
                    ImGui.pushStyleColor(ImGuiCol.Text, Color.YELLOW.getRGB());
                    ImBoolean dontClosePopups = new ImBoolean();
                    if (ImGui.selectable("../")) {
                        currentFolder = directory.getParent();
                    }
                    ImGui.popStyleColor();
                }

                File[] fileSystemEntries = getFileSystemEntries(directory.getAbsolutePath());
                for (File fse : fileSystemEntries) {
                    if (fse.isDirectory()) {
                        String name = fse.getName();
                        ImGui.pushStyleColor(ImGuiCol.Text, Color.YELLOW.getRGB());
                        if (ImGui.selectable(name + "/", new ImBoolean(false), ImGuiSelectableFlags.DontClosePopups)) {
                            currentFolder = fse.getAbsolutePath();
                            System.out.println("Selected folder: " + currentFolder);
                        }
                        ImGui.popStyleColor();
                    } else {
                        String name = fse.getName();
                        boolean isSelected = selectedFile != null && selectedFile.equals(fse.getAbsolutePath());
                        ImBoolean dontClosePopups = new ImBoolean();
                        if (ImGui.selectable(name)) {
                            selectedFile = fse.getAbsolutePath();
                            if (ImGui.isMouseDoubleClicked(0)) {
                                result = true;
                                ImGui.closeCurrentPopup();
                            }
                        }
                    }
                }
            }
        }
        ImGui.endChildFrame();

        if (ImGui.button("Cancel")) {
            result = false;
            ImGui.closeCurrentPopup();
        }

        if (onlyAllowFolders) {
            ImGui.sameLine();
            if (ImGui.button("Open")) {
                result = true;
                selectedFile = currentFolder;
                ImGui.closeCurrentPopup();
            }
        } else if (selectedFile != null) {
            ImGui.sameLine();
            if (ImGui.button("Open")) {
                result = true;
                ImGui.closeCurrentPopup();
            }
        }

        return result;
    }

    private boolean tryGetFileInfo(String fileName, File[] realFile) {
        realFile[0] = new File(fileName);
        return true;
    }

    private File[] getFileSystemEntries(String fullName) {
        List<File> files = new ArrayList<>();
        List<File> dirs = new ArrayList<>();

        File[] fileSystemEntries = new File(fullName).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isDirectory()) {
                    dirs.add(pathname);
                    return true;
                } else if (!onlyAllowFolders) {
                    if (allowedExtensions != null) {
                        String ext = getExtension(pathname.getName());
                        if (allowedExtensions.contains(ext)) {
                            files.add(pathname);
                            return true;
                        }
                    } else {
                        files.add(pathname);
                        return true;
                    }
                }
                return false;
            }
        });

        List<File> ret = new ArrayList<>(dirs);
        ret.addAll(files);

        return ret.toArray(new File[0]);
    }

    private String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }
}
