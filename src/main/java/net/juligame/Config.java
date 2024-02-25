package net.juligame;

import net.juligame.classes.logic.annotations.ShowVar;
import net.juligame.classes.utils.Vector2;

public class Config {
    @ShowVar
    public float brushSize = 1;
    @ShowVar
    public float hue;
    @ShowVar(editable = true, callback = "onGravityChanged")
    public Vector2 gravity = new Vector2(0, 0.14f);
    public void onGravityChanged() {
        Window.tileMap.particles.forEach(p -> Window.tileMap.AddParticleToTickQueue(p));
    }

    @ShowVar(editable = true, callback = "onWindForceChanged")
    public Vector2 windForce = new Vector2(0.02f, 0);
    public void onWindForceChanged() {
        System.out.println("Wind corce changed");
    }
}
