package es.uloyola.pdm.Alumni_android.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.widget.ImageView;

/**
 * AvatarUtil
 * ----------
 * Genera "avatares" tipo Gmail (iniciales sobre un circulo de color) a partir
 * de un nombre. Color reproducible (determinista por nombre) y diverso.
 *
 * Esto cubre el caso "foto de perfil por defecto" sin necesidad de bundlear PNGs
 * ni de descargar imagenes remotas (cumple con foto de perfil para cada alumni
 * de forma unica y diversa).
 */
public final class AvatarUtil {

    /** Paleta corporativa Loyola + complementarios para que los avatares sean variados. */
    private static final int[] PALETA = {
            0xFF00205B, // azul oscuro UL
            0xFF6AD1E3, // azul claro UL
            0xFF71CC98, // verde UL
            0xFFFFB25B, // naranja UL
            0xFFFFCD00, // amarillo UL
            0xFFF9423A, // rojo UL
            0xFF1976D2,
            0xFF8E44AD,
            0xFF2E7D32,
            0xFFD81B60
    };

    private AvatarUtil() {}

    /** Coloca un avatar generado dentro del ImageView dado. */
    public static void pintarEn(ImageView iv, String nombre, int sizeDp) {
        float dpi = iv.getResources().getDisplayMetrics().density;
        int px = Math.max(1, (int) (sizeDp * dpi));
        iv.setImageBitmap(generar(nombre, px));
    }

    /** Genera un bitmap circular con las iniciales del nombre. */
    public static Bitmap generar(String nombre, int sizePx) {
        Bitmap bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);

        int color = colorPara(nombre);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        c.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f, p);

        // Iniciales
        String iniciales = iniciales(nombre);
        Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
        text.setColor(esColorClaro(color) ? Color.parseColor("#00205B") : Color.WHITE);
        text.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        text.setTextSize(sizePx * 0.42f);
        text.setTextAlign(Paint.Align.CENTER);
        Rect b = new Rect();
        text.getTextBounds(iniciales, 0, iniciales.length(), b);
        float y = sizePx / 2f + b.height() / 2f - b.bottom;
        c.drawText(iniciales, sizePx / 2f, y, text);
        return bmp;
    }

    private static String iniciales(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) return "?";
        String[] partes = nombre.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < partes.length && sb.length() < 2; i++) {
            if (!partes[i].isEmpty()) sb.append(Character.toUpperCase(partes[i].charAt(0)));
        }
        return sb.length() == 0 ? "?" : sb.toString();
    }

    private static int colorPara(String nombre) {
        if (nombre == null) nombre = "";
        int h = nombre.toLowerCase().hashCode();
        return PALETA[Math.abs(h) % PALETA.length];
    }

    private static boolean esColorClaro(int color) {
        int r = (color >> 16) & 0xFF, g = (color >> 8) & 0xFF, b = color & 0xFF;
        double luminancia = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0;
        return luminancia > 0.6;
    }
}
