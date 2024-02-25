package net.juligame.classes.logic;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import net.juligame.Main;
import net.juligame.classes.logic.ZEscapeAPI.EditorUtils;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class CreatingMenu {

    public Object edited;
    private final String name;
    private final EditorUtils editorUtils = new EditorUtils();
    public CreatingMenu(Object edited) {
        this.name = edited.getClass().getName().substring(edited.getClass().getName().lastIndexOf('.') + 1);
        AtomicBoolean exists = new AtomicBoolean(false);
        Main.instance.menus.forEach(menu -> {
            if (Objects.equals(menu.name, name)) {
                exists.set(true);
                return;
            }
        });
        if (exists.get())
            return;


        Main.instance.menus.add(this);
        this.edited = edited;
    }

    public void process() {
        ImGui.begin("Creator Menu - " + name + "##" + edited.hashCode(), ImGuiWindowFlags.AlwaysAutoResize);
        try {
            editorUtils.Draw(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ImGui.end();
    }

    public void PlaySound(String realAudioPath) {
        System.out.println("Playing sound: " + realAudioPath);
    }
}
