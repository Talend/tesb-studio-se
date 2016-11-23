package org.talend.repository.services.utils;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class RefactorRenameHandler implements IHandler {

    private IHandler handler = new org.eclipse.wst.wsdl.ui.internal.refactor.handlers.RenameHandler();

    @Override
    public void addHandlerListener(IHandlerListener handlerListener) {
        handler.addHandlerListener(handlerListener);
    }

    @Override
    public void dispose() {
        handler.dispose();
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        return handler.execute(event);
    }

    @Override
    public boolean isEnabled() {
        if (!handler.isEnabled()) {
            return false;
        }
        // disable the command when editor is readonly.
        IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench == null) {
            return false;
        }
        IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        if (window == null) {
            return false;
        }
        IWorkbenchPage activePage = window.getActivePage();
        if (activePage == null) {
            return false;
        }
        IEditorPart activeEditor = activePage.getActiveEditor();
        if (activeEditor != null && activeEditor instanceof LocalWSDLEditor) {
            LocalWSDLEditor wsdlEditor = (LocalWSDLEditor) activeEditor;
            return !wsdlEditor.isEditorInputReadOnly();
        }
        return false;
    }

    @Override
    public boolean isHandled() {
        return handler.isHandled();
    }

    @Override
    public void removeHandlerListener(IHandlerListener handlerListener) {
        handler.removeHandlerListener(handlerListener);
    }

}
