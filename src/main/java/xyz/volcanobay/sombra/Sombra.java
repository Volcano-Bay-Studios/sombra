package xyz.volcanobay.sombra;

import com.mojang.logging.LogUtils;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import foundry.veil.api.client.render.shader.uniform.ShaderUniform;
import foundry.veil.platform.VeilEventPlatform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Sombra.MODID)
public class Sombra {
    public static final String MODID = "sombra";
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final ResourceLocation COHESION_PIPELINE = id("cohesion");
    private static final ResourceLocation COHESION_SHADER = id("cohesion");

    private static float power = 0;

    public Sombra(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        VeilEventPlatform.INSTANCE.preVeilPostProcessing((pipelineName, pipeline, context) -> {
            if (COHESION_PIPELINE.equals(pipelineName)) {
//                ShaderProgram shader = context.getShader(COHESION_SHADER);
//                if (shader != null) { // Set uniforms somehow
//                    updateUniforms(shader);
//                }
            }
        });

        VeilEventPlatform.INSTANCE.onVeilRenderLevelStage(((stage, levelRenderer, bufferSource, matrixStack, frustumMatrix, projectionMatrix, renderTick, deltaTracker, camera, frustum) -> {
            if (!VeilRenderSystem.renderer().getPostProcessingManager().isActive(COHESION_PIPELINE)) {
                VeilRenderSystem.renderer().getPostProcessingManager().add(COHESION_PIPELINE);
            }

            ShaderProgram shader = VeilRenderSystem.renderer().getShaderManager().getShader(COHESION_SHADER);
            if (shader != null) { // Set uniforms somehow
                updateUniforms(shader, deltaTracker.getGameTimeDeltaPartialTick(false));
            }
        }));
    }


    public void updateUniforms(ShaderProgram shader, float partial) {
        ShaderUniform shaderUniform = shader.getUniform("powerUniform");
        if (shaderUniform != null) {
            Minecraft minecraft = Minecraft.getInstance();
            float distance = 0;
            if (minecraft.player != null) {
                distance = (float) minecraft.player.getPosition(partial).distanceTo(new Vec3(0, 56, 0));
            }
            shaderUniform.setFloat(power);
        }
    }


    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
        }
    }

    @EventBusSubscriber(modid = MODID)
    public static class ModEvents {
        @SubscribeEvent
        public static void tickLevel(LevelTickEvent.Pre event) {
            if (event.getLevel().isClientSide) {
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.player != null) {
                    float target = minecraft.player.isHolding(Items.ECHO_SHARD) ? 1 : 0;

                    power += target - power / 16f;
                }
            } else if (event.getLevel() instanceof ServerLevel level) {
                for (Player player : level.players()) {
                }
            }
        }

        @SubscribeEvent
        public static void playerAttack(AttackEntityEvent event) {
            if (!(event.getTarget() instanceof Player player && Utils.isInvisible(player))) {
                incohesiveInteraction(event.getEntity());
            }
        }

        @SubscribeEvent
        public static void playerInteract(PlayerInteractEvent.EntityInteract event) {
            if (!(event.getTarget() instanceof Player player && Utils.isInvisible(player))) {
                incohesiveInteraction(event.getEntity());
            }
        }

        @SubscribeEvent
        public static void playerInteract(PlayerInteractEvent.RightClickItem event) {
            incohesiveInteraction(event.getEntity());
        }

        @SubscribeEvent
        public static void playerInteract(PlayerInteractEvent.EntityInteractSpecific event) {
            if (!(event.getTarget() instanceof Player player && Utils.isInvisible(player))) {
                incohesiveInteraction(event.getEntity());
            }
        }

        @SubscribeEvent
        public static void playerInteract(PlayerInteractEvent.RightClickBlock event) {
            incohesiveInteraction(event.getEntity());
        }

        @SubscribeEvent
        public static void playerInteract(BlockEvent.BreakEvent event) {
            incohesiveInteraction(event.getPlayer());
        }

        @SubscribeEvent
        public static void playerInteract(BlockEvent.EntityPlaceEvent event) {
            if (event.getEntity() instanceof Player player) {
                incohesiveInteraction(player);
            }
        }

        public static void incohesiveInteraction(Player player) {
            if (Utils.isInvisible(player)) {
                player.hurt(player.damageSources().magic(), 1);
            }
        }
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
