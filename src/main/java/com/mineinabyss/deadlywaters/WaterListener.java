package com.mineinabyss.deadlywaters;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.UUID;

public class WaterListener implements Listener {
    private DeadlyWaters plugin;
    private FileConfiguration config;

    private double checkRange = 0.3; //multiplier for blocks around player to be checked
    private double fallRange = 1.2; //block distance to check for player to be pushed while falling in water
    private double pushVelocity = 0.2; //velocity to push players off horizontally in waterfall

    public WaterListener(DeadlyWaters plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();

        checkRange = config.getDouble("checkRange");
        fallRange = config.getDouble("fallRange");
        pushVelocity = config.getDouble("pushVelocity");
    }

    @EventHandler
    public void onPlayerWaterFall(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (p.getAllowFlight()) return; //do not run if player is flying

        Vector v = p.getVelocity();
        Location l = p.getLocation();
        double y = v.getY();
        UUID uuid = p.getUniqueId();
        float fallDistance = p.getFallDistance();

        for (int x = -1; x <= 1; x += 2) { //check in a 2x2 area around player
            for (int z = -1; z <= 1; z += 2) {
                //Fall damage from falling in water
                Location getTo = e.getTo();
                Location to = new Location(p.getWorld(), getTo.getX(), getTo.getY(), getTo.getZ()).add(x * checkRange, 0, z * checkRange); //if we set this directly to e.getTo(), it will change player location
                Block block = to.getBlock();
//                Material type = block.getType();
                if (fallDistance > 5 && (block.isLiquid()
//                        || type.equals(Material.LADDER) //TODO do we keep this or not?
//                        || type.equals(Material.VINE))
                        //for some reason cobwebs cancel the fall distance before we can see them in the getTo, so we just check for cobwebs half a block lower
                        /*|| to.clone().add(0, -0.5, 0).getBlock().getType().equals(Material.COBWEB)*/)) {
                    p.damage(fallDistance / 2 - 3); //just over half the damage of regular fall (calculated as falldistance - 3)
                }

                //Waterfalls
                Block currentBlock = p.getLocation().add(x * checkRange, 0, z * checkRange).getBlock();
                Block upperWater = p.getLocation().add(x * checkRange, 5, z * checkRange).getBlock();

                if (currentBlock.isLiquid() && upperWater.isLiquid()) { //check if blocks around player, and above are water
                    int level = ((Levelled) currentBlock.getBlockData()).getLevel();

                    if (level == 9 || level == 8) { //check if water is flowing, and not just deep water
                        double vYFunction = -1.25 * Math.abs(y) - 0.03 - y;
                        double vXFunction = (Math.random() - 0.5) / 10;
                        p.setVelocity(p.getVelocity().add(new Vector(0, vYFunction, 0))); //push player down in waterfall
                        if (p.getLocation().add(x * checkRange, -1, z * checkRange).getBlock().getType().isSolid()) { //push player away from wall while falling in waterfall
                            v = p.getVelocity(); //update velocity
                            if (p.getLocation().add(-fallRange, 0, 0).getBlock().getType().isSolid())
                                p.setVelocity(v.setX(pushVelocity).setY(0.1));
                            if (p.getLocation().add(fallRange, 0, 0).getBlock().getType().isSolid())
                                p.setVelocity(v.setX(-pushVelocity).setY(0.1));
                            if (p.getLocation().add(0, 0, -fallRange).getBlock().getType().isSolid())
                                p.setVelocity(v.setZ(pushVelocity).setY(0.1));
                            if (p.getLocation().add(0, 0, fallRange).getBlock().getType().isSolid())
                                p.setVelocity(v.setZ(-pushVelocity).setY(0.1));
                        }
                        p.getWorld().spawnParticle(Particle.CLOUD, l, 2);
                        if (y < -0.1)
                            p.damage(2 * y * y); //damage player
                        return;
                    }
                }
            }
        }
    }
}