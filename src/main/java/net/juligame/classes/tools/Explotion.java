package net.juligame.classes.tools;

import net.juligame.Window;
import net.juligame.classes.Particle;
import net.juligame.classes.utils.ColorUtils;
import net.juligame.classes.utils.Vector2;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Explotion {

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

    // lo calculamos en cuartos porque lo importante es añadir la fuerza de
    // afuera para adentro.

    // ordenar las listas es carardo, asi que mejor hacerlo en cuartos
    // que al ser un circulo es muy parecido el resultado.

    public static void Explode(int x, int y, float size) {
        size = size * 1.3f;

        List<Info> particles = CalculateQuarterCircle(x, y, size);

        // sort particles by distance, further to closer
//        particles.sort((o1, o2) -> Float.compare(o2.distance, o1.distance));

        float finalSize = size;
        particles.forEach(info -> {
            float force = finalSize * 5 / Math.max(info.distance, 1);
            force = Math.min(force, 30);
//            force = finalSize / 10;

            Particle particle = info.particle;
            particle.SetVelocityWithTimeBurn(info.direction.Multiply(force), 1000);
            Window.tileMap.AddParticleToTickQueue(particle);
        });
    }


    public static List<Info> CalculateQuarterCircle(int x, int y, float size){
        List<Info> particles = new ArrayList<>();
        for (int i = (int) -size; i <= 0; i++) {
            for (int j = (int) -size; j <= 0; j++) {
                float distance = (float) Math.sqrt(i * i + j * j);
                if (size <= distance)
                    continue;


                Particle particle =  Window.tileMap.getTile(x + i, y + j);
                if (particle == null || particle.getID() == -1)
                    continue;

                Info info = new Info(particle, distance, new Vector2(i, j).Normalize());
                particles.add(info);
            }
        }
        for (int i = (int) size; i > 0; i--) {
            for (int j = (int) -size; j < 0; j++) {
                float distance = (float) Math.sqrt(i * i + j * j);
                if (size <= distance)
                    continue;


                Particle particle =  Window.tileMap.getTile(x + i, y + j);
                if (particle == null || particle.getID() == -1)
                    continue;

                Info info = new Info(particle, distance, new Vector2(i, j).Normalize());
                particles.add(info);
            }
        }

        for (int i = (int) -size; i < 0; i++) {
            for (int j = (int) size; j > 0; j--) {
                float distance = (float) Math.sqrt(i * i + j * j);
                if (size <= distance)
                    continue;


                Particle particle =  Window.tileMap.getTile(x + i, y + j);
                if (particle == null || particle.getID() == -1)
                    continue;

                Info info = new Info(particle, distance, new Vector2(i, j).Normalize());
                particles.add(info);
            }
        }

        for (int i = (int) size; i >= 0; i--) {
            for (int j = (int) size; j >= 0; j--) {
                float distance = (float) Math.sqrt(i * i + j * j);
                if (size <= distance)
                    continue;


                Particle particle =  Window.tileMap.getTile(x + i, y + j);
                if (particle == null || particle.getID() == -1)
                    continue;

                Info info = new Info(particle, distance, new Vector2(i, j).Normalize());
                particles.add(info);
            }
        }

        return particles;
    }
}
