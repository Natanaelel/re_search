package natte.re_search.particle.custom;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

public class CustomParticle extends SpriteBillboardParticle {

    private int fadeInTicks;
    private int fadeOutTicks;


    protected CustomParticle(ClientWorld clientWorld, double x, double y, double z, SpriteProvider spriteSet, double xd,
            double yd, double zd) {
        super(clientWorld, x, y, z, 0, 0, 0);

        this.velocityMultiplier = 0;
        this.x = x;
        this.y = y;
        this.z = z;
        // this.scale *= 0.5f;
        this.age = 0;
        this.fadeInTicks = 3;
        this.fadeOutTicks = 10;
;
        this.maxAge = (int) (5 * 20 + this.random.nextFloat() * 10);
        this.setSpriteForAge(spriteSet);
        this.alpha = 0;
        // this.red = 1f;
        // this.green = 1f;
        // this.blue = 1;
        this.collidesWithWorld = false;
    }
    
    @Override
    public void tick() {
        if (this.age++ >= this.maxAge) {
            this.markDead();
            return;
        }
        if (this.age <= this.fadeInTicks) {
            this.alpha = 1 - (float)(this.fadeInTicks - age) / this.fadeInTicks;
        }
        if (this.age >= this.maxAge - this.fadeOutTicks) {
            this.alpha = (float)(this.maxAge - age) / this.fadeOutTicks;
        }
    }



    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleFactory<DefaultParticleType> {

        private final SpriteProvider sprites;

        public Factory(SpriteProvider spriteSet) {
            this.sprites = spriteSet;
        }

        public Particle createParticle(DefaultParticleType particleType, ClientWorld world, double x, double y,
                double z, double xd, double yd, double zd) {
            return new CustomParticle(world, x, y, z, sprites, xd, yd, zd);
        }
    }

}
