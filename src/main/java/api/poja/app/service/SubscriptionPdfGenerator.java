package api.poja.app.service;

import static java.io.File.createTempFile;

import java.io.File;
import java.util.List;
import lombok.SneakyThrows;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionPdfGenerator {

  private static final float MARGIN = 50;
  private static final float PAGE_WIDTH = 595; // A4 width in points

  @SneakyThrows
  public File generate(String recipientLabel, String message) {
    var file = createTempFile("subscription-", ".pdf");

    try (PDDocument document = new PDDocument()) {
      PDPage page = new PDPage();
      document.addPage(page);

      var titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
      var bodyFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

      try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
        float y = 750;

        y = writeLine(cs, titleFont, 20, MARGIN, y, "Confirmation d'inscription");
        y -= 20;
        y = writeLine(cs, bodyFont, 14, MARGIN, y, "Destinataire : " + recipientLabel);
        y -= 20;

        for (String line : wrap(message, bodyFont, 12, PAGE_WIDTH - 2 * MARGIN)) {
          y = writeLine(cs, bodyFont, 12, MARGIN, y, line);
        }
      }

      document.save(file);
    }
    return file;
  }

  @SneakyThrows
  private float writeLine(
      PDPageContentStream cs, PDFont font, float size, float x, float y, String text) {
    cs.beginText();
    cs.setFont(font, size);
    cs.newLineAtOffset(x, y);
    cs.showText(text);
    cs.endText();
    return y - (size + 4);
  }

  @SneakyThrows
  private List<String> wrap(String text, PDFont font, float size, float maxWidth) {
    var lines = new java.util.ArrayList<String>();
    var words = text.split(" ");
    var current = new StringBuilder();

    for (String word : words) {
      var candidate = current.isEmpty() ? word : current + " " + word;
      float width = font.getStringWidth(candidate) / 1000 * size;
      if (width > maxWidth && !current.isEmpty()) {
        lines.add(current.toString());
        current = new StringBuilder(word);
      } else {
        current = new StringBuilder(candidate);
      }
    }
    if (!current.isEmpty()) lines.add(current.toString());
    return lines;
  }
}
