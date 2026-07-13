package api.poja.app.file.image;


import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Component;

@Component
public class ImageGrayscaleConverter {

  public File convert(File source, String originalFilename) throws IOException {
    BufferedImage original = ImageIO.read(source);
    if (original == null) {
      throw new IOException("Unable to read image: " + originalFilename);
    }

    BufferedImage grayscale =
        new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

    Graphics2D graphics = grayscale.createGraphics();
    try {
      graphics.drawImage(original, 0, 0, null);
    } finally {
      graphics.dispose();
    }

    String format = formatFor(originalFilename);
    File output = File.createTempFile("grayscale-", "." + format);
    ImageIO.write(grayscale, format, output);
    return output;
  }

  private String formatFor(String filename) {
    return filename != null && filename.toLowerCase().endsWith(".png") ? "png" : "jpg";
  }
}
