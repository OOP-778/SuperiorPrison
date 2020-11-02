package com.bgsoftware.superiorprison.plugin.tasks;

import com.bgsoftware.superiorprison.api.data.mine.SuperiorMine;
import com.bgsoftware.superiorprison.api.data.mine.area.Area;
import com.bgsoftware.superiorprison.api.data.mine.area.AreaEnum;
import com.bgsoftware.superiorprison.plugin.SuperiorPrisonPlugin;
import com.oop.orangeengine.main.task.OTask;
import com.oop.orangeengine.particle.OParticle;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.concurrent.TimeUnit;

public class MineVisualization extends OTask {
    public MineVisualization() {
        sync(false);
        delay(TimeUnit.SECONDS, 1);
        runnable(() -> {
            for (SuperiorMine mine : SuperiorPrisonPlugin.getInstance().getMineController().getMines()) {
                for (AreaEnum value : AreaEnum.values()) {
                    String particle = value == AreaEnum.MINE ? "CLOUD" : "BARRIER";
                    Area area = mine.getArea(value);
                    Location minPoint = area.getMinPoint().clone().add(0.5, 0.5, 0.5);
                    Location highPoint = area.getHighPoint().add(0.5, 0.5, 0.5);

                    Location thirdCorner = new Location(mine.getWorld(), minPoint.getX(), 95, highPoint.getZ());
                    Location fourthCorner = new Location(mine.getWorld(), highPoint.getX(), 95, minPoint.getZ());

                    // Line 1
                    drawLine(highPoint, fourthCorner, 95, 1.5, particle);
                    drawLine(thirdCorner, minPoint, 95, 1.5, particle);
                    drawLine(thirdCorner, highPoint, 95, 1.5, particle);
                    drawLine(fourthCorner, minPoint, 95, 1.5, particle);
                }
            }
        });
        execute();
    }

    private void drawLine(Location point1, Location point2, double y, double space, String particle) {
        World world = point1.getWorld();
        double distance = point2.distance(point1);
        Vector p1 = point1.toVector();
        Vector p2 = point2.toVector();
        Vector vector = p2.clone().subtract(p1).normalize().multiply(space);

        double len = 0.0;
        while (len < distance) {
            OParticle.getProvider().display(particle, new Location(world, p1.getX(), y, p1.getZ()), 1);
            len += space;
            p1.add(vector);
        }
    }
}
