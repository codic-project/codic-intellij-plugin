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
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.popup.PopupFactoryImpl;
import jp.codic.plugins.intellij.api.APIException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;

public class QuickLookAction extends AnAction {

    private QuickLookForm form = null;
    private JBPopup popup;
    private CancelListener cancelListener;
    private CodicPluginProjectComponent component;

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

        component = editor.getProject().getComponent(CodicPluginProjectComponent.class);
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
        final Project project = editor.getProject();
        if (form == null) {
            form = new QuickLookForm(editor.getProject());
        }

        form.setSelectionListener(new QuickLookForm.EventListener() {
            @Override
            public void selected(final String text) {
                CommandProcessor.getInstance().executeCommand(editor.getProject(), new Runnable() {
                    public void run() {
                        ApplicationManager.getApplication().runWriteAction(new Runnable() {
                            public void run() {
                                EditorModificationUtil.deleteSelectedText(editor);
                                EditorModificationUtil.insertStringAtCaret(editor, text);
                                popup.cancel();
                                popup = null;
                            }
                        });
                    }
                }, IdeBundle.message("command.pasting.reference"), null);
            }

            @Override
            public void failed(APIException e) {
                String message = e.getMessage();
                if (e.getCode() == 429) {   // Rate limit exceeded.
                    message = CodicPlugin.getString("messages.api_rate_limit_exceeded");
                }
                Notifications.Bus.notify(
                    new Notification("CodicPlugin", "Codic Plugin Error", message, NotificationType.WARNING),
                    project
                );
            }
        });

        PopupFactoryImpl popupFactory = new PopupFactoryImpl();
        ComponentPopupBuilder builder = popupFactory.createComponentPopupBuilder(
                form.getRoot(), form.getPreferredControl());
        builder.setResizable(true)
                .setRequestFocus(true)
                .setCancelOnClickOutside(false) // Bugfix : Cancel manually.
                .addListener(new JBPopupListener() {
                    @Override
                    public void beforeShown(LightweightWindowEvent lightweightWindowEvent) {

                    }

                    @Override
                    public void onClosed(LightweightWindowEvent lightweightWindowEvent) {
                        if (cancelListener != null) {
                            Toolkit.getDefaultToolkit().removeAWTEventListener(cancelListener);
                            cancelListener = null;
                        }

                        // Save latest size.
                        component.getState().updateQuickLookSize(popup.getContent().getSize());
                        popup.dispose();
                        popup = null;
                    }
                });

        final JBPopup popup = builder.createPopup();
        popup.showInBestPositionFor(editor);

        Dimension size = component.getState().quickLookSize();
        if (size != null) {
            popup.setSize(size);
        }

        cancelListener = new CancelListener(popup) {
            @Override public void cancel() {
                popup.cancel();
            }
        };

        // Bugfix : Cancel manually.
        Toolkit.getDefaultToolkit().addAWTEventListener(this.cancelListener, AWTEvent.MOUSE_MOTION_EVENT_MASK
                | AWTEvent.MOUSE_EVENT_MASK
                | AWTEvent.KEY_EVENT_MASK);

        return popup;
    }

    private String getSelectedText(Editor editor) {
        SelectionModel selectedModel = editor.getSelectionModel();
        return selectedModel.getSelectedText();
    }


    public abstract class CancelListener implements AWTEventListener {

        JBPopup popup;
        public CancelListener(JBPopup popup) {
            this.popup = popup;
        }

        public abstract void cancel();

        @Override
        public void eventDispatched(AWTEvent awtEvent) {
            if (awtEvent instanceof MouseEvent) {
                MouseEvent mouseEvent = (MouseEvent)awtEvent;
                if (mouseEvent.getID() == MouseEvent.MOUSE_CLICKED && !withinPopup(awtEvent)) {
                    cancel();
                }
            }
        }

        private boolean withinPopup(final AWTEvent event) {
            final MouseEvent mouse = (MouseEvent)event;
            final Point point = mouse.getPoint();
            SwingUtilities.convertPointToScreen(point, mouse.getComponent());
            return (new Rectangle(this.popup.getLocationOnScreen(), this.popup.getSize())).contains(point);
        }
    }
}