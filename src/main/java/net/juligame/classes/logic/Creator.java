package net.juligame.classes.logic;

import imgui.ImGui;
import net.juligame.classes.logic.Interfaces.ICreatable;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class Creator {

    public Creator() {
        getClasses();
    }

    List<Class<?>> classes;
    public void getClasses() {
        try {
            classes = findClasses("net.juligame.sveditor.types");
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            return;
        }
    }
    public void process() {
        ImGui.begin("Creator");
        for (Class<?> c : classes) {
            ImGui.text(c.getName().substring(c.getName().lastIndexOf(".") + 1));
            ImGui.sameLine();
            if (ImGui.button("Create")) {
                try {
                    Object o = c.newInstance();
                    if (!(o instanceof ICreatable)) {
                        System.out.println("Class " + c.getName() + " does not implement ICreatable");
                        continue;
                    }
                    CreatingMenu menu = new CreatingMenu((ICreatable) o);
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        ImGui.end();
    }

    private List<Class<?>> findClasses(String packageName) throws IOException, ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);

        List<String> classNames = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            String protocol = resource.getProtocol();
            if ("file".equals(protocol)) {
                classNames.addAll(findClassesInDirectory(packageName, resource.getPath()));
            } else if ("jar".equals(protocol)) {
                // Handle classes in JAR files
                // You may need a different approach based on your requirements
                // For simplicity, this example doesn't handle JAR files
            }
        }

        for (String className : classNames) {
            classes.add(Class.forName(className));
        }

        return classes;
    }

    private List<String> findClassesInDirectory(String packageName, String path) {
        List<String> classNames = new ArrayList<>();
        java.io.File directory = new java.io.File(path);
        if (directory.exists()) {
            String[] files = directory.list();
            for (String file : files) {
                if (file.endsWith(".class")) {
                    String className = packageName + '.' + file.substring(0, file.length() - 6);
                    classNames.add(className);
                }
            }
        }
        return classNames;
    }
}
