package jp.codic.plugins.intellij;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.components.ComponentManager;
import com.intellij.openapi.components.ComponentManagerConfig;
import com.intellij.openapi.components.impl.ComponentManagerImpl;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;


public class QuickLookForm {

    private static final long DELAY_FOR_KEY_EVENT = 200;

    private JPanel rootPanel;
    private JList candidatesList;
    private DefaultListModel candidatesListModel;
    private JComboBox letterCaseComboBox;
    private JTextField queryTextField;
    private JLabel statusTextField;
    private Debouncer debouncer ;
    //private Editor editor;
    private SelectionListener listener;
    private CodicPluginProjectComponent component;
    private String activeFileType;

    /**
     * A constructor.
     */
    public QuickLookForm(Project project, SelectionListener listener_) {

        //editor = project_;
        listener = listener_;
        debouncer = new Debouncer(DELAY_FOR_KEY_EVENT);
        component = project.getComponent(CodicPluginProjectComponent.class);

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
                    requestFocusAnItem(keyEvent.getKeyCode() == 38);
                }
                if (keyEvent.getKeyCode() ==  10) {  // Enter
                    applySelection();
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
        letterCaseComboBox.addActionListener (new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateSearch();
                component.getState().addLetterCaseConvention(activeFileType,
                        ((LetterCase)letterCaseComboBox.getSelectedItem()).id);
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


        // Init status message.
        //statusTextField.setFont(increaseFontSize(statusTextField.getFont(), -1));
        //statusTextField.setBorder(new EmptyBorder(3, 3, 3, 3));

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


//    private String getFileTypeName() {
//        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
//        return virtualFile.getFileType().getName();
//    }


    public void applySelection() {
        String selected = this.getSelected();
        if (selected != null) {
            listener.select(selected);
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

    private void updateSearch() {
        CodicPluginSettings settings = component.getState();
        debouncer.push(new SearchTask(settings, queryTextField.getText(),
                ((LetterCase)letterCaseComboBox.getSelectedItem()).id));
    }

    private void updateResultList(Translation[] translations) {
        UIUtil.invokeLaterIfNeeded(
                new CandidatesListUpdater(translations));
    }

    private void updateStatusMessage(String message) {
        UIUtil.invokeLaterIfNeeded(
                new StatusUpdater(true, message));
    }

    private void requestFocusAnItem(boolean reverse) {
        int index = this.candidatesList.getSelectedIndex();
        if (index == -1) {
            if (this.candidatesList.getModel().getSize() > 0) {
                this.candidatesList.setSelectedIndex(reverse ?
                        this.candidatesList.getModel().getSize() - 1 : 0);
            }
        }
        this.candidatesList.requestFocus();
    }

    private Font increaseFontSize(Font baseFont, int delta) {
        return new Font(baseFont.getName(), baseFont.getStyle(), baseFont.getSize() + delta);
    }

    private class SearchTask implements Runnable {
        private CodicPluginSettings settings;
        private String text;
        private String letterCase;
        //private Project project;

        private SearchTask(CodicPluginSettings settings, String text, String letterCase) {
            this.settings = settings;
            this.text = text;
            //this.project = project;
            this.letterCase = letterCase;
        }

        @Override
        public void run() {
            if (this.text == null || this.text.equals("")) {
                updateResultList(new Translation[0]);
                return;
            }

            CodicPluginSettings settings = component.getState();

            try {
                Translation[] translations = CodicAPI.translate(this.settings.getAccessToken(),
                        this.settings.getProjectId(), this.text, this.letterCase);

                updateResultList(translations);

            } catch (APIException e) {
                updateResultList(new Translation[0]);
                //updateStatusMessage(e.getMessage());

                Notifications.Bus.notify(
                    new Notification("CodicPlugin", "Codic Plugin Error", e.getMessage(), NotificationType.WARNING)
                );

            }
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
            statusTextField.setText(message);
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
                        candidatesListModel.addElement(candidate.text);
                    }
                } else {
                    candidatesListModel.addElement(translations[0].translatedText);
                }
            }
        }
    }


    public static interface SelectionListener {
        public void select(String text);
    }

}
