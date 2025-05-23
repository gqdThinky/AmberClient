package com.amberclient.modules.hacks;

import com.amberclient.modules.Module;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public class Xray extends Module {

    private final Set<Block> targetBlocks = new HashSet<>();
    private final int renderDistance = 16; // Réduit de 32 à 16
    private boolean isRegistered = false;
    private WorldRenderEvents.AfterTranslucent renderCallback;

    // Settings
    private boolean onlyExposed = false;
    private float opacity = 0.15f;
    private boolean fullbright = true;
    private double savedGamma = 1.0; // Sauvegarder la gamma originale

    // Cache pour l'optimisation
    private final ThreadLocal<BlockPos.Mutable> mutablePosCache = ThreadLocal.withInitial(BlockPos.Mutable::new);

    public Xray() {
        super("X-Ray", "Advanced X-Ray with transparency and exposure detection", "Render");
        initializeDefaultBlocks();

        // Créer le callback une seule fois
        this.renderCallback = this::renderXray;
    }

    private void initializeDefaultBlocks() {
        // Minerais de base seulement pour éviter la surcharge
        targetBlocks.add(Blocks.DIAMOND_ORE);
        targetBlocks.add(Blocks.DEEPSLATE_DIAMOND_ORE);
        targetBlocks.add(Blocks.IRON_ORE);
        targetBlocks.add(Blocks.DEEPSLATE_IRON_ORE);
        targetBlocks.add(Blocks.GOLD_ORE);
        targetBlocks.add(Blocks.DEEPSLATE_GOLD_ORE);
        targetBlocks.add(Blocks.EMERALD_ORE);
        targetBlocks.add(Blocks.DEEPSLATE_EMERALD_ORE);
        targetBlocks.add(Blocks.ANCIENT_DEBRIS);
        targetBlocks.add(Blocks.SPAWNER);
        targetBlocks.add(Blocks.CHEST);
        targetBlocks.add(Blocks.ENDER_CHEST);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        try {
            if (!isRegistered && renderCallback != null) {
                WorldRenderEvents.AFTER_TRANSLUCENT.register(renderCallback);
                isRegistered = true;
            }

            // Sauvegarder et activer le fullbright si nécessaire
            if (fullbright) {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.options != null) {
                    savedGamma = mc.options.getGamma().getValue();
                    setFullbright(true);
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'activation du X-Ray: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        try {
            // Désactiver le fullbright et restaurer la gamma originale
            if (fullbright) {
                setFullbright(false);
            }

            // Note: On ne désenregistre pas l'événement car cela peut causer des problèmes
            // On utilise plutôt la vérification isEnabled() dans renderXray
        } catch (Exception e) {
            System.err.println("Erreur lors de la désactivation du X-Ray: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void renderXray(WorldRenderContext context) {
        // Vérifications de sécurité critiques
        if (!isEnabled() || context == null) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.world.isClient == false) return;

        // Limiter les mises à jour pour les performances (plus conservateur)
        if (mc.player.age % 5 != 0) return;

        try {
            MatrixStack matrices = context.matrixStack();
            VertexConsumerProvider vertexConsumers = context.consumers();

            if (matrices == null || vertexConsumers == null) return;

            Vec3d cameraPos = context.camera().getPos();
            if (cameraPos == null) return;

            matrices.push();
            matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

            BlockPos playerPos = mc.player.getBlockPos();
            World world = mc.world;

            // Rendu très optimisé avec limites strictes
            int processed = 0;
            int maxBlocks = 500; // Réduit drastiquement
            int step = 2; // Sauter des blocs pour les performances

            for (int x = -renderDistance; x <= renderDistance && processed < maxBlocks; x += step) {
                for (int y = -renderDistance; y <= renderDistance && processed < maxBlocks; y += step) {
                    for (int z = -renderDistance; z <= renderDistance && processed < maxBlocks; z += step) {
                        BlockPos pos = playerPos.add(x, y, z);

                        // Vérifications de sécurité
                        if (pos == null || !world.isChunkLoaded(pos)) continue;

                        try {
                            BlockState blockState = world.getBlockState(pos);
                            if (blockState == null) continue;

                            Block block = blockState.getBlock();
                            if (block == null) continue;

                            if (isVisible(block, pos, world)) {
                                renderBlockHighlight(matrices, vertexConsumers, pos, getColorForBlock(block));
                                processed++;
                            }
                        } catch (Exception e) {
                            // Ignorer les erreurs de blocs individuels
                            continue;
                        }
                    }
                }
            }

            matrices.pop();
        } catch (Exception e) {
            System.err.println("Erreur dans renderXray: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isVisible(Block block, BlockPos pos, World world) {
        if (block == null || !targetBlocks.contains(block)) {
            return false;
        }

        // Si "only exposed" est activé, vérifier si le bloc est exposé
        if (onlyExposed && pos != null && world != null) {
            try {
                return isExposed(pos, world);
            } catch (Exception e) {
                return true; // En cas d'erreur, on affiche le bloc
            }
        }

        return true;
    }

    private boolean isExposed(BlockPos pos, World world) {
        if (pos == null || world == null) return true;

        try {
            BlockPos.Mutable mutablePos = mutablePosCache.get();
            if (mutablePos == null) return true;

            for (Direction direction : Direction.values()) {
                mutablePos.set(pos, direction);
                BlockState neighborState = world.getBlockState(mutablePos);

                if (neighborState == null) continue;

                // Si un côté n'est pas un cube opaque complet, le bloc est exposé
                if (!neighborState.isOpaque() || !neighborState.isFullCube(world, mutablePos)) {
                    return true;
                }
            }
        } catch (Exception e) {
            return true; // En cas d'erreur, considérer comme exposé
        }

        return false;
    }

    private void renderBlockHighlight(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                      BlockPos pos, float[] color) {
        if (matrices == null || vertexConsumers == null || pos == null || color == null) return;

        try {
            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getLines());
            if (vertexConsumer == null) return;

            double x1 = pos.getX();
            double y1 = pos.getY();
            double z1 = pos.getZ();
            double x2 = pos.getX() + 1.0;
            double y2 = pos.getY() + 1.0;
            double z2 = pos.getZ() + 1.0;

            // Rendu avec opacité (simplifié)
            float alpha = Math.max(0.4f, opacity);
            drawBoxOutline(matrices, vertexConsumer, x1, y1, z1, x2, y2, z2,
                    color[0], color[1], color[2], alpha);
        } catch (Exception e) {
            // Ignorer les erreurs de rendu individuelles
        }
    }

    private void drawBoxOutline(MatrixStack matrices, VertexConsumer vertexConsumer,
                                double x1, double y1, double z1, double x2, double y2, double z2,
                                float red, float green, float blue, float alpha) {
        if (matrices == null || vertexConsumer == null) return;

        try {
            MatrixStack.Entry entry = matrices.peek();
            if (entry == null) return;

            // Seulement les arêtes principales pour les performances
            // Arêtes du bas
            vertex(vertexConsumer, entry, (float)x1, (float)y1, (float)z1, red, green, blue, alpha);
            vertex(vertexConsumer, entry, (float)x2, (float)y1, (float)z1, red, green, blue, alpha);

            vertex(vertexConsumer, entry, (float)x2, (float)y1, (float)z1, red, green, blue, alpha);
            vertex(vertexConsumer, entry, (float)x2, (float)y1, (float)z2, red, green, blue, alpha);

            vertex(vertexConsumer, entry, (float)x2, (float)y1, (float)z2, red, green, blue, alpha);
            vertex(vertexConsumer, entry, (float)x1, (float)y1, (float)z2, red, green, blue, alpha);

            vertex(vertexConsumer, entry, (float)x1, (float)y1, (float)z2, red, green, blue, alpha);
            vertex(vertexConsumer, entry, (float)x1, (float)y1, (float)z1, red, green, blue, alpha);

            // Arêtes du haut
            vertex(vertexConsumer, entry, (float)x1, (float)y2, (float)z1, red, green, blue, alpha);
            vertex(vertexConsumer, entry, (float)x2, (float)y2, (float)z1, red, green, blue, alpha);

            vertex(vertexConsumer, entry, (float)x2, (float)y2, (float)z1, red, green, blue, alpha);
            vertex(vertexConsumer, entry, (float)x2, (float)y2, (float)z2, red, green, blue, alpha);

            vertex(vertexConsumer, entry, (float)x2, (float)y2, (float)z2, red, green, blue, alpha);
            vertex(vertexConsumer, entry, (float)x1, (float)y2, (float)z2, red, green, blue, alpha);

            vertex(vertexConsumer, entry, (float)x1, (float)y2, (float)z2, red, green, blue, alpha);
            vertex(vertexConsumer, entry, (float)x1, (float)y2, (float)z1, red, green, blue, alpha);

            // Arêtes verticales
            vertex(vertexConsumer, entry, (float)x1, (float)y1, (float)z1, red, green, blue, alpha);
            vertex(vertexConsumer, entry, (float)x1, (float)y2, (float)z1, red, green, blue, alpha);

            vertex(vertexConsumer, entry, (float)x2, (float)y1, (float)z1, red, green, blue, alpha);
            vertex(vertexConsumer, entry, (float)x2, (float)y2, (float)z1, red, green, blue, alpha);

            vertex(vertexConsumer, entry, (float)x2, (float)y1, (float)z2, red, green, blue, alpha);
            vertex(vertexConsumer, entry, (float)x2, (float)y2, (float)z2, red, green, blue, alpha);

            vertex(vertexConsumer, entry, (float)x1, (float)y1, (float)z2, red, green, blue, alpha);
            vertex(vertexConsumer, entry, (float)x1, (float)y2, (float)z2, red, green, blue, alpha);
        } catch (Exception e) {
            // Ignorer les erreurs de vertex
        }
    }

    private void vertex(VertexConsumer consumer, MatrixStack.Entry entry,
                        float x, float y, float z, float r, float g, float b, float a) {
        try {
            consumer.vertex(entry, x, y, z).color(r, g, b, a);
        } catch (Exception e) {
            // Ignorer les erreurs de vertex individuelles
        }
    }

    private float[] getColorForBlock(Block block) {
        if (block == null) return new float[]{1.0f, 1.0f, 1.0f, 1.0f};

        // Couleurs simplifiées
        if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE) {
            return new float[]{0.0f, 1.0f, 1.0f, 1.0f}; // Cyan
        } else if (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE) {
            return new float[]{0.0f, 1.0f, 0.0f, 1.0f}; // Vert
        } else if (block == Blocks.ANCIENT_DEBRIS) {
            return new float[]{0.8f, 0.0f, 0.8f, 1.0f}; // Violet
        } else if (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE) {
            return new float[]{0.9f, 0.9f, 0.9f, 1.0f}; // Gris clair
        } else if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE) {
            return new float[]{1.0f, 1.0f, 0.0f, 1.0f}; // Jaune
        } else if (block == Blocks.SPAWNER) {
            return new float[]{0.5f, 0.0f, 0.5f, 1.0f}; // Violet foncé
        } else if (block == Blocks.CHEST) {
            return new float[]{0.8f, 0.6f, 0.2f, 1.0f}; // Brun doré
        } else if (block == Blocks.ENDER_CHEST) {
            return new float[]{0.2f, 0.8f, 0.8f, 1.0f}; // Cyan foncé
        }

        return new float[]{1.0f, 1.0f, 1.0f, 1.0f}; // Blanc par défaut
    }

    private void setFullbright(boolean enabled) {
        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.options != null) {
                if (enabled) {
                    mc.options.getGamma().setValue(16.0);
                } else {
                    // Restaurer la gamma sauvegardée
                    mc.options.getGamma().setValue(savedGamma);
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du changement de gamma: " + e.getMessage());
        }
    }

    // Méthodes de configuration avec validation
    public void setOnlyExposed(boolean onlyExposed) {
        this.onlyExposed = onlyExposed;
    }

    public void setOpacity(float opacity) {
        this.opacity = Math.max(0.0f, Math.min(1.0f, opacity));
    }

    public void setFullbrightEnabled(boolean fullbright) {
        boolean wasFullbright = this.fullbright;
        this.fullbright = fullbright;

        if (isEnabled() && wasFullbright != fullbright) {
            if (fullbright) {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.options != null) {
                    savedGamma = mc.options.getGamma().getValue();
                }
            }
            setFullbright(fullbright);
        }
    }

    // Méthodes de gestion des blocs avec validation
    public void addBlock(Block block) {
        if (block != null) {
            targetBlocks.add(block);
        }
    }

    public void removeBlock(Block block) {
        if (block != null) {
            targetBlocks.remove(block);
        }
    }

    public Set<Block> getTargetBlocks() {
        return new HashSet<>(targetBlocks);
    }

    public boolean isOnlyExposed() {
        return onlyExposed;
    }

    public float getOpacity() {
        return opacity;
    }

    public boolean isFullbrightEnabled() {
        return fullbright;
    }
}