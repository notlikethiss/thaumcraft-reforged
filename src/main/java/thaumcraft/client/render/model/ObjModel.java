package thaumcraft.client.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import thaumcraft.Thaumcraft;
import thaumcraft.client.render.TcRenderUtil;

public final class ObjModel {
    private final List<float[][]> faces;

    private ObjModel(List<float[][]> faces) {
        this.faces = faces;
    }

    public static ObjModel load(Identifier location) {
        List<float[]> positions = new ArrayList<>();
        List<float[]> uvs = new ArrayList<>();
        List<float[]> normals = new ArrayList<>();
        List<float[][]> faces = new ArrayList<>();
        try (BufferedReader reader = Minecraft.getInstance().getResourceManager().openAsReader(location)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                switch (parts[0]) {
                    case "v" -> positions.add(floats(parts, 3));
                    case "vt" -> uvs.add(floats(parts, 2));
                    case "vn" -> normals.add(floats(parts, 3));
                    case "f" -> {
                        int count = parts.length - 1;
                        float[][] face = new float[Math.max(count, 4)][];
                        for (int index = 0; index < face.length; index++) {
                            String[] indices = parts[1 + Math.min(index, count - 1)].split("/");
                            float[] position = positions.get(Integer.parseInt(indices[0]) - 1);
                            float[] uv = uvs.get(Integer.parseInt(indices[1]) - 1);
                            float[] normal = normals.get(Integer.parseInt(indices[2]) - 1);
                            face[index] = new float[] {position[0], position[1], position[2], uv[0], 1.0F - uv[1], normal[0], normal[1], normal[2]};
                        }
                        faces.add(face);
                    }
                    default -> {
                    }
                }
            }
        } catch (IOException | RuntimeException exception) {
            Thaumcraft.LOGGER.error("Failed to load model {}", location, exception);
        }
        return new ObjModel(faces);
    }

    private static float[] floats(String[] parts, int count) {
        float[] values = new float[count];
        for (int index = 0; index < count; index++) {
            values[index] = Float.parseFloat(parts[1 + index]);
        }
        return values;
    }

    public void render(PoseStack.Pose pose, VertexConsumer buffer, int light) {
        for (float[][] face : faces) {
            for (float[] vertex : face) {
                TcRenderUtil.vertex(pose, buffer, vertex[0], vertex[1], vertex[2], vertex[3], vertex[4], 0xFFFFFFFF, light, vertex[5], vertex[6], vertex[7]);
            }
        }
    }
}
