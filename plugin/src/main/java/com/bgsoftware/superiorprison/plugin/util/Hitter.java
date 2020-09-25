package com.bgsoftware.superiorprison.plugin.util;

import com.oop.orangeengine.main.events.SyncEvents;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

public class Hitter {
    static {
        SyncEvents.listen(ProjectileHitEvent.class, event -> {
            if (event.getEntity().getShooter() instanceof Tracker)
                ((Tracker) event.getEntity().getShooter()).onHit();
        });
    }

    public static void listenForHit(Projectile projectile, Runnable onHit) {
        final ProjectileSource shooter = projectile.getShooter();
        projectile.setShooter(new Tracker() {
            @Override
            public void onHit() {
                onHit.run();
            }

            @Override
            public <T extends Projectile> T launchProjectile(Class<? extends T> aClass) {
                return shooter.launchProjectile(aClass);
            }

            @Override
            public <T extends Projectile> T launchProjectile(Class<? extends T> aClass, Vector vector) {
                return shooter.launchProjectile(aClass, vector);
            }
        });
    }

    private interface Tracker extends ProjectileSource {
        void onHit();
    }

}
