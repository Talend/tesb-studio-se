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
package org.talend.designer.esb.runcontainer.ui.dialog;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.talend.designer.esb.runcontainer.core.ESBRunContainerPlugin;
import org.talend.designer.esb.runcontainer.preferences.RunContainerPreferenceInitializer;
import org.talend.designer.esb.runcontainer.util.JMXUtil;

public class RunClientDialog extends TitleAreaDialog {

    private StyledText logStyledText;

    private String command;

    private List<StyleRange> redStyleRanges = new ArrayList<StyleRange>();

    /**
     * Create the dialog.
     * 
     * @param parentShell
     */
    public RunClientDialog(Shell parentShell) {
        super(parentShell);
        setShellStyle(SWT.SHELL_TRIM);
        setHelpAvailable(false);
    }

    /**
     * Create contents of the dialog.
     * 
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        setMessage("Runtime client running information");
        setTitle("Initalize Runtime Server");
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayout(new GridLayout(1, false));
        container.setLayoutData(new GridData(GridData.FILL_BOTH));

        logStyledText = new StyledText(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
        logStyledText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        logStyledText.setText("Checking for all bundles start...");
        return area;
    }

    /**
     * Create contents of the button bar.
     * 
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false).setEnabled(false);
    }

    public static void runClientWithCommandConsole(Shell shell, String command) {
        RunClientDialog dlg = new RunClientDialog(shell);
        dlg.setCommand(command);
        dlg.open();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.window.Window#open()
     */
    @Override
    public int open() {
        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                redStyleRanges.clear();
                // wait half sec to start running commands, thus the empty console will be shown clearly.
                waitForActive();
                try {
                    IPreferenceStore store = ESBRunContainerPlugin.getDefault().getPreferenceStore();
                    File containerDir = new File(store.getString(RunContainerPreferenceInitializer.P_ESB_RUNTIME_LOCATION));
                    Process process = Runtime.getRuntime().exec(command, null, containerDir);

                    new Thread(new ProcessOutput(process.getInputStream(), false)).start();
                    new Thread(new ProcessOutput(process.getErrorStream(), true)).start();
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                process.waitFor();
                            } catch (InterruptedException e) {
                            }
                            Display.getDefault().asyncExec(new Runnable() {

                                @Override
                                public void run() {
                                    getButton(IDialogConstants.OK_ID).setEnabled(true);
                                }
                            });
                        }
                    }).start();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        return super.open();
    }

    private void setCommand(String commands) {
        this.command = commands;
    }

    final class ProcessOutput implements Runnable {

        private static final char LINE = '\n';

        private Color red = Display.getDefault().getSystemColor(SWT.COLOR_RED);

        private InputStream input;

        private boolean isError;

        ProcessOutput(InputStream input, boolean isError) {
            this.input = input;
            this.isError = isError;
        }

        @Override
        public void run() {
            try {
                BufferedReader inReader = new BufferedReader(new InputStreamReader(input));
                String input = null;
                while ((input = inReader.readLine()) != null) {
                    final String fInput = input;
                    Display.getDefault().asyncExec(new Runnable() {

                        @Override
                        public void run() {
                            String log = logStyledText.getText();
                            logStyledText.setText(log + LINE + fInput);
                            if (isError) {
                                StyleRange styleRange = new StyleRange();
                                styleRange.foreground = red;
                                styleRange.start = log.length() + 1;
                                styleRange.length = fInput.length();
                                redStyleRanges.add(styleRange);
                            }
                            logStyledText.setStyleRanges(redStyleRanges.toArray(new StyleRange[0]));
                            logStyledText.setSelection(log.length() + 1);
                        }
                    });
                }
            } catch (IOException e) {
            }
        }
    }

    private void waitForActive() {
        try {
            int deactiveCount = 0;
            do {
                long[] bundleList = JMXUtil.getBundlesList();
                deactiveCount = bundleList.length;
                for (long id : bundleList) {
                    if ("Active".equals(JMXUtil.getBundleStatus(id))) {
                        deactiveCount--;
                    }
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (deactiveCount < 5);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
