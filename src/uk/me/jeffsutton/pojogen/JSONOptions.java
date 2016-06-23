package uk.me.jeffsutton.pojogen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import org.apache.commons.io.IOUtils;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.TextEditorPane;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.jsonschema2pojo.Annotator;
import org.jsonschema2pojo.GsonAnnotator;
import org.jsonschema2pojo.Jackson2Annotator;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Timer;

public class JSONOptions extends JDialog {
    private final Project project;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField jsonSourceUrlTextField;
    private JTextField textField2;
    private JTextField textField3;
    private JButton browseButton;
    private JButton browseButton1;
    private JRadioButton GSONRadioButton;
    private JRadioButton jackson2RadioButton;
    private JTextField textField4;
    private JCheckBox usePrimitiveDataTypesCheckBox;
    private JCheckBox isParcellableCheckBox;
    private JCheckBox generateBuildersCheckBox;
    private JCheckBox generateToStringCheckBox;
    private JCheckBox useCommonsLang3CheckBox;
    private JCheckBox useJodaDatesTimesCheckBox;
    private JCheckBox generateDynamicAccessorsCheckBox;
    private TextEditorPane textEditor;
    private RTextScrollPane RTextScrollPane1;
    private JButton formatButton;
    private final Gson printingGson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeNulls().create();

    public JSONOptions(final Project project) {
        this.project = project;

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        RTextScrollPane1.setViewportView(textEditor);
        RTextScrollPane1.setLineNumbersEnabled(true);
        RTextScrollPane1.setFoldIndicatorEnabled(true);

        Gutter gutter = RTextScrollPane1.getGutter();
        gutter.setBackground(new Color(47, 47, 47));


        SyntaxScheme scheme = textEditor.getSyntaxScheme();
        scheme.getStyle(Token.RESERVED_WORD).background = Color.yellow;
        scheme.getStyle(Token.RESERVED_WORD_2).background = Color.yellow;
        scheme.getStyle(Token.DATA_TYPE).foreground = Color.blue;
        scheme.getStyle(Token.LITERAL_NUMBER_HEXADECIMAL).foreground = Color.decode("#FFC66D");
        scheme.getStyle(Token.IDENTIFIER).foreground = Color.decode("#A9B7C6");
        scheme.getStyle(Token.FUNCTION).foreground = Color.yellow;
        scheme.getStyle(Token.MARKUP_TAG_NAME).foreground = Color.yellow;
        scheme.getStyle(Token.SEPARATOR).foreground = Color.decode("#A9B7C6");
        scheme.getStyle(Token.LITERAL_BOOLEAN).foreground = Color.decode("#CB772F");
        scheme.getStyle(Token.VARIABLE).foreground = Color.decode("#9876AA");
        scheme.getStyle(Token.LITERAL_NUMBER_DECIMAL_INT).foreground = Color.decode("#6897BB");
        scheme.getStyle(Token.LITERAL_NUMBER_FLOAT).foreground = Color.decode("#6897BB");
        scheme.getStyle(Token.LITERAL_STRING_DOUBLE_QUOTE).underline = false;
        scheme.getStyle(Token.LITERAL_STRING_DOUBLE_QUOTE).foreground = Color.decode("#A5C25C");

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        browseButton.setText("选择");
        jsonSourceUrlTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                warn();
            }

            public void removeUpdate(DocumentEvent e) {
                warn();
            }

            public void insertUpdate(DocumentEvent e) {
                warn();
            }

            public void warn() {
                String text = jsonSourceUrlTextField.getText();
                if (text == null || text.trim().equals("")) {
                    browseButton.setText("选择");
                } else {
                    browseButton.setText("获取");
                }
            }
        });

        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String jsonFromUrl = null;
                    String urlString = jsonSourceUrlTextField.getText();
                    if (urlString == null || urlString.trim().equals("")) {
                        FileChooserDescriptor Descriptor = new FileChooserDescriptor(true, false, false, false, false, false);
                        Descriptor.setShowFileSystemRoots(true);
                        Descriptor.withFileFilter(new Condition<VirtualFile>() {
                            @Override
                            public boolean value(VirtualFile virtualFile) {
                                return virtualFile != null && virtualFile.getExtension() != null && (virtualFile.getExtension().equalsIgnoreCase("json") || virtualFile.getExtension().equalsIgnoreCase("txt"));
                            }
                        });
                        VirtualFile virtualFile = FileChooser.chooseFile(Descriptor, project, null);
                        if (virtualFile != null) {
                            jsonSourceUrlTextField.setText(virtualFile.getCanonicalPath());
                            jsonFromUrl = getJsonFromFile(virtualFile);
                        }
                    } else {
                        File file1 = new File(urlString);
                        URL url;
                        if (file1.exists()) {
                            url = file1.toURI().toURL();
                        } else {
                            url = new URL(urlString);
                        }
                        jsonFromUrl = IOUtils.toString(url);
                    }
                    if (jsonFromUrl != null && !"".equals(jsonFromUrl)) {
                        JsonParser parser = new JsonParser();
                        String formatJsonText = printingGson.toJson(parser.parse(jsonFromUrl));
                        textEditor.setText(formatJsonText);
                    }
                } catch (MalformedURLException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        browseButton1.setText("选择");
        browseButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showFileChoicer();
            }
        });

        formatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    JsonParser parser = new JsonParser();
                    String text = textEditor.getText();
                    final String formatJsonText = printingGson.toJson(parser.parse(text));
                    if (!text.equals(formatJsonText) && formatJsonText != null)
                        textEditor.setText(formatJsonText);
                } catch (Exception ex) {
                    return;
                }
            }
        });
    }

    private String getJsonFromFile(VirtualFile virtualFile) {
        try {
            System.out.println("Using URL: " + new URL(virtualFile.getUrl()).toExternalForm());
            BufferedReader source = new BufferedReader(
                    new InputStreamReader(virtualFile.getInputStream(), StandardCharsets.UTF_8), 4096);
            String jsonString = IOUtils.toString(source);
            return jsonString;
        } catch (Exception err) {
            err.printStackTrace();
        }
        return null;
    }

    private void onOK() {
        if (textEditor.getText() == null || textEditor.getText().equalsIgnoreCase("")) {
            JOptionPane.showMessageDialog(textEditor, "You must enter or select json data", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            JsonParser parser = new JsonParser();
            parser.parse(textEditor.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(textEditor, "Json data format error", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (textField4.getText() == null || textField4.getText().equalsIgnoreCase("")) {
            JOptionPane.showMessageDialog(textField4, "You must provide a name for the root class", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Annotator annotator;
        if (GSONRadioButton.isSelected())
            annotator = new GsonAnnotator();
        else
            annotator = new Jackson2Annotator();

        SourceGenerationConfig config = new SourceGenerationConfig();
        config.usePrimitives = usePrimitiveDataTypesCheckBox.isSelected();
        config.useParcel = isParcellableCheckBox.isSelected();
        config.useBuilder = generateBuildersCheckBox.isSelected();
        config.useCommons = useCommonsLang3CheckBox.isSelected();
        config.useJoda = useJodaDatesTimesCheckBox.isSelected();
        config.useToString = generateToStringCheckBox.isSelected();
        config.useDynamic = generateDynamicAccessorsCheckBox.isSelected();

        JPG.generate(textEditor.getText(), new File(textField2.getText()), textField3.getText(), textField4.getText(), annotator, config);

        dispose();
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }

    private void showFileChoicer() {
        FileChooserDescriptor Descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
        Descriptor.setShowFileSystemRoots(true);
        VirtualFile VirtualFile = FileChooser.chooseFile(Descriptor, project, null);
        if (VirtualFile != null) {
            com.intellij.openapi.vfs.VirtualFile fOUT = VirtualFile;
            PsiDirectory Directory = PsiDirectoryFactory.getInstance(project).createDirectory(VirtualFile);

            String path = VirtualFile.getCanonicalPath();
            String pkg = "";
            String src = "";
            if (path.contains("src") && path.contains("java")) {
                pkg = path.split("java/")[1].replaceAll("/", ".");
                src = path.split("java/")[0] + "java";
            } else if (path.contains("Java")) {
                pkg = path.split("Java/")[1].replaceAll("/", ".");
                src = path.split("Java/")[0] + "Java";
            } else if (path.contains("src")) {
                try {
                    pkg = path.split("src/")[1].replaceAll("/", ".");
                } catch (Exception e) {
                }
                src = path.split("src/")[0] + "src";
            }

            textField2.setText(src);
            textField3.setText(pkg);
        }
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here

    }
}
