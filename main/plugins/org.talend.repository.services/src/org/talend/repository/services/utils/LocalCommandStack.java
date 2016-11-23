package org.talend.repository.services.utils;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;

public class LocalCommandStack extends CommandStack {

    private LocalWSDLEditor editor;

    public LocalCommandStack(LocalWSDLEditor editor) {
        super();
        this.editor = editor;
    }

    @Override
    public void execute(Command command) {
        if (editor.isEditorInputReadOnly()) {
            return;
        }
        super.execute(command);
    }

}
