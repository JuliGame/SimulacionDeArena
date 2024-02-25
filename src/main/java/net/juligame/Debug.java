package net.juligame;

import net.juligame.classes.logic.annotations.ShowVar;

public class Debug {

    @ShowVar(editable = false)
    public int TPS = 1;
    @ShowVar(editable = false)
    public int FPS;
    @ShowVar(editable = false)
    public int TickingParticles;
}
