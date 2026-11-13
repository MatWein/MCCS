import de.mccs.core.render.CrystalRender;
import de.mccs.core.render.VertexSink;
import de.mccs.core.staff.StaffKind;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Renders the staff's crystal (de.mccs.core.render.CrystalRender, the same code the mod uses) to an image with a small
 * software rasterizer, to judge its look without starting Minecraft: several views, at night and by day, both kinds.
 *
 * <pre>
 * ./gradlew :core:classes
 * java -cp core/build/classes/java/main tools/CrystalPreview.java preview.png [time]
 * java -cp core/build/classes/java/main tools/CrystalPreview.java --logo [logo.png]
 * </pre>
 * With {@code --logo} it renders the mod icon instead (core/.../assets/mccs/icon.png, 256x256), and the same picture in
 * 512x512 to the given file (the CurseForge project logo).
 */
public final class CrystalPreview {
    private static final String ASSETS = "core/src/main/resources/assets/mccs/textures/entity/";
    private static final int SIZE = 420;
    private static final float FOV = 40f;

    public static void main(String[] args) throws IOException {
        if (args.length > 0 && args[0].equals("--logo")) {
            Logo.render(args.length > 1 ? args[1] : null);
            return;
        }
        String out = args.length > 0 ? args[0] : "preview.png";
        float time = args.length > 1 ? Float.parseFloat(args[1]) : 37f;

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
                    drawCrystal(frame, new CrystalRender.State(kind, 100, 100, time, 7, false), pose);
                    frame.copyTo(image, c * SIZE, row * SIZE);
                }
                row++;
            }
        }
        ImageIO.write(image, "png", new File(out));
        System.out.println("wrote " + out);
    }

    private static Texture crystal, glow, glint, beam;

    /** The crystal with all of its light, drawn by the mod's own code; {@code pose} maps item model space to view space. */
    static void drawCrystal(Frame frame, CrystalRender.State state, float[] pose) throws IOException {
        if (crystal == null) {
            crystal = Texture.load(ASSETS + "crystal.png", false);
            glow = Texture.load(ASSETS + "crystal_glow.png", true);
            glint = Texture.load(ASSETS + "glint.png", true);
            beam = Texture.load(ASSETS + "beam.png", false);
        }
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
    }

    /** Item model space to view space: the crystal in front of the camera, turned by pitch and yaw (degrees). */
    static float[] pose(float pitch, float yaw, float distance) {
        float[] m = identity();
        m = mul(m, translation(0, 0, -distance));
        m = mul(m, rotationX((float) Math.toRadians(-pitch)));
        m = mul(m, rotationY((float) Math.toRadians(yaw)));
        m = mul(m, translation(-0.5f, -26.8f / 16, -0.5f));
        return m;
    }

    record Vertex(float x, float y, float z, float r, float g, float b, float u, float v) {}

    record Quad(Vertex[] vertices, Texture texture, ColorTexture colors) {
        Quad(Vertex[] vertices, Texture texture) {
            this(vertices, texture, null);
        }
    }

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
            column(pose, 0.75f, -8, 20.9f, metal);
            column(pose, 1.1f, 20.9f, 21.8f, new float[]{0.55f, 0.57f, 0.6f});
            column(pose, 1.6f, 21.8f, 22.5f, metal);
            column(pose, 1.15f, 22.5f, 22.85f, rim);
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
            triangle(v[0], v[1], v[2], quad.texture(), quad.colors(), additive);
            triangle(v[0], v[2], v[3], quad.texture(), quad.colors(), additive);
        }

        private void triangle(Vertex a, Vertex b, Vertex c, Texture texture, ColorTexture colors, boolean additive) {
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
                    float u = k0 * a.u() + k1 * b.u() + k2 * c.u();
                    float v = k0 * a.v() + k1 * b.v() + k2 * c.v();
                    if (texture != null) t = texture.sample(u, v);
                    if (colors != null) {
                        float[] rgb = colors.sample(u, v);
                        r *= rgb[0];
                        g *= rgb[1];
                        bl *= rgb[2];
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

    /** A colored texture, sampled nearest like Minecraft's item textures. */
    record ColorTexture(int width, int height, float[] rgb) {
        static ColorTexture load(String path) throws IOException {
            BufferedImage image = ImageIO.read(new File(path));
            float[] rgb = new float[image.getWidth() * image.getHeight() * 3];
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int argb = image.getRGB(x, y), i = (y * image.getWidth() + x) * 3;
                    rgb[i] = ((argb >> 16) & 0xFF) / 255f;
                    rgb[i + 1] = ((argb >> 8) & 0xFF) / 255f;
                    rgb[i + 2] = (argb & 0xFF) / 255f;
                }
            }
            return new ColorTexture(image.getWidth(), image.getHeight(), rgb);
        }

        float[] sample(float u, float v) {
            int x = Math.max(0, Math.min(width - 1, (int) Math.floor(u * width)));
            int y = Math.max(0, Math.min(height - 1, (int) Math.floor(v * height)));
            int i = (y * width + x) * 3;
            return new float[]{rgb[i], rgb[i + 1], rgb[i + 2]};
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

    /** The mod icon and project logo: head and crystal of the normal staff (item model and texture) on a dark blue glow. */
    static final class Logo {
        private static final int SIZE = 512;
        private static final int SUPERSAMPLING = 3;
        private static final String MODEL = "shared/mc/all/resources/assets/mccs/models/item/crystal_staff_shape.json";
        private static final String TEXTURE = "core/src/main/resources/assets/mccs/textures/item/crystal_staff.png";
        private static final String ICON = "core/src/main/resources/assets/mccs/icon.png";
        private static final int ICON_SIZE = 256;
        /** Light direction in view space (from the upper left front). */
        private static final float[] LIGHT = normalize(-0.5f, 0.7f, 0.6f);

        static void render(String out) throws IOException {
            int size = SIZE * SUPERSAMPLING;
            Frame frame = new Frame(size, new float[]{0, 0, 0});
            float[] pose = identity();
            pose = mul(pose, translation(0, 0, -1.55f));
            pose = mul(pose, rotationZ((float) Math.toRadians(-42)));
            pose = mul(pose, rotationX((float) Math.toRadians(6)));
            pose = mul(pose, rotationY((float) Math.toRadians(45)));
            // the staff's head with the crystal in the middle of the picture
            pose = mul(pose, translation(-0.5f, -22.5f / 16, -0.5f));
            drawModel(frame, pose, ColorTexture.load(TEXTURE));
            drawCrystal(frame, new CrystalRender.State(StaffKind.NORMAL, 100, 100, 37, 7, false), pose);

            float[] background = background(size);
            float[] color = frame.color;
            for (int i = 0; i < size * size; i++) {
                if (frame.depth[i] != Float.MAX_VALUE) continue;
                for (int k = 0; k < 3; k++) color[i * 3 + k] += background[i * 3 + k];
            }
            bloom(color, size);

            BufferedImage image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < SIZE; y++) {
                for (int x = 0; x < SIZE; x++) {
                    float[] sum = new float[3];
                    for (int sy = 0; sy < SUPERSAMPLING; sy++) {
                        for (int sx = 0; sx < SUPERSAMPLING; sx++) {
                            int i = ((y * SUPERSAMPLING + sy) * size + x * SUPERSAMPLING + sx) * 3;
                            for (int k = 0; k < 3; k++) sum[k] += Math.min(1f, color[i + k]);
                        }
                    }
                    int n = SUPERSAMPLING * SUPERSAMPLING;
                    image.setRGB(x, y, (int) (sum[0] / n * 255) << 16 | (int) (sum[1] / n * 255) << 8 | (int) (sum[2] / n * 255));
                }
            }
            if (out != null) {
                ImageIO.write(image, "png", new File(out));
                System.out.println("wrote " + out);
            }
            BufferedImage icon = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_RGB);
            var graphics = icon.createGraphics();
            graphics.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.drawImage(image.getScaledInstance(ICON_SIZE, ICON_SIZE, java.awt.Image.SCALE_AREA_AVERAGING), 0, 0, null);
            graphics.dispose();
            ImageIO.write(icon, "png", new File(ICON));
            System.out.println("wrote " + ICON);
        }

        /** The staff's item model: Minecraft elements (boxes, optionally turned around one axis) with their face UVs. */
        @SuppressWarnings("unchecked")
        private static void drawModel(Frame frame, float[] pose, ColorTexture texture) throws IOException {
            var model = (java.util.Map<String, Object>) Json.parse(java.nio.file.Files.readString(java.nio.file.Path.of(MODEL)));
            for (Object e : (List<Object>) model.get("elements")) {
                var element = (java.util.Map<String, Object>) e;
                float[] from = floats(element.get("from")), to = floats(element.get("to"));
                boolean shade = !Boolean.FALSE.equals(element.get("shade"));
                var rotation = (java.util.Map<String, Object>) element.get("rotation");
                var faces = (java.util.Map<String, Object>) element.get("faces");
                for (var face : faces.entrySet()) {
                    float[] uv = floats(((java.util.Map<String, Object>) face.getValue()).get("uv"));
                    float[][] corners = corners(face.getKey(), from, to);
                    float[][] points = new float[4][];
                    for (int k = 0; k < 4; k++) {
                        float[] p = rotation == null ? corners[k] : rotate(corners[k], rotation);
                        points[k] = apply(pose, p[0] / 16, p[1] / 16, p[2] / 16);
                    }
                    float light = shade ? shading(points) : 1.25f;
                    float[][] uvs = {{uv[0], uv[1]}, {uv[2], uv[1]}, {uv[2], uv[3]}, {uv[0], uv[3]}};
                    Vertex[] vertices = new Vertex[4];
                    for (int k = 0; k < 4; k++) {
                        vertices[k] = new Vertex(points[k][0], points[k][1], points[k][2], light, light, light, uvs[k][0] / 16, uvs[k][1] / 16);
                    }
                    frame.draw(new Quad(vertices, null, texture), false);
                }
            }
        }

        /** A face's corners, top left first and clockwise, as seen from outside (Minecraft's UV orientation). */
        private static float[][] corners(String face, float[] a, float[] b) {
            return switch (face) {
                case "north" -> new float[][]{{b[0], b[1], a[2]}, {a[0], b[1], a[2]}, {a[0], a[1], a[2]}, {b[0], a[1], a[2]}};
                case "south" -> new float[][]{{a[0], b[1], b[2]}, {b[0], b[1], b[2]}, {b[0], a[1], b[2]}, {a[0], a[1], b[2]}};
                case "east" -> new float[][]{{b[0], b[1], b[2]}, {b[0], b[1], a[2]}, {b[0], a[1], a[2]}, {b[0], a[1], b[2]}};
                case "west" -> new float[][]{{a[0], b[1], a[2]}, {a[0], b[1], b[2]}, {a[0], a[1], b[2]}, {a[0], a[1], a[2]}};
                case "up" -> new float[][]{{a[0], b[1], a[2]}, {b[0], b[1], a[2]}, {b[0], b[1], b[2]}, {a[0], b[1], b[2]}};
                default -> new float[][]{{a[0], a[1], b[2]}, {b[0], a[1], b[2]}, {b[0], a[1], a[2]}, {a[0], a[1], a[2]}};
            };
        }

        private static float[] rotate(float[] p, java.util.Map<String, Object> rotation) {
            float[] o = floats(rotation.get("origin"));
            double angle = Math.toRadians(((Number) rotation.get("angle")).doubleValue());
            float c = (float) Math.cos(angle), s = (float) Math.sin(angle);
            float x = p[0] - o[0], y = p[1] - o[1], z = p[2] - o[2];
            float[] r = switch ((String) rotation.get("axis")) {
                case "x" -> new float[]{x, y * c - z * s, y * s + z * c};
                case "y" -> new float[]{x * c + z * s, y, -x * s + z * c};
                default -> new float[]{x * c - y * s, x * s + y * c, z};
            };
            return new float[]{r[0] + o[0], r[1] + o[1], r[2] + o[2]};
        }

        /** Diffuse light with a little ambient and a cool rim on faces turned away from the camera. */
        private static float shading(float[][] p) {
            float[] u = {p[1][0] - p[0][0], p[1][1] - p[0][1], p[1][2] - p[0][2]};
            float[] v = {p[3][0] - p[0][0], p[3][1] - p[0][1], p[3][2] - p[0][2]};
            float[] n = normalize(u[1] * v[2] - u[2] * v[1], u[2] * v[0] - u[0] * v[2], u[0] * v[1] - u[1] * v[0]);
            float diffuse = Math.max(0, -(n[0] * LIGHT[0] + n[1] * LIGHT[1] + n[2] * LIGHT[2]));
            return 0.4f + 0.6f * diffuse;
        }

        /** A radial dark blue gradient with a soft ring of light behind the crystal. */
        private static float[] background(int size) {
            float[] rgb = new float[size * size * 3];
            float[] inner = {38 / 255f, 92 / 255f, 150 / 255f}, outer = {6 / 255f, 12 / 255f, 28 / 255f}, ring = {70 / 255f, 150 / 255f, 210 / 255f};
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    float r = (float) Math.hypot(x - size * 0.5, y - size * 0.46) / (size * 0.75f);
                    float t = Math.min(1, r);
                    float a = (float) Math.pow(1 - t, 1.6);
                    float glowRing = (float) Math.exp(-Math.pow((r * 0.75 - 0.36) / 0.035, 2)) * 0.55f;
                    for (int k = 0; k < 3; k++) rgb[(y * size + x) * 3 + k] = inner[k] * a + outer[k] * (1 - a) + ring[k] * glowRing;
                }
            }
            return rgb;
        }

        /** Spreads what is brighter than white-ish into a soft glow around it. */
        private static void bloom(float[] color, int size) {
            float[] bright = new float[color.length];
            for (int i = 0; i < color.length; i++) bright[i] = Math.max(0, color[i] - 0.85f);
            int radius = size / 40;
            for (int pass = 0; pass < 3; pass++) {
                boxBlur(bright, size, radius, true);
                boxBlur(bright, size, radius, false);
            }
            for (int i = 0; i < color.length; i++) color[i] += bright[i] * 1.1f;
        }

        private static void boxBlur(float[] data, int size, int radius, boolean horizontal) {
            float[] line = new float[size * 3];
            for (int a = 0; a < size; a++) {
                for (int b = 0; b < size; b++) {
                    int i = horizontal ? a * size + b : b * size + a;
                    System.arraycopy(data, i * 3, line, b * 3, 3);
                }
                for (int k = 0; k < 3; k++) {
                    float sum = 0;
                    for (int b = -radius; b <= radius; b++) sum += line[Math.max(0, Math.min(size - 1, b)) * 3 + k];
                    for (int b = 0; b < size; b++) {
                        int i = horizontal ? a * size + b : b * size + a;
                        data[i * 3 + k] = sum / (2 * radius + 1);
                        sum += line[Math.min(size - 1, b + radius + 1) * 3 + k] - line[Math.max(0, b - radius) * 3 + k];
                    }
                }
            }
        }

        private static float[] floats(Object list) {
            List<?> values = (List<?>) list;
            float[] result = new float[values.size()];
            for (int i = 0; i < result.length; i++) result[i] = ((Number) values.get(i)).floatValue();
            return result;
        }

        private static float[] normalize(float x, float y, float z) {
            float length = (float) Math.sqrt(x * x + y * y + z * z);
            return new float[]{x / length, y / length, z / length};
        }
    }

    /** Just enough JSON for Minecraft models: objects, arrays, strings, numbers and booleans. */
    static final class Json {
        private final String text;
        private int pos;

        private Json(String text) {
            this.text = text;
        }

        static Object parse(String text) {
            return new Json(text).value();
        }

        private Object value() {
            skipSpace();
            char c = text.charAt(pos);
            if (c == '{') {
                var map = new java.util.LinkedHashMap<String, Object>();
                pos++;
                while (true) {
                    skipSpace();
                    if (text.charAt(pos) == '}') {
                        pos++;
                        return map;
                    }
                    String key = (String) value();
                    skipSpace();
                    pos++; // ':'
                    map.put(key, value());
                    skipSpace();
                    if (text.charAt(pos) == ',') pos++;
                }
            }
            if (c == '[') {
                var list = new ArrayList<Object>();
                pos++;
                while (true) {
                    skipSpace();
                    if (text.charAt(pos) == ']') {
                        pos++;
                        return list;
                    }
                    list.add(value());
                    skipSpace();
                    if (text.charAt(pos) == ',') pos++;
                }
            }
            if (c == '"') {
                int end = text.indexOf('"', pos + 1);
                String s = text.substring(pos + 1, end);
                pos = end + 1;
                return s;
            }
            if (text.startsWith("true", pos)) {
                pos += 4;
                return true;
            }
            if (text.startsWith("false", pos)) {
                pos += 5;
                return false;
            }
            Matcher m = NUMBER.matcher(text).region(pos, text.length());
            if (!m.lookingAt()) throw new IllegalArgumentException("unexpected JSON at " + pos);
            pos = m.end();
            return Double.parseDouble(m.group());
        }

        private static final Pattern NUMBER = Pattern.compile("-?\\d+(\\.\\d+)?([eE][-+]?\\d+)?");

        private void skipSpace() {
            while (Character.isWhitespace(text.charAt(pos))) pos++;
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

    static float[] rotationZ(float a) {
        float c = (float) Math.cos(a), s = (float) Math.sin(a);
        return new float[]{c, s, 0, 0, -s, c, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1};
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
