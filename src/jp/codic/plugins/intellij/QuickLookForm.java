package jp.codic.plugins.intellij;

import com.intellij.openapi.actionSystem.KeyboardShortcut;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.keymap.KeymapManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.util.ui.UIUtil;
import jp.codic.plugins.intellij.api.*;
import jp.codic.plugins.intellij.util.Debouncer;
import jp.codic.plugins.intellij.util.DefaultKeyListener;
import jp.codic.plugins.intellij.util.DefaultMouseListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;


public class QuickLookForm {

    private static final long DELAY_FOR_KEY_EVENT = 200;
    private static final String QUICK_LOOK_ACTION_ID = "CodicPluginQuickLookAction";
    private final Logger LOG = Logger.getInstance("#" + getClass().getCanonicalName());

    private JPanel rootPanel;
    private JList candidatesList;
    private DefaultListModel candidatesListModel;
    private JComboBox letterCaseComboBox;
    private JTextField queryTextField;
    private JLabel statusLabel;
    private Debouncer debouncer ;
    private EventListener listener;
    private CodicPluginProjectComponent component;
    private String activeFileType;
    private KeyStroke actionShortcutKey;

    /**
     * A constructor.
     */
    public QuickLookForm(Project project) {

        debouncer = new Debouncer(DELAY_FOR_KEY_EVENT);
        component = project.getComponent(CodicPluginProjectComponent.class);
        actionShortcutKey = getShortcut();

        // Init query text field.
        queryTextField.getDocument().addDocumentListener(new ModifyListener() {
            @Override public void changed(DocumentEvent documentEvent) {
                updateSearch();
            }
        });

        queryTextField.addKeyListener(new DefaultKeyListener() {
            @Override public void keyPressed(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() ==  38 ||
                        keyEvent.getKeyCode() ==  40) {
                    scrollCandidatesList(keyEvent.getKeyCode() == 38);
                    keyEvent.consume(); // Prevent default behavior.
                }
                if (keyEvent.getKeyCode() ==  10) {  // Enter
                    applySelection();
                }
            }
            @Override public void keyReleased(KeyEvent keyEvent) {
                // Note: keyReleasedでないとハンドリングできない、ただしOracleのJDKはkeyPressedでもハンドリングできる
                // Note: KeyEvent#code, KeyEvent#modifiers は keyStroke と互換性がないので変換している
                KeyStroke keyStroke = KeyStroke.getKeyStrokeForEvent(keyEvent);
                if (actionShortcutKey != null && (actionShortcutKey.getKeyCode() == keyStroke.getKeyCode() &&
                        actionShortcutKey.getModifiers() == keyStroke.getModifiers())) {
                    keyEvent.consume();
                    scrollLetterCaseComboBox();
                }
            }
        });


        // Init letter case combo box.
        for (LetterCase letterCase : LetterCase.ENTRIES) {
            letterCaseComboBox.addItem(letterCase);
        }

        letterCaseComboBox.setRenderer(new ListCellRendererWrapper() {
            @Override public void customize(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof LetterCase)
                    setText(((LetterCase) value).shortName);
            }
        });
        letterCaseComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent event) {
                if (event.getStateChange() == ItemEvent.SELECTED) {
                    //Object item = event.getItem();
                    updateSearch();
                    component.getState().addLetterCaseConvention(activeFileType,
                            ((LetterCase)letterCaseComboBox.getSelectedItem()).id);
                }
            }
        });

        // Init result list.
        candidatesListModel = new DefaultListModel();
        candidatesList.setModel(candidatesListModel);
        candidatesList.setBorder(new EmptyBorder(2, 4, 4, 2));
        candidatesList.setFont(increaseFontSize(candidatesList.getFont(), +1));
        candidatesList.addMouseListener(new DefaultMouseListener() {
            @Override public void mouseClicked(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() >= 2) {
                    applySelection();
                }
            }
        });
        candidatesList.addKeyListener(new DefaultKeyListener() {
            @Override public void keyPressed(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() ==  10) {  // Enter
                    applySelection();
                }
            }
        });

        queryTextField.putClientProperty("JTextField.variant", "search");

        // Broken char ...
//        statusLabel.setText("Press " + getKeyStrokeLabel(
//                getShortcut(QUICK_LOOK_ACTION_ID)) + " : Change letter case.");
    }

    private KeyStroke getShortcut() {
        final Shortcut[] shortcuts = KeymapManager.getInstance().getActiveKeymap().getShortcuts(QUICK_LOOK_ACTION_ID);
        for (final Shortcut shortcut : shortcuts) {
            if (shortcut instanceof KeyboardShortcut) {
                return ((KeyboardShortcut)shortcut).getFirstKeyStroke();
            }
        }
        return null;
    }

    private String getKeyStrokeLabel(KeyStroke myKeyStroke) {
        StringBuilder sb = new StringBuilder();
        if ((myKeyStroke.getModifiers() & KeyEvent.CTRL_MASK) > 0) {
            sb.append("Ctrl + ");
        }
        if ((myKeyStroke.getModifiers() & KeyEvent.SHIFT_MASK) > 0) {
            sb.append("Shift + ");
        }
        if ((myKeyStroke.getModifiers() & KeyEvent.META_MASK) > 0) {
            sb.append("Meta + ");
        }
        sb.append(myKeyStroke.getKeyChar());
        return sb.toString();
    }

    public void setSelectionListener(EventListener listener) {
        this.listener = listener;
    }

    public void beforeShow(String fileType, String text) {
        this.queryTextField.setText(text);
        this.candidatesListModel.removeAllElements();
        this.activeFileType = fileType;

        String letterCase = component.getState().findLetterCaseConvention(fileType);
        if (letterCase != null) {
            this.letterCaseComboBox.setSelectedItem(LetterCase.valueOf(letterCase));
        } else {
            this.letterCaseComboBox.setSelectedIndex(0);
        }

    }

    public void applySelection() {
        String selected = this.getSelected();
        if (selected != null && listener != null) {
            listener.selected(selected);
        }
    }

    public String getSelected() {
        String selected = (String)this.candidatesList.getSelectedValue();
        if (selected == null) {
            return null;
        }
       return selected;
    }

    public JComponent getRoot() {
        return this.rootPanel;
    }

    public JComponent getPreferredControl() {
        return this.queryTextField;
    }

    // Private methods -------

    private void updateSearch() {
        CodicPluginSettings settings = component.getState();
        debouncer.push(new SearchTask(settings, queryTextField.getText(),
                ((LetterCase)letterCaseComboBox.getSelectedItem()).id));
    }

    private void updateResultList(Translation[] translations) {
        UIUtil.invokeLaterIfNeeded(
                new CandidatesListUpdater(translations));
    }

    private void setStatusLabelAsync(String message) {
        UIUtil.invokeLaterIfNeeded(
                new StatusUpdater(true, message));
    }

    private void scrollCandidatesList(boolean reverse) {
        int index = this.candidatesList.getSelectedIndex();
        int size = this.candidatesList.getModel().getSize();
        if (size > 0) {
            index += (reverse ? -1 : 1);
            if (index < 0) {
                index = size - 1;
            } else if (index > size - 1) {
                index = 0;
            }
            System.out.println(">" + index);
            this.candidatesList.setSelectedIndex(index);
        }
    }

    private Font increaseFontSize(Font baseFont, int delta) {
        return new Font(baseFont.getName(), baseFont.getStyle(), baseFont.getSize() + delta);
    }

    private class SearchTask implements Runnable {
        private CodicPluginSettings settings;
        private String text;
        private String letterCase;

        private SearchTask(CodicPluginSettings settings, String text, String letterCase) {
            this.settings = settings;
            this.text = text;
            this.letterCase = letterCase;
        }

        @Override
        public void run() {
            if (this.text == null || this.text.equals("")) {
                updateResultList(new Translation[0]);
                return;
            }

            try {
                Translation[] translations = CodicAPI.translate(this.settings.getAccessToken(),
                        this.settings.getProjectId(), this.text, this.letterCase);

                updateResultList(translations);

            } catch (APIException e) {
                updateResultList(new Translation[0]);
                listener.failed(e);
            }
        }
    }

    private void scrollLetterCaseComboBox() {
        int index = letterCaseComboBox.getSelectedIndex();
        int size = letterCaseComboBox.getModel().getSize();
        if (size > 0) {
            index++;
            if (index > size - 1) {
                index = 0;
            }
            letterCaseComboBox.setSelectedIndex(index); // Fire event selection change.
        }
    }


    private static class LetterCase {
        private String id;
        private String name;
        private String shortName;

        public static final LetterCase[] ENTRIES = {
            new LetterCase("pascal", "PascalCase", "Aa"),
            new LetterCase("camel", "camelCase", "aA"),
            new LetterCase("lower underscore", "snake_case (小文字)", "a_a"),
            new LetterCase("upper underscore", "SNAKE_CASE (大文字)", "A_A"),
            new LetterCase("hyphen", "ハイフネーション", "a-a"),
            new LetterCase("", "変換なし", "a a"),
        };

        public static LetterCase valueOf(String id) {
            for (int i= 0; i < ENTRIES.length; i++) {
                if (id.equals(ENTRIES[i].id)) {
                    return ENTRIES[i];
                }
            }
            return null;
        }

        private LetterCase(String id, String name, String shortName) {
            this.id = id;
            this.name = name;
            this.shortName = shortName;
        }
    }


    abstract private class ModifyListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent documentEvent) {
            changed(documentEvent);
        }
        @Override
        public void removeUpdate(DocumentEvent documentEvent) {
            changed(documentEvent);
        }
        @Override
        public void changedUpdate(DocumentEvent documentEvent) {
            changed(documentEvent);
        }
        abstract public void changed(DocumentEvent documentEvent);
    }


    private class StatusUpdater implements Runnable {
        private boolean status;
        private String message;

        private StatusUpdater(boolean status, String message) {
            this.status = status;
            this.message = message;
        }

        @Override
        public void run() {
            statusLabel.setText(message);
        }
    }

    private class CandidatesListUpdater implements Runnable {
        private Translation[] translations;

        private CandidatesListUpdater(Translation[] translations) {
            this.translations = translations;
        }

        @Override
        public void run() {
            candidatesListModel.removeAllElements();
            if (translations.length > 0) {
                if (translations[0].words.length == 1 && translations[0].words[0].successful) {
                    for (Candidate candidate : translations[0].words[0].candidates) {
                        candidatesListModel.addElement(candidate.textInCasing);
                    }
                } else {
                    candidatesListModel.addElement(translations[0].translatedTextInCasing);
                }
            }
        }
    }

    public static interface EventListener {
        public void selected(String text);
        public void failed(APIException e);
    }
}
