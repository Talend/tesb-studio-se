// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.esb.runcontainer.ui.console;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsoleFactory;
import org.talend.designer.esb.runcontainer.server.RuntimeServerController;
import org.talend.designer.esb.runcontainer.ui.actions.StartRuntimeAction;
import org.talend.designer.esb.runcontainer.util.RuntimeConsoleUtil;

public class ESBRuntimeConsoleFactory implements IConsoleFactory {

    @Override
    public void openConsole() {
        if (RuntimeServerController.getInstance().isRunning()) {
            RuntimeConsoleUtil.loadConsole();
        } else {
            Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
            if (MessageDialog.openConfirm(shell, "ESB Runtime Console",
                    "ESB Runtime Server is not running, do you want to start it?")) {
                new StartRuntimeAction(true, shell).run();
            } else {
                RuntimeConsoleUtil.findConsole();
            }
        }
    }
}
