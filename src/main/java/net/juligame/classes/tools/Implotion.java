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
        size = size * 4;
        List<Info> particles = new ArrayList<>();
        for (int i = (int) -size; i < size; i++) {
            for (int j = (int) -size; j < size; j++) {
                float distance = (float) Math.sqrt(i * i + j * j);
                if (size <= distance)
                    continue;


                Particle particle =  Window.tileMap.getTile(x + i, y + j);
                if (particle == null || particle.getID() == -1)
                    continue;

                Info info = new Info(particle, distance, new Vector2(i, j).Normalize().Invert());
                particles.add(info);
            }
        }

        // sort particles by distance, further to closer
        particles.sort((o1, o2) -> Float.compare(o1.distance, o2.distance));

        float finalSize = size;
        particles.forEach(info -> {
            float force = finalSize * 1 / Math.max(info.distance, 1);

            Particle particle = info.particle;
            particle.velocity = info.direction.Multiply(force);
            Window.tileMap.AddParticleToTickQueue(particle);
        });
    }
}
