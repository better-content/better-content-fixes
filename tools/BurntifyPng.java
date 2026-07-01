import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public final class BurntifyPng {
    private BurntifyPng() {
    }

    public static void main(final String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: BurntifyPng <input> <output>");
        }

        final BufferedImage source = ImageIO.read(new File(args[0]));
        if (source == null) {
            throw new IllegalArgumentException("Could not decode image " + args[0]);
        }

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
}
