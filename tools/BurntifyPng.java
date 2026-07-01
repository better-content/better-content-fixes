import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public final class BurntifyPng {
    private BurntifyPng() {
    }

    public static void main(final String[] args) throws Exception {
        if (args.length != 2 && args.length != 3) {
            throw new IllegalArgumentException("Usage: BurntifyPng <input> <output> [crestOverlay]");
        }

        final BufferedImage source = ImageIO.read(new File(args[0]));
        if (source == null) {
            throw new IllegalArgumentException("Could not decode image " + args[0]);
        }
        final BufferedImage crestOverlay = args.length == 3 ? ImageIO.read(new File(args[2])) : null;

        final BufferedImage output = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                final int argb = source.getRGB(x, y);
                final int alpha = (argb >>> 24) & 0xFF;
                if (alpha == 0) {
                    output.setRGB(x, y, argb);
                    continue;
                }

                final int red = (argb >>> 16) & 0xFF;
                final int green = (argb >>> 8) & 0xFF;
                final int blue = argb & 0xFF;
                final int luminance = (red * 30 + green * 59 + blue * 11) / 100;
                final int charValue = clamp((int) (luminance * 0.48));
                final int burnedRed = clamp((int) (charValue * 0.82 + red * 0.18) + 18);
                final int burnedGreen = clamp((int) (charValue * 0.72 + green * 0.14) + 10);
                final int burnedBlue = clamp((int) (charValue * 0.68 + blue * 0.10) + 8);

                output.setRGB(x, y, (alpha << 24) | (burnedRed << 16) | (burnedGreen << 8) | burnedBlue);
            }
        }
        if (crestOverlay != null) {
            applyCrestOverlay(output, crestOverlay);
        }

        final File outputFile = new File(args[1]);
        final File parent = outputFile.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        ImageIO.write(output, "png", outputFile);
    }

    private static int clamp(final int value) {
        return Math.max(0, Math.min(255, value));
    }

    private static void applyCrestOverlay(final BufferedImage output, final BufferedImage crestOverlay) {
        final int crestHeight = Math.min(4, Math.min(output.getHeight(), crestOverlay.getHeight()));
        final double[] rowWeights = {0.92D, 0.68D, 0.42D, 0.18D};
        for (int y = 0; y < crestHeight; y++) {
            final double blendWeight = rowWeights[y];
            for (int x = 0; x < output.getWidth(); x++) {
                final int base = output.getRGB(x, y);
                final int overlay = crestOverlay.getRGB(x % crestOverlay.getWidth(), y);
                final int baseAlpha = (base >>> 24) & 0xFF;
                if (baseAlpha == 0) {
                    continue;
                }
                final int red = blend((base >>> 16) & 0xFF, (overlay >>> 16) & 0xFF, blendWeight);
                final int green = blend((base >>> 8) & 0xFF, (overlay >>> 8) & 0xFF, blendWeight);
                final int blue = blend(base & 0xFF, overlay & 0xFF, blendWeight);
                output.setRGB(x, y, (baseAlpha << 24) | (red << 16) | (green << 8) | blue);
            }
        }
    }

    private static int blend(final int base, final int overlay, final double weight) {
        return clamp((int) Math.round(base * (1.0D - weight) + overlay * weight));
    }
}
