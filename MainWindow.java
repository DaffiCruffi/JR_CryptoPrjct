import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainWindow {
    private static MainWindow mainWindow;
    private JFrame mFrame;
    private JPanel jPanel;
    private JButton butCode;
    private JButton butDeCode;
    private JButton butBrut;
    private Crto crto;
    JTextField jTextField;

    private MainWindow() { // constructor
        createMainWin();
    }


    public static MainWindow getInstance() { // pattern Singleton
        if (mainWindow == null) {
            mainWindow = new MainWindow();
        }
        return mainWindow;
    }

    private void createMainWin() {
        mFrame = createMainFrame(); // генерируем окно
        jPanel = new JPanel(); // генерируем объект поля, на котором будет все размещаться
        mFrame.add(jPanel); // добавляем в окно само поле
        jPanel.add(new JLabel("Ключ шифра: ")); // теперь редачим уже само поле в окне, добавляем надпись
        jPanel.add(addJTextField());
        jPanel.add(addButton());
        jPanel.add(addButtonDecode());
        jPanel.add(addButtonBrut());
        jPanel.add(addButtonAnalysis());
        jPanel.revalidate();
    }

    private JButton addButton() {
        butCode = new JButton("Зашифровать"); // кнопка с надписью расшифровать
        butCode.addActionListener(new ActionListener() { // создание обработчика событий
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jTextField.getText().isEmpty()) { // если поле ввода после нажатия кнопки было пустым
                    showMessageBox("Введите ключ");
                }
                if (crto.normaliseKey(Integer.parseInt(jTextField.getText())) == 0) {
                    showMessageBox("Не валидный ключ, попробуйте ввести другой.");
                } else {
                    JFileChooser jDialog = new JFileChooser();
                    jDialog.setFileFilter(new FileFilter() {
                        @Override
                        public boolean accept(File f) {
                            return f.getName().endsWith("txt");
                        }

                        @Override
                        public String getDescription() {
                            return "*.txt";
                        }
                    });
                    if (jDialog.showOpenDialog(jPanel) == 0) {
                        try {
                            crto = new Crto(jDialog.getSelectedFile(), Integer.parseInt(jTextField.getText()));
                            crto.crypt(false);
                            crto.saveFile();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });
        butCode.setSize(5, 3);
        return butCode;
    }

    private JButton addButtonDecode() {
        butDeCode = new JButton("Расшифровать");
        butDeCode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jTextField.getText().isEmpty() ) {
                    showMessageBox("Введите ключ");
                } if(Crto.normaliseKey(Integer.parseInt(jTextField.getText()))==0) {
                    showMessageBox("Не валидный ключ, попробуйте ввести другой.");
                } else
                {
                    JFileChooser jDialog = new JFileChooser();
                    jDialog.setFileFilter(new FileFilter() {
                        @Override
                        public boolean accept(File f) {
                            return f.getName().endsWith("txt");
                        }

                        @Override
                        public String getDescription() {
                            return "*.txt";
                        }
                    });
                    if (jDialog.showOpenDialog(jPanel) == 0) {
                        try {
                            crto = new Crto(jDialog.getSelectedFile(),Integer.parseInt(jTextField.getText()));
                            crto.crypt(true);
                            crto.saveFile();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });
        butDeCode.setSize(5, 3);
        return butDeCode;
    }

    private JButton addButtonBrut() {
        butBrut = new JButton("Брут дешифровка");
        Boolean repeat = Boolean.TRUE;

        butBrut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jDialog = new JFileChooser();
                jDialog.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.getName().endsWith("txt");
                    }

                    @Override
                    public String getDescription() {
                        return "*.txt";
                    }
                });
                if (jDialog.showOpenDialog(jPanel) == 0) {
                    try {
                        File file = jDialog.getSelectedFile();
                        int numberIter = 0;
                        while(repeat){
                            crto = new Crto(file);
                            crto.setKey(numberIter);
                            crto.crypt(true);
                            if(crto.checkText() || crto.getSymbolsCount()==numberIter){
                                break;
                            }
                            numberIter++;
                        }
                        crto.saveFile();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        butBrut.setSize(5, 3);
        return butBrut;
    }

    private JButton addButtonAnalysis() {
        butBrut = new JButton("Анализ дешифровка");
        Boolean repeat = Boolean.TRUE;

        butBrut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jDialog = new JFileChooser();
                jDialog.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.getName().endsWith("txt");
                    }

                    @Override
                    public String getDescription() {
                        return "*.txt";
                    }
                });
                if (jDialog.showOpenDialog(jPanel) == 0) {
                    try {
                        crto = new Crto(jDialog.getSelectedFile());
                        jDialog.setDialogTitle("Выбирите файл для анализа");
                        if (jDialog.showOpenDialog(jPanel) == 0){
                            crto.setAnalysFile(jDialog.getSelectedFile());
                            crto.analysisEncrypt();
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }


        });
        butBrut.setSize(5, 3);
        return butBrut;
    }

    private JTextField addJTextField() {
        jTextField = new JTextField(7);
        ((AbstractDocument) jTextField.getDocument()).setDocumentFilter(new DocumentFilter() {
            Pattern regEx = Pattern.compile("\\d*");

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                Matcher matcher = regEx.matcher(text);
                if (!matcher.matches()) {
                    return;
                }
                super.replace(fb, offset, length, text, attrs);
            }
        });
        return jTextField;
    }

    private JFrame createMainFrame() {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(500, 100);
        frame.setResizable(false); // нельзя менять размер окна
        frame.setLocationRelativeTo(null); // установить расположение относительно
        frame.setVisible(true); // сделал окно видимым, по - умолчанию оно невидимо
        return frame; // возвращает настроенное окно


    }

    private void showMessageBox(String message){
        JFrame jMessageBox = new JFrame();
        jMessageBox.setSize(350, 100);
        jMessageBox.setLocationRelativeTo(null);
        JLabel jLabel = new JLabel(message);
        jLabel.setHorizontalAlignment(JLabel.CENTER);
        jMessageBox.add(jLabel);
        jMessageBox.setResizable(false);
        jMessageBox.setVisible(true);
    }

}
