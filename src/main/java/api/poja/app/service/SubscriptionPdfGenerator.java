package api.poja.app.service;

import static java.io.File.createTempFile;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.File;
import java.io.FileOutputStream;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionPdfGenerator {

  @SneakyThrows
  public File generate(String recipientLabel, String courseTitle, String message) {
    var file = createTempFile("subscription-", ".pdf");
    String html = buildHtml(recipientLabel, courseTitle, message);

    try (FileOutputStream os = new FileOutputStream(file)) {
      PdfRendererBuilder builder = new PdfRendererBuilder();
      builder.useFastMode();
      builder.withHtmlContent(html, null);
      builder.toStream(os);
      builder.run();
    }
    return file;
  }

  private String buildHtml(String recipientLabel, String courseTitle, String message) {
    return
"""
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8"/>
    <style>
        @page { size: A4; margin: 50px; }

        body {
            font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
            color: #334155;
            margin: 0;
            padding: 0;
        }

        .page-container {
            width: 100%%;
        }

        /* Header, replacing display:flex with a table */
        .header-table {
            width: 100%%;
            border-collapse: collapse;
            border-bottom: 2px solid #e2e8f0;
            padding-bottom: 24px;
            margin-bottom: 40px;
        }

        .header-table td {
            vertical-align: middle;
            padding-bottom: 24px;
        }

        .header-text h1 {
            font-size: 28px;
            font-weight: 700;
            color: #0f172a;
            margin: 0 0 8px 0;
        }

        .header-text p {
            font-size: 12px;
            color: #64748b;
            margin: 0;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            font-weight: 600;
        }

        .logo-icon {
            width: 48px;
            height: 48px;
            background: #0b0370;
            border-radius: 12px;
            color: #fac519;
            font-size: 16px;
            font-weight: 700;
            text-align: center;
            vertical-align: middle;
        }

        .badge {
            display: inline-block;
            background: #dbeafe;
            color: #1d4ed8;
            font-size: 12px;
            font-weight: 700;
            padding: 6px 14px;
            border-radius: 20px;
            margin-bottom: 28px;
        }

        .card {
            background: #f8fafc;
            border: 1px solid #e2e8f0;
            border-radius: 12px;
            padding: 28px;
            margin-bottom: 32px;
        }

        /* Card grid, replacing CSS Grid with a table */
        .card-grid {
            width: 100%%;
            border-collapse: collapse;
        }

        .card-grid td {
            width: 50%%;
            vertical-align: top;
            padding-right: 24px;
        }

        .card .label {
            font-size: 11px;
            text-transform: uppercase;
            letter-spacing: 0.06em;
            color: #64748b;
            font-weight: 700;
            margin-bottom: 8px;
        }

        .card .value {
            font-size: 16px;
            font-weight: 600;
            color: #0f172a;
            margin: 0;
        }

        .message {
            font-size: 15px;
            line-height: 1.7;
            color: #475569;
            margin: 0;
        }

        .footer {
            margin-top: 60px;
            padding-top: 24px;
            border-top: 1px solid #e2e8f0;
            font-size: 12px;
            color: #94a3b8;
            text-align: center;
        }
    </style>
</head>
<body>
    <div class="page-container">

        <table class="header-table">
            <tr>
                <td class="header-text">
                    <h1>Confirmation d'inscription</h1>
                    <p>Document g&#233;n&#233;r&#233; automatiquement</p>
                </td>
                <td class="logo-icon" width="48">HEI</td>
            </tr>
        </table>

        <span class="badge">&#10003; INSCRIPTION CONFIRM&#201;E</span>

        <div class="card">
            <table class="card-grid">
                <tr>
                    <td>
                        <div class="label">Destinataire</div>
                        <div class="value">%s</div>
                    </td>
                    <td>
                        <div class="label">Cours</div>
                        <div class="value">%s</div>
                    </td>
                </tr>
            </table>
        </div>

        <p class="message">%s</p>

        <div class="footer">
            Ce document confirme votre inscription. Conservez-le pour vos dossiers.
        </div>
    </div>
</body>
</html>
"""
        .formatted(escape(recipientLabel), escape(courseTitle), escape(message));
  }

  private String escape(String s) {
    return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }
}
