package natte.re_search.particle;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModParticles {
    public static final DefaultParticleType CUSTOM_PARTICLE = FabricParticleTypes.simple(true);
    
    public static void registerParticles(){
        Registry.register(Registries.PARTICLE_TYPE, new Identifier("re_search", "custom_particle"), CUSTOM_PARTICLE);
    }
}
