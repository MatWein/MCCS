import de.mccs.core.render.CrystalRender;
import de.mccs.core.render.VertexSink;
import de.mccs.core.staff.StaffKind;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders the staff's crystal (de.mccs.core.render.CrystalRender, the same code the mod uses) to an image with a small
 * software rasterizer, to judge its look without starting Minecraft: several views, at night and by day, both kinds.
 *
 * <pre>
 * ./gradlew :core:classes
 * java -cp core/build/classes/java/main tools/CrystalPreview.java preview.png [time]
 * </pre>
 */
public final class CrystalPreview {
    private static final String ASSETS = "core/src/main/resources/assets/mccs/textures/entity/";
    private static final int SIZE = 420;
    private static final float FOV = 40f;

    public static void main(String[] args) throws IOException {
        String out = args.length > 0 ? args[0] : "preview.png";
        float time = args.length > 1 ? Float.parseFloat(args[1]) : 37f;
        Texture crystal = Texture.load(ASSETS + "crystal.png", false);
        Texture glow = Texture.load(ASSETS + "crystal_glow.png", true);
        Texture glint = Texture.load(ASSETS + "glint.png", true);
        Texture beam = Texture.load(ASSETS + "beam.png", false);

        float[][] views = {{-12, 25}, {-30, 100}, {8, -40}, {-5, 60, 0.75f}};
        float[][] skies = {{0.015f, 0.018f, 0.035f}, {0.42f, 0.58f, 0.85f}};
        StaffKind[] kinds = StaffKind.values();
        int columns = views.length;
        int rows = skies.length * kinds.length;
        BufferedImage image = new BufferedImage(SIZE * columns, SIZE * rows, BufferedImage.TYPE_INT_RGB);
        int row = 0;
        for (StaffKind kind : kinds) {
            for (float[] sky : skies) {
                for (int c = 0; c < columns; c++) {
                    Frame frame = new Frame(SIZE, sky);
                    float[] pose = pose(views[c][0], views[c][1], views[c].length > 2 ? views[c][2] : 1.45f);
                    frame.drawStaff(pose);
                    var state = new CrystalRender.State(kind, 100, 100, time, 7, false);
                    List<Quad> body = new ArrayList<>();
                    List<Quad> lights = new ArrayList<>();
                    CrystalRender.draw(state, pose, new float[]{1, 0, 0}, new float[]{0, 1, 0}, new CrystalRender.Output() {
                        @Override
                        public void body(java.util.function.Consumer<VertexSink> emitter) {
                            emitter.accept(collect(body, pose, crystal));
                        }

                        @Override
                        public void light(CrystalRender.Light light, java.util.function.Consumer<VertexSink> emitter) {
                            Texture texture = switch (light) {
                                case GLOW -> glow;
                                case GLINT -> glint;
                                case BEAM -> beam;
                            };
                            emitter.accept(collect(lights, null, texture));
                        }
                    });
                    for (Quad q : body) frame.draw(q, false);
                    for (Quad q : lights) frame.draw(q, true);
                    frame.copyTo(image, c * SIZE, row * SIZE);
                }
                row++;
            }
        }
        ImageIO.write(image, "png", new File(out));
        System.out.println("wrote " + out);
    }

    /** Item model space to view space: the crystal in front of the camera, turned by pitch and yaw (degrees). */
    static float[] pose(float pitch, float yaw, float distance) {
        float[] m = identity();
        m = mul(m, translation(0, 0, -distance));
        m = mul(m, rotationX((float) Math.toRadians(-pitch)));
        m = mul(m, rotationY((float) Math.toRadians(yaw)));
        m = mul(m, translation(-0.5f, -20.4f / 16, -0.5f));
        return m;
    }

    record Vertex(float x, float y, float z, float r, float g, float b, float u, float v) {}

    record Quad(Vertex[] vertices, Texture texture) {}

    static VertexSink collect(List<Quad> quads, float[] pose, Texture texture) {
        List<Vertex> pending = new ArrayList<>();
        return (x, y, z, r, g, b, a, u, v, nx, ny, nz) -> {
            float[] p = pose == null ? new float[]{x, y, z} : apply(pose, x, y, z);
            pending.add(new Vertex(p[0], p[1], p[2], r, g, b, u, v));
            if (pending.size() == 4) {
                quads.add(new Quad(pending.toArray(new Vertex[0]), texture));
                pending.clear();
            }
        };
    }

    static final class Frame {
        final int size;
        final float[] color;
        final float[] depth;
        final float focal;

        Frame(int size, float[] sky) {
            this.size = size;
            color = new float[size * size * 3];
            depth = new float[size * size];
            java.util.Arrays.fill(depth, Float.MAX_VALUE);
            for (int i = 0; i < size * size; i++) {
                float shade = 1f - 0.25f * (i / size) / (float) size;
                color[i * 3] = sky[0] * shade;
                color[i * 3 + 1] = sky[1] * shade;
                color[i * 3 + 2] = sky[2] * shade;
            }
            focal = (float) (size / 2 / Math.tan(Math.toRadians(FOV / 2)));
        }

        /** A plain stand-in for the staff's head and shaft below the crystal (dark metal), so the crystal is seen in context. */
        void drawStaff(float[] pose) {
            float[] metal = {0.16f, 0.17f, 0.2f};
            float[] rim = {0.35f, 0.75f, 0.9f};
            column(pose, 0.75f, -8, 14.5f, metal);
            column(pose, 1.1f, 14.5f, 15.4f, new float[]{0.55f, 0.57f, 0.6f});
            column(pose, 1.6f, 15.4f, 16.1f, metal);
            column(pose, 1.15f, 16.1f, 16.45f, rim);
        }

        private void column(float[] pose, float radius, float bottom, float top, float[] base) {
            int sides = 12;
            for (int k = 0; k < sides; k++) {
                double a0 = Math.PI * 2 * k / sides, a1 = Math.PI * 2 * (k + 1) / sides;
                float shade = 0.6f + 0.4f * (float) Math.cos(a0 - 0.8);
                float[] c = {base[0] * shade, base[1] * shade, base[2] * shade};
                Vertex[] v = {
                        vertex(pose, a0, radius, bottom, c), vertex(pose, a1, radius, bottom, c),
                        vertex(pose, a1, radius, top, c), vertex(pose, a0, radius, top, c)};
                draw(new Quad(v, null), false);
                Vertex[] cap = {vertex(pose, a0, radius, top, base), vertex(pose, a1, radius, top, base), vertex(pose, a1, 0, top, base), vertex(pose, a0, 0, top, base)};
                draw(new Quad(cap, null), false);
            }
        }

        private Vertex vertex(float[] pose, double angle, float radius, float y, float[] c) {
            float[] p = apply(pose, 0.5f + (float) Math.cos(angle) * radius / 16, y / 16, 0.5f + (float) Math.sin(angle) * radius / 16);
            return new Vertex(p[0], p[1], p[2], c[0], c[1], c[2], 0, 0);
        }

        void draw(Quad quad, boolean additive) {
            Vertex[] v = quad.vertices();
            triangle(v[0], v[1], v[2], quad.texture(), additive);
            triangle(v[0], v[2], v[3], quad.texture(), additive);
        }

        private void triangle(Vertex a, Vertex b, Vertex c, Texture texture, boolean additive) {
            if (a.z() >= -0.01f || b.z() >= -0.01f || c.z() >= -0.01f) return;
            float[] pa = project(a), pb = project(b), pc = project(c);
            float area = (pb[0] - pa[0]) * (pc[1] - pa[1]) - (pb[1] - pa[1]) * (pc[0] - pa[0]);
            if (Math.abs(area) < 1e-6f) return;
            int minX = Math.max(0, (int) Math.floor(Math.min(pa[0], Math.min(pb[0], pc[0]))));
            int maxX = Math.min(size - 1, (int) Math.ceil(Math.max(pa[0], Math.max(pb[0], pc[0]))));
            int minY = Math.max(0, (int) Math.floor(Math.min(pa[1], Math.min(pb[1], pc[1]))));
            int maxY = Math.min(size - 1, (int) Math.ceil(Math.max(pa[1], Math.max(pb[1], pc[1]))));
            for (int y = minY; y <= maxY; y++) {
                for (int x = minX; x <= maxX; x++) {
                    float px = x + 0.5f, py = y + 0.5f;
                    float w0 = ((pb[0] - px) * (pc[1] - py) - (pb[1] - py) * (pc[0] - px)) / area;
                    float w1 = ((pc[0] - px) * (pa[1] - py) - (pc[1] - py) * (pa[0] - px)) / area;
                    float w2 = 1 - w0 - w1;
                    if (w0 < 0 || w1 < 0 || w2 < 0) continue;
                    // perspective correct interpolation
                    float iw = w0 * pa[2] + w1 * pb[2] + w2 * pc[2];
                    float z = 1 / iw;
                    int i = y * size + x;
                    if (z > depth[i] + 1e-4f) continue;
                    float k0 = w0 * pa[2] * z, k1 = w1 * pb[2] * z, k2 = w2 * pc[2] * z;
                    float r = k0 * a.r() + k1 * b.r() + k2 * c.r();
                    float g = k0 * a.g() + k1 * b.g() + k2 * c.g();
                    float bl = k0 * a.b() + k1 * b.b() + k2 * c.b();
                    float t = 1f;
                    if (texture != null) {
                        float u = k0 * a.u() + k1 * b.u() + k2 * c.u();
                        float v = k0 * a.v() + k1 * b.v() + k2 * c.v();
                        t = texture.sample(u, v);
                    }
                    if (additive) {
                        color[i * 3] += r * t;
                        color[i * 3 + 1] += g * t;
                        color[i * 3 + 2] += bl * t;
                    } else {
                        color[i * 3] = r * t;
                        color[i * 3 + 1] = g * t;
                        color[i * 3 + 2] = bl * t;
                        depth[i] = z;
                    }
                }
            }
        }

        /** Screen x, y and 1/depth. */
        private float[] project(Vertex v) {
            float d = -v.z();
            return new float[]{size / 2f + v.x() / d * focal, size / 2f - v.y() / d * focal, 1 / d};
        }

        void copyTo(BufferedImage image, int ox, int oy) {
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    int i = (y * size + x) * 3;
                    int r = (int) (Math.min(1f, color[i]) * 255), g = (int) (Math.min(1f, color[i + 1]) * 255), b = (int) (Math.min(1f, color[i + 2]) * 255);
                    image.setRGB(ox + x, oy + y, r << 16 | g << 8 | b);
                }
            }
        }
    }

    /** A grey texture (its red channel), sampled nearest or bilinear like Minecraft with and without "blur". */
    record Texture(int width, int height, float[] values, boolean smooth) {
        static Texture load(String path, boolean smooth) throws IOException {
            BufferedImage image = ImageIO.read(new File(path));
            float[] values = new float[image.getWidth() * image.getHeight()];
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) values[y * image.getWidth() + x] = ((image.getRGB(x, y) >> 16) & 0xFF) / 255f;
            }
            return new Texture(image.getWidth(), image.getHeight(), values, smooth);
        }

        float sample(float u, float v) {
            float x = u * width - (smooth ? 0.5f : 0), y = v * height - (smooth ? 0.5f : 0);
            if (!smooth) return at((int) Math.floor(x), (int) Math.floor(y));
            int x0 = (int) Math.floor(x), y0 = (int) Math.floor(y);
            float fx = x - x0, fy = y - y0;
            return at(x0, y0) * (1 - fx) * (1 - fy) + at(x0 + 1, y0) * fx * (1 - fy) + at(x0, y0 + 1) * (1 - fx) * fy + at(x0 + 1, y0 + 1) * fx * fy;
        }

        private float at(int x, int y) {
            x = Math.max(0, Math.min(width - 1, x));
            y = Math.max(0, Math.min(height - 1, y));
            return values[y * width + x];
        }
    }

    // ---- column-major 4x4 matrices

    static float[] identity() {
        return new float[]{1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1};
    }

    static float[] translation(float x, float y, float z) {
        float[] m = identity();
        m[12] = x;
        m[13] = y;
        m[14] = z;
        return m;
    }

    static float[] rotationX(float a) {
        float c = (float) Math.cos(a), s = (float) Math.sin(a);
        return new float[]{1, 0, 0, 0, 0, c, s, 0, 0, -s, c, 0, 0, 0, 0, 1};
    }

    static float[] rotationY(float a) {
        float c = (float) Math.cos(a), s = (float) Math.sin(a);
        return new float[]{c, 0, -s, 0, 0, 1, 0, 0, s, 0, c, 0, 0, 0, 0, 1};
    }

    static float[] mul(float[] a, float[] b) {
        float[] r = new float[16];
        for (int col = 0; col < 4; col++) {
            for (int rowIndex = 0; rowIndex < 4; rowIndex++) {
                float sum = 0;
                for (int k = 0; k < 4; k++) sum += a[k * 4 + rowIndex] * b[col * 4 + k];
                r[col * 4 + rowIndex] = sum;
            }
        }
        return r;
    }

    static float[] apply(float[] m, float x, float y, float z) {
        return new float[]{m[0] * x + m[4] * y + m[8] * z + m[12], m[1] * x + m[5] * y + m[9] * z + m[13], m[2] * x + m[6] * y + m[10] * z + m[14]};
    }
}
