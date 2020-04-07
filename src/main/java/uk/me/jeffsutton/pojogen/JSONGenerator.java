package uk.me.jeffsutton.pojogen;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Created by jeff on 10/12/2015.
 */
public class JSONGenerator extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        // Set the availability based on whether a project is open
        Project project = e.getProject();
        e.getPresentation().setEnabledAndVisible(project != null);
    }


    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        JSONOptions form = new JSONOptions(project);
        form.setTitle("JSON POJO Generator");
        form.pack();
        form.setLocationRelativeTo(e.getInputEvent().getComponent());
        form.setResizable(true);
        form.setModal(true);
        form.setVisible(true);
    }
}
