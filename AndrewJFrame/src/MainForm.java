import com.sun.tools.javac.Main;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class MainForm {
    private JTextArea textArea1;
    private JButton button1;
    private JComboBox comboBox1;
    private JPanel panalMain;

    public MainForm() {

        JFrame _frame = new JFrame("App");
        _frame.setContentPane(this.panalMain);
        _frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        _frame.setVisible(true);

        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
    }
}
