import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ImageLoader {

    private static final String IMAGE_DIR = "src/images/";
    private final Map<String, Image> cache = new HashMap<>();
    private final int tileSize;

    public ImageLoader(int tileSize) {
        this.tileSize = tileSize;
        preloadAll();
    }

    private void preloadAll() {
        for (PieceColor color : PieceColor.values())
            for (PieceType type : PieceType.values())
                loadImage(type.getImageName(color));
    }

    private void loadImage(String filename) {
        File file = new File(IMAGE_DIR + filename);
        if (!file.exists()) {
            System.err.println("[ImageLoader] Missing: " + file.getAbsolutePath());
            return;
        }
        try {
            BufferedImage raw = ImageIO.read(file);
            Image scaled = raw.getScaledInstance(tileSize - 10, tileSize - 10, Image.SCALE_SMOOTH);
            cache.put(filename, scaled);
        } catch (IOException e) {
            System.err.println("[ImageLoader] Failed: " + filename);
        }
    }

    public Image getImage(Piece piece) {
        if (piece == null) return null;
        return cache.get(piece.getType().getImageName(piece.getColor()));
    }
}