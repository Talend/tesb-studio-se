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

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.part.IPageBookViewPage;
import org.talend.designer.esb.runcontainer.core.ESBRunContainerPlugin;
import org.talend.designer.esb.runcontainer.server.RuntimeServerController;
import org.talend.designer.esb.runcontainer.server.RuntimeStatusChangeListener;
import org.talend.designer.esb.runcontainer.ui.actions.OpenRuntimeInfoAction;
import org.talend.designer.esb.runcontainer.ui.actions.OpenRuntimePrefsAction;
import org.talend.designer.esb.runcontainer.ui.actions.StartRuntimeAction;
import org.talend.designer.esb.runcontainer.ui.actions.StopRuntimeAction;
import org.talend.designer.esb.runcontainer.ui.console.ansi.AnsiConsoleStyleListener;

public class ESBRuntimeConsolePageParticipant implements IConsolePageParticipant {

    private StartRuntimeAction startRuntimeAction;

    private StopRuntimeAction haltRuntimeAction;

    private OpenRuntimeInfoAction openRuntimeInfoAction;

    private OpenRuntimePrefsAction openRuntimePrefsAction;

    private RuntimeStatusChangeListener serverListener;

    @Override
    public Object getAdapter(Class adapter) {
        return null;
    }

    @Override
    public void init(IPageBookViewPage page, IConsole console) {

        if (page.getControl() instanceof StyledText) {
            StyledText viewer = (StyledText) page.getControl();
            AnsiConsoleStyleListener myListener = new AnsiConsoleStyleListener();
            viewer.addLineStyleListener(myListener);
            ESBRunContainerPlugin.getDefault().addViewer(viewer, this);
        }

        startRuntimeAction = new StartRuntimeAction(true, page.getControl().getShell());
        haltRuntimeAction = new StopRuntimeAction(page.getControl().getShell());
        openRuntimeInfoAction = new OpenRuntimeInfoAction();
        openRuntimePrefsAction = new OpenRuntimePrefsAction();
        serverListener = new RuntimeStatusChangeListener() {

            @Override
            public void stopRunning() {
                Display.getDefault().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        startRuntimeAction.setEnabled(true);
                        haltRuntimeAction.setEnabled(false);
                        openRuntimeInfoAction.setEnabled(false);
                    }
                });
            }

            @Override
            public void startRunning() {
                Display.getDefault().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        startRuntimeAction.setEnabled(false);
                        haltRuntimeAction.setEnabled(true);
                        openRuntimeInfoAction.setEnabled(true);
                    }
                });
            }

            @Override
            public void featureUninstalled(int id) {

            }

            @Override
            public void featureInstalled(int id) {

            }
        };
        RuntimeServerController.getInstance().addStatusChangeListener(serverListener);

        IActionBars actionBars = page.getSite().getActionBars();
        configureToolBar(actionBars.getToolBarManager());
    }

    private void configureToolBar(IToolBarManager mgr) {
        mgr.appendToGroup(IConsoleConstants.LAUNCH_GROUP, startRuntimeAction);
        mgr.appendToGroup(IConsoleConstants.LAUNCH_GROUP, haltRuntimeAction);
        mgr.appendToGroup(IConsoleConstants.OUTPUT_GROUP, openRuntimeInfoAction);
        mgr.appendToGroup(IConsoleConstants.OUTPUT_GROUP, openRuntimePrefsAction);
    }

    @Override
    public void dispose() {
        // startRuntimeAction = null;
        // rebootRuntimeAction = null;
        // haltRuntimeAction = null;
        // openRuntimeInfoAction = null;
    }

    @Override
    public void activated() {
        // TODO Auto-generated method stub
    }

    @Override
    public void deactivated() {
        // TODO Auto-generated method stub
    }

}
