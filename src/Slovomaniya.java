import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.*;
import javax.microedition.rms.*;

/**
 * Словомания: первый игровой прототип для Java ME (MIDP 2.0).
 * Ввод слова выполняется штатным редактором телефона.
 */
public final class Slovomaniya extends MIDlet implements CommandListener {
    private static final String STORE = "slovomaniya1";
    private static final String[] QUESTIONS = {
        "Полосатая родственница лошади.",
        "Столица Франции.",
        "Инструмент с мехами и кнопками.",
        "Небесное светило, вокруг которого вращается Земля.",
        "Наука о происхождении слов.",
        "Столица Италии.",
        "Зверь с иголками на спине.",
        "Прибор для измерения температуры.",
        "Самый большой океан Земли.",
        "Русский поэт, автор Евгения Онегина."
    };
    private static final String[] ANSWERS = {
        "ЗЕБРА", "ПАРИЖ", "БАЯН", "СОЛНЦЕ", "ЭТИМОЛОГИЯ",
        "РИМ", "ЁЖ", "ТЕРМОМЕТР", "ТИХИЙ", "ПУШКИН"
    };
    private final Command back = new Command("Назад", Command.BACK, 1);
    private final Command check = new Command("Проверить", Command.OK, 1);
    private final Command hint = new Command("Подсказка", Command.SCREEN, 2);
    private final Command next = new Command("Дальше", Command.OK, 1);
    private Display display;
    private MenuScreen menu;
    private Form puzzle;
    private TextField input;
    private int level = 0;
    private int coins = 500000;
    private int solved = 0;
    private int hintsUsed = 0;
    private String revealed = "";

    public void startApp() {
        display = Display.getDisplay(this);
        load();
        if (menu == null) menu = new MenuScreen();
        display.setCurrent(menu);
    }
    public void pauseApp() { save(); }
    public void destroyApp(boolean unconditional) { save(); }

    private void showPuzzle() {
        if (level >= ANSWERS.length) {
            Alert done = new Alert("Поздравляем!", "Все вопросы этой версии разгаданы!", null, AlertType.INFO);
            done.setTimeout(Alert.FOREVER);
            display.setCurrent(done, menu);
            return;
        }
        puzzle = new Form("Угадай слово");
        puzzle.append(join("Уровень ", Integer.toString(level + 1), " из ", Integer.toString(ANSWERS.length), "\n"));
        puzzle.append(join("Монеты: ", Integer.toString(coins), "\n\n"));
        puzzle.append(join(QUESTIONS[level], "\n"));
        puzzle.append(join("Букв: ", Integer.toString(ANSWERS[level].length()), "\n"));
        if (revealed.length() > 0) puzzle.append(join("Открыто: ", revealed, "\n"));
        input = new TextField("Ответ", "", Integer.toString(ANSWERS[level].length()), TextField.ANY);
        puzzle.append(input);
        puzzle.addCommand(back);
        puzzle.addCommand(check);
        puzzle.addCommand(hint);
        puzzle.setCommandListener(this);
        display.setCurrent(puzzle);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == back) {
            display.setCurrent(menu);
        } else if (c == check && d == puzzle) {
            String value = input.getString().trim().toUpperCase();
            if (value.equals(ANSWERS[level])) {
                coins += 10;
                solved++;
                level++;
                revealed = "";
                save();
                Alert success = new Alert("Правильно!", join("Награда: 10 монет. Баланс: ", Integer.toString(coins)),
                                          null, AlertType.CONFIRMATION);
                success.setTimeout(Alert.FOREVER);
                success.addCommand(next);
                success.setCommandListener(this);
                display.setCurrent(success);
            } else {
                Alert wrong = new Alert("Не угадано", "Попробуй ещё раз.", null, AlertType.INFO);
                wrong.setTimeout(1800);
                display.setCurrent(wrong, puzzle);
            }
        } else if (c == hint && d == puzzle) {
            if (coins < 50) {
                Alert noCoins = new Alert("Недостаточно монет", "Подсказка стоит 50 монет.",
                                          null, AlertType.INFO);
                noCoins.setTimeout(1800);
                display.setCurrent(noCoins, puzzle);
                return;
            }
            String solution = ANSWERS[level];
            if (revealed.length() == 0) {
                StringBuffer buffer = new StringBuffer();
                for (int i = 0; i < solution.length(); i++) buffer.append('_');
                revealed = buffer.toString();
            }
            int pos = revealed.indexOf('_');
            if (pos < 0) {
                Alert all = new Alert("Подсказка", "Все буквы уже открыты.", null, AlertType.INFO);
                all.setTimeout(1800);
                display.setCurrent(all, puzzle);
                return;
            }
            char[] chars = revealed.toCharArray();
            chars[pos] = solution.charAt(pos);
            revealed = new String(chars);
            coins -= 50;
            hintsUsed++;
            save();
            String previousInput = input.getString();
            showPuzzle();
            input.setString(previousInput);
        } else if (c == next) {
            showPuzzle();
        }
    }

    private static String join(String a, String b) {
        StringBuffer sb = new StringBuffer();
        sb.append(a).append(b);
        return sb.toString();
    }
    private static String join(String a, String b, String c) {
        StringBuffer sb = new StringBuffer();
        sb.append(a).append(b).append(c);
        return sb.toString();
    }
    private static String join(String a, String b, String c, String d, String e) {
        StringBuffer sb = new StringBuffer();
        sb.append(a).append(b).append(c).append(d).append(e);
        return sb.toString();
    }
    private static String join(String a, String b, String c, String d, String e, String f, String g, String h, String i) {
        StringBuffer sb = new StringBuffer();
        sb.append(a).append(b).append(c).append(d).append(e).append(f).append(g).append(h).append(i);
        return sb.toString();
    }

    private static String join(String a, String b, String c, String d, String e, String f) {
        StringBuffer sb = new StringBuffer();
        sb.append(a).append(b).append(c).append(d).append(e).append(f);
        return sb.toString();
    }

    private void save() {
        RecordStore store = null;
        try {
            store = RecordStore.openRecordStore(STORE, true);
            String state = join(Integer.toString(level), ";", Integer.toString(coins), ";", Integer.toString(solved), ";", Integer.toString(hintsUsed), ";", revealed);
            byte[] bytes = state.getBytes("UTF-8");
            if (store.getNumRecords() == 0) store.addRecord(bytes, 0, bytes.length);
            else store.setRecord(1, bytes, 0, bytes.length);
        } catch (Exception ignored) {
            // При ошибке хранилища игра продолжает работать без сохранения.
        } finally {
            try { if (store != null) store.closeRecordStore(); } catch (Exception ignored) {}
        }
    }
    private void load() {
        RecordStore store = null;
        try {
            store = RecordStore.openRecordStore(STORE, true);
            if (store.getNumRecords() == 0) return;
            String state = new String(store.getRecord(1), "UTF-8");
            int a = state.indexOf(';');
            int b = state.indexOf(';', a + 1);
            int c = state.indexOf(';', b + 1);
            int e = state.indexOf(';', c + 1);
            int newLevel = Integer.parseInt(state.substring(0, a));
            int newCoins = Integer.parseInt(state.substring(a + 1, b));
            if (newLevel < 0 || newLevel > ANSWERS.length || newCoins < 0) return;
            level = newLevel;
            coins = newCoins;
            solved = Integer.parseInt(state.substring(b + 1, c));
            hintsUsed = Integer.parseInt(state.substring(c + 1, e));
            revealed = state.substring(e + 1);
        } catch (Exception ignored) {
        } finally {
            try { if (store != null) store.closeRecordStore(); } catch (Exception ignored) {}
        }
    }

    private final class MenuScreen extends Canvas {
        private final String[] items = {
            "Угадай слово", "Кроссворды", "Составь слово",
            "Рекорды", "Настройки"
        };
        private int selected;
        protected void paint(Graphics g) {
            int w = getWidth(), h = getHeight();
            g.setColor(0xFFF8E6); g.fillRect(0, 0, w, h);
            g.setColor(0x184D80); g.fillRect(0, 0, w, 22);
            g.setColor(0xFFFFFF);
            g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL));
            g.drawString("СЛОВОМАНИЯ", 3, 3, Graphics.TOP | Graphics.LEFT);
            g.setColor(0x30291D);
            g.drawString(join("Монеты: ", Integer.toString(coins)), 4, 26, Graphics.TOP | Graphics.LEFT);
            g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL));
            int y = 46;
            for (int i = 0; i < items.length; i++) {
                if (i == selected) {
                    g.setColor(0xFFD16A); g.fillRoundRect(2, y - 1, w - 4, 19, 4, 4);
                }
                g.setColor(0x142F48);
                g.drawString(items[i], 6, y, Graphics.TOP | Graphics.LEFT);
                y += 19;
            }
            g.setColor(0x184D80); g.fillRect(0, h - 17, w, 17);
            g.setColor(0xFFFFFF);
            g.drawString("Выбор", 3, h - 16, Graphics.TOP | Graphics.LEFT);
            g.drawString("Выход", w - 3, h - 16, Graphics.TOP | Graphics.RIGHT);
        }
        protected void keyPressed(int key) {
            int action = getGameAction(key);
            if (action == UP) selected = (selected + items.length - 1) % items.length;
            else if (action == DOWN) selected = (selected + 1) % items.length;
            else if (action == FIRE || key == -6) {
                if (selected == 0) showPuzzle();
                else if (selected == 3) {
                    Alert stats = new Alert("Рекорды", join("Разгадано: ", Integer.toString(solved), "\nПодсказок: ", Integer.toString(hintsUsed), "\nМонеты: ", Integer.toString(coins)), null, AlertType.INFO);
                    stats.setTimeout(Alert.FOREVER);
                    display.setCurrent(stats, menu);
                } else {
                    Alert soon = new Alert("Скоро", "Этот раздел появится позже.", null, AlertType.INFO);
                    soon.setTimeout(1800);
                    display.setCurrent(soon, menu);
                }
            } else if (key == -7) {
                save();
                notifyDestroyed();
                return;
            }
            repaint();
        }
    }
}
