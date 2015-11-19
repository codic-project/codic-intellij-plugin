package jp.codic.plugins.intellij;

import com.intellij.ide.IdeBundle;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.popup.PopupFactoryImpl;

public class QuickLookAction extends AnAction {

    private QuickLookForm form = null;
    private JBPopup popup;

    public void actionPerformed(AnActionEvent e) {

        Editor editor = PlatformDataKeys.EDITOR_EVEN_IF_INACTIVE.getData(e.getDataContext());
        if (editor == null) {
            return;
        }

        if (!isConfigured(editor.getProject())) {
            Notifications.Bus.notify(
                    new Notification("CodicPlugin", "Codic Plugin Error",
                            CodicPlugin.getString("messages.access_token_required"), NotificationType.WARNING)
            );
            return;
        }

        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        String fileName = virtualFile.getFileType().getName();

        String text = getSelectedText(editor);

        popup = showPopup(editor);
        form.beforeShow(fileName, text);
    }

    private boolean isConfigured(Project project) {
        CodicPluginProjectComponent component = project.getComponent(CodicPluginProjectComponent.class);
        CodicPluginSettings settings = component.getState();

        return settings.getAccessToken() != null &&
                !settings.getAccessToken().equals("");
    }

    private JBPopup showPopup(final Editor editor) {
        if (form == null) {
            form = new QuickLookForm(editor.getProject());
        }

        form.setSelectionListener(new QuickLookForm.SelectionListener() {
            @Override
            public void select(final String text) {
                CommandProcessor.getInstance().executeCommand(editor.getProject(), new Runnable() {
                    public void run() {
                        ApplicationManager.getApplication().runWriteAction(new Runnable() {
                            public void run() {
                                EditorModificationUtil.deleteSelectedText(editor);
                                EditorModificationUtil.insertStringAtCaret(editor, text);
                                popup.dispose();
                                popup = null;
                            }
                        });
                    }
                }, IdeBundle.message("command.pasting.reference"), null);
            }
        });

        PopupFactoryImpl popupFactory = new PopupFactoryImpl();
        ComponentPopupBuilder builder = popupFactory.createComponentPopupBuilder(
                form.getRoot(), form.getPreferredControl());
        builder.setResizable(true);
        builder.setTitle("Generate naming");
        builder.setRequestFocus(true);

        JBPopup popup = builder.createPopup();
        popup.showInBestPositionFor(editor);

        // BUGFIX :
//        try {
//            PointerInfo pointer = MouseInfo.getPointerInfo();
//            Robot robot = new Robot();
//            robot.mouseMove(popup.getLocationOnScreen().x, popup.getLocationOnScreen().y);
//            robot.mousePress(InputEvent.BUTTON1_MASK);
//            robot.mouseMove(pointer.getLocation().x, pointer.getLocation().y);
//
//        } catch (AWTException e) {
//        }


        return popup;
    }

    private String getSelectedText(Editor editor) {
        SelectionModel selectedModel = editor.getSelectionModel();
        return selectedModel.getSelectedText();
    }


}
