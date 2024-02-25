package net.juligame.classes.logic.ZEscapeAPI;

import imgui.ImGui;
import imgui.flag.ImGuiMouseButton;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;
import net.juligame.Main;
import net.juligame.classes.logic.CreatingMenu;
import net.juligame.classes.logic.annotations.ShowVar;
import net.juligame.classes.utils.Vector2;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class EditorUtils {
    public void Draw(CreatingMenu itemEditorMain) throws Exception {
        Object _item = itemEditorMain.edited;
        ImGui.setCursorPosX(ImGui.getCursorPosX() + 600);
//        if (ImGui.button("X", 20, 20)) {
//            Main.instance.menus.remove(itemEditorMain);
//        }
        ImGui.sameLine();
        ImGui.setCursorPosX(10);

        HashMap<String, List<Field>> a = PrintPropertyKeys(_item);

        for (Map.Entry<String, List<Field>> entry : a.entrySet()) {
            String preatyName = entry.getKey().substring(entry.getKey().lastIndexOf('.') + 1);
            List<Field> Value = entry.getValue();
            if (!Value.isEmpty()) {
                ImGui.textColored(0, 255, 255, 255, preatyName);
                for (Field fieldInfo : Value) {
                    fieldInfo.setAccessible(true);

                    Method callback = null;
                    if (fieldInfo.isAnnotationPresent(ShowVar.class)) {
                        ShowVar showVar = fieldInfo.getAnnotation(ShowVar.class);
                        if (!showVar.editable()) {
                            String value = fieldInfo.get(_item).toString();
                            ImGui.text(fieldInfo.getName() + ": " + value);

                            if (ImGui.isItemClicked(ImGuiMouseButton.Right)) {
//                            ClipboardHelper.HandleRightClick(fieldInfo, _item);
                            }
                            continue;
                        } else {
                            if (!showVar.callback().isEmpty()) {
                                callback = _item.getClass().getMethod(showVar.callback());
                            }
                        }
                    } else {
                        continue;
                    }


                    if (fieldInfo.getType() == String.class) {
                        String value = (String) fieldInfo.get(_item);
                        ImString imString = new ImString(value);
                        ImGui.inputText(fieldInfo.getName(), imString);
                        fieldInfo.set(_item, imString.get());

                        if (ImGui.isItemClicked(ImGuiMouseButton.Right)) {
//                            ClipboardHelper.HandleRightClick(fieldInfo, _item);
                        }
                    }

                    if (fieldInfo.getType() == int.class)
                    {
                        int value = (int) fieldInfo.get(_item);
                        ImInt imInt = new ImInt(value);
                        ImGui.inputInt(fieldInfo.getName(), imInt);
                        fieldInfo.set(_item, imInt.get());

                        if (ImGui.isItemClicked(ImGuiMouseButton.Right)) {
//                            ClipboardHelper.HandleRightClick(fieldInfo, _item);
                        }
                    }

                    if (fieldInfo.getType() == float.class)
                    {
                        float value = (float) fieldInfo.get(_item);
                        ImFloat imFloat = new ImFloat(value);
                        ImGui.inputFloat(fieldInfo.getName(), imFloat);
                        fieldInfo.set(_item, imFloat.get());

                        if (ImGui.isItemClicked(ImGuiMouseButton.Right)) {
//                            ClipboardHelper.HandleRightClick(fieldInfo, _item);
                        }
                    }

                    if (fieldInfo.getType() == boolean.class) {
                        Boolean value = (Boolean) fieldInfo.get(_item);
                        ImBoolean imBoolean = new ImBoolean(value);
                        ImGui.checkbox(fieldInfo.getName(), imBoolean);
                        fieldInfo.set(_item, imBoolean.get());

                        if (ImGui.isItemClicked(ImGuiMouseButton.Right)) {
//                            ClipboardHelper.HandleRightClick(fieldInfo, _item);
                        }
                    }

                    if (fieldInfo.isEnumConstant()) {
                        int value = (int) fieldInfo.get(_item);
                        ImInt imInt = new ImInt(value);
//                        ImGui.combo(fieldInfo.getName(), imInt, Enum.GetNames(fieldInfo.getType()), Enum.GetNames(fieldInfo.getType()).length);
                        fieldInfo.set(_item, imInt.get());

                        if (ImGui.isItemClicked(ImGuiMouseButton.Right)) {
//                            ClipboardHelper.HandleRightClick(fieldInfo, _item);
                        }
                    }

                    if (fieldInfo.getType() == Vector2.class) {
                        Vector2 value = (Vector2) fieldInfo.get(_item);
                        ImFloat ImIntX = new ImFloat(value.x);
                        ImFloat ImIntY = new ImFloat(value.y);
                        ImGui.inputFloat("X #" + value.hashCode(), ImIntX);
                        ImGui.sameLine();
                        ImGui.inputFloat("Y #" + value.hashCode(), ImIntY);

                        if (value.x != ImIntX.get() || value.y != ImIntY.get()) {
                            value.x = ImIntX.get();
                            value.y = ImIntY.get();
                            fieldInfo.set(_item, value);

                            if (callback != null)
                                callback.invoke(_item);
                        }


                        if (ImGui.isItemClicked(ImGuiMouseButton.Right)) {
//                            ClipboardHelper.HandleRightClick(fieldInfo, _item);
                        }
                    }

//                    if (fieldInfo.getType() == Image.class) {
//                        Image image = (Image) fieldInfo.get(_item);
//                        String value = "";
//                        if (image != null) {
//                            value = image.Path;
//                        }
//
//                        if (ImGui.button("Open##" + fieldInfo.getName())) {
//                            ImGui.openPopup("Item Editor - Texture " + fieldInfo.getName() + " -  " + _item.hashCode());
//                        }
//                        ImGui.sameLine();
//
//                        ImString imString = new ImString(value);
//                        ImGui.inputText(fieldInfo.getName(), imString, 255);
//                        if (image != null) {
//                            if (!value.isEmpty()) {
//                                int id = Main.LoadTexture2D(value);
//                                ImGui.image(id, 100, 100);
//
//                                if (ImGui.isItemClicked(ImGuiMouseButton.Right)) {
////                                    ClipboardHelper.HandleRightClick(fieldInfo, _item);
//                                }
//                            }
//
//                            image.Path = value;
//                        } else {
//                            fieldInfo.set(_item, new Image(imString.get()));
//                        }
//
//                        ImBoolean isOpen = new ImBoolean(true);
//                        if (ImGui.beginPopupModal("Item Editor - Texture " + fieldInfo.getName() + " -  " + _item.hashCode(), isOpen, ImGuiWindowFlags.NoTitleBar)) {
//                            FilePicker filePicker = FilePicker.getFolderPicker(fieldInfo, "");
//                            filePicker.onlyAllowFolders = false;
//                            filePicker.allowedExtensions = Arrays.asList(new String[]{"png"});
//
//                            if (filePicker.draw()) {
//                                value = filePicker.selectedFile.replace("Path.Combine(ItemList.path)", "");
//                                FilePicker.removeFilePicker(this);
//                            }
//
//                            ImGui.endPopup();
//                        }
//                        if (image != null)
//                            image.Path = value;
//                        else
//                            fieldInfo.set(_item, new Image(value));
//
//                        if (ImGui.isItemClicked(ImGuiMouseButton.Right)) {
////                            ClipboardHelper.HandleRightClick(fieldInfo, _item);
//                        }
//                    }
//
//                    if (fieldInfo.getType() == Audio.class) {
//                        Audio audio = (Audio) fieldInfo.get(_item);
//                        String value = "";
//                        if (audio != null) {
//                            value = audio.Path;
//                        }
//                        if (ImGui.button("Open##" + fieldInfo.getName())) {
//                            ImGui.openPopup("Item Editor - Audio " + fieldInfo.getName() + " -  " + _item.hashCode());
//                        }
//                        ImGui.sameLine();
//
//                        ImString imString = new ImString(value);
//                        ImGui.inputText(fieldInfo.getName(), imString, 255);
//                        if (ImGui.isItemClicked(ImGuiMouseButton.Right)) {
////                            ClipboardHelper.HandleRightClick(fieldInfo, _item);
//                        }
//
//                        String realAudioPath = value;
//                        if (audio != null && new File(realAudioPath).exists()) {
//                            ImGui.sameLine();
//                            if (ImGui.button("Play##" + fieldInfo.getName())) {
//                                itemEditorMain.PlaySound(realAudioPath);
//                            }
//                        }
//
//                        ImBoolean isOpen = new ImBoolean(true);
//                        if (ImGui.beginPopupModal("Item Editor - Audio " + fieldInfo.getName() + " -  " + _item.hashCode(), isOpen, ImGuiWindowFlags.NoTitleBar)) {
//                            FilePicker filePicker = FilePicker.getFolderPicker(itemEditorMain, "");
//                            filePicker.onlyAllowFolders = false;
//                            filePicker.allowedExtensions = Arrays.asList(new String[]{".ogg"});
//
//                            if (filePicker.draw()) {
//                                value = filePicker.selectedFile.replace("Path.Combine(ItemList.path)", "");
//                                FilePicker.removeFilePicker(this);
//                            }
//
//                            ImGui.endPopup();
//                        }
//
//                        fieldInfo.set(_item, new Audio(value));
//                    }


                    if (ImGui.isItemHovered()) {
                        ImGui.beginTooltip();
                        ImGui.textColored(0, 1, 1, 1, "Type: " + fieldInfo.getType().toString().substring(fieldInfo.getType().toString().lastIndexOf('.') + 1));
                        try {
                            ImGui.textColored(0, 1, 1, 1, "Rigth click to paste. ");
                        } catch (Exception e) {
                            // ignored
                        }

                        ImGui.endTooltip();
                    }
                }
            }
        }

        ImGui.newLine();
//        if (ImGui.button("Save"))
//            itemEditorMain.edited.create();
    }

    public static <T> HashMap<String, List<Field>> PrintPropertyKeys(T obj){
        Class objectType = obj.getClass();
        List<Class> classes = new ArrayList<Class>();
        Class lastType = null;
        while (objectType != null && objectType != lastType) {
            classes.add(objectType);
            lastType = objectType;
            objectType = objectType.getClass().getSuperclass();
        }
        Collections.reverse(classes);

        HashMap<String, List<Field>> fieldsDict = new HashMap<String, List<Field>>();
        classes.forEach(Class -> {
            // Use BindingFlags to get only the fields declared in the current class, not the inherited ones
            List<Field> field = Arrays.stream(Class.getDeclaredFields()).collect(Collectors.toList());
            fieldsDict.put(Class.getTypeName(), field);
        });

        return fieldsDict;
    }
}
