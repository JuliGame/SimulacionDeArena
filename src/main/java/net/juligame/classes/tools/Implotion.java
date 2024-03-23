package net.juligame.classes.tools;

import net.juligame.Window;
import net.juligame.classes.Particle;
import net.juligame.classes.utils.Vector2;

import java.util.ArrayList;
import java.util.List;

public class Implotion {

    private static class Info {
        public Particle particle;
        public float distance;
        public Vector2 direction;
        public Info(Particle particle, float distance, Vector2 direction) {
            this.particle = particle;
            this.distance = distance;
            this.direction = direction;
        }
    }
    public static void Implode(int x, int y, float size) {
        size = size * 1.3f;
        float finalSize = size;

        List<Explotion.Info> particles = Explotion.CalculateQuarterCircle(x, y, size);

        particles.forEach(info -> {
            float force = finalSize * 5 / Math.max(info.distance, 1);
            force = -Math.min(force, 30);

            Particle particle = info.particle;
            particle.SetVelocityWithTimeBurn(info.direction.Multiply(force), Window.TicksPerSecond * 3);
        });

        Window.tileMap.AddSyncTask(() -> {
            particles.forEach(info -> {
                Window.tileMap.AddParticleToTickQueue(info.particle);
            });
        });

    }
}
