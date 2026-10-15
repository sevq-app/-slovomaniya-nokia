import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.*;

/** Первая версия меню для Nokia C1-02 (Java ME, MIDP 2.0). */
public final class Slovomaniya extends MIDlet {
    private Display display;
    private MenuScreen menu;

    public void startApp() {
        display = Display.getDisplay(this);
        if (menu == null) menu = new MenuScreen();
        display.setCurrent(menu);
    }
    public void pauseApp() {}
    public void destroyApp(boolean unconditional) {}

    private final class MenuScreen extends Canvas {
        private final String[] items = {
            "Угадай слово", "Кроссворды", "Составь слово",
            "Рекорды", "Настройки"
        };
        private int selected;

        protected void paint(Graphics g) {
            int w = getWidth(), h = getHeight();
            g.setColor(0xFFF8E6);
            g.fillRect(0, 0, w, h);
            g.setColor(0x184D80);
            g.fillRect(0, 0, w, 22);
            g.setColor(0xFFFFFF);
            g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL));
            g.drawString("СЛОВОМАНИЯ", 3, 3, Graphics.TOP | Graphics.LEFT);
            g.setColor(0x30291D);
            g.drawString("Монеты: 500 000", 4, 26, Graphics.TOP | Graphics.LEFT);
            g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL));
            int y = 46;
            for (int i = 0; i < items.length; i++) {
                if (i == selected) {
                    g.setColor(0xFFD16A);
                    g.fillRoundRect(2, y - 1, w - 4, 19, 4, 4);
                }
                g.setColor(0x142F48);
                g.drawString(items[i], 6, y, Graphics.TOP | Graphics.LEFT);
                y += 19;
            }
            g.setColor(0x184D80);
            g.fillRect(0, h - 17, w, 17);
            g.setColor(0xFFFFFF);
            g.drawString("Выбор", 3, h - 16, Graphics.TOP | Graphics.LEFT);
            g.drawString("Выход", w - 3, h - 16, Graphics.TOP | Graphics.RIGHT);
        }

        protected void keyPressed(int key) {
            int action = getGameAction(key);
            if (action == UP) selected = (selected + items.length - 1) % items.length;
            else if (action == DOWN) selected = (selected + 1) % items.length;
            else if (key == -7) {
                notifyDestroyed();
                return;
            }
            // Открытие разделов будет добавлено на следующем этапе.
            repaint();
        }
    }
}
