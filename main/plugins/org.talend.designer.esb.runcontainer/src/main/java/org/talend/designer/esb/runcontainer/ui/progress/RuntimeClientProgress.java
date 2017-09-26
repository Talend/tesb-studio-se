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
package org.talend.designer.esb.runcontainer.ui.progress;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.talend.designer.esb.runcontainer.core.ESBRunContainerPlugin;
import org.talend.designer.esb.runcontainer.preferences.RunContainerPreferenceInitializer;
import org.talend.designer.esb.runcontainer.util.JMXUtil;

public class RuntimeClientProgress extends RuntimeProgress {

    private final String command;

    private final List<String> log;

    public RuntimeClientProgress(String command) {
        this.command = command;
        this.log = new ArrayList<>();
    }

    @Override
    public void run(IProgressMonitor parentMonitor) throws InvocationTargetException, InterruptedException {
        System.out.println("Running command " + command);
        SubMonitor subMonitor = SubMonitor.convert(parentMonitor, 10);
        subMonitor.setTaskName("Running script (" + command + ")"); //$NON-NLS-1$
        if (checkRunning()) {
            subMonitor.subTask("Checking runtime bundles...");
            waitForActive(subMonitor);
            subMonitor.worked(2);

            IPreferenceStore store = ESBRunContainerPlugin.getDefault().getPreferenceStore();
            File containerDir = new File(store.getString(RunContainerPreferenceInitializer.P_ESB_RUNTIME_LOCATION));
            try {
                subMonitor.subTask("Script is running to end...");
                executeScript(containerDir);
                subMonitor.worked(8);

                int size = log.size();
                if (!log.get(size - 1).equals("EOF")) {
                    System.out.println("initlocal.sh logs: " + getLog());

                    StackTraceElement[] stackTrace = new StackTraceElement[size];
                    for (int i = 0; i < size; i++) {
                        stackTrace[i] = new StackTraceElement("RuntimeClientProgress", log.get(i), "Unknown source", i);
                    }
                    InterruptedException e = new InterruptedException("Script initlocal.sh run failed");
                    e.setStackTrace(stackTrace);
                    throw e;
                }
            } catch (IOException e) {
                throw new InvocationTargetException(e);
            }
        }
    }

    private void executeScript(File containerDir) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(command, null, containerDir);
        new Thread(new ProcessOutput(process.getInputStream(), false)).start();
        new Thread(new ProcessOutput(process.getErrorStream(), true)).start();
        process.waitFor();
    }

    /**
     * Ensure all bundles must be actived/resolved before running script
     * 
     * @param subMonitor
     */
    private void waitForActive(SubMonitor subMonitor) {
        int bundles = 0;
        int activeCount = 0;
        do {
            try {
                Thread.sleep(1000);
                subMonitor.subTask(JMXUtil.getBundlesList().length + " bundles have been actived");
                if (JMXUtil.getBundlesList().length < 80) {
                    Thread.sleep(1000);
                    continue;
                }

                if (bundles != JMXUtil.getBundlesList().length) {
                    bundles = JMXUtil.getBundlesList().length;
                    Thread.sleep(1000);
                    continue;
                }

                int actived = 0;
                Thread.sleep(4000);
                for (long id : JMXUtil.getBundlesList()) {
                    if ("Active".equals(JMXUtil.getBundleStatus(id))) {
                        actived++;
                    }
                }
                if (activeCount != actived) {
                    activeCount = actived;
                    Thread.sleep(1000);
                    continue;
                } else {
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } while (!subMonitor.isCanceled());
    }

    /**
     * Ensure all bundles must be actived/resolved before running script
     */
    private void waitForActive() {
        boolean allActived = false;
        int bundles = 0;
        do {
            try {
                Thread.sleep(2000);
                long[] bundleList = JMXUtil.getBundlesList();
                if (bundleList.length < 80) {
                    Thread.sleep(1000);
                    continue;
                }

                if (bundles != bundleList.length) {
                    bundles = bundleList.length;
                    Thread.sleep(2000);
                    continue;
                }
                int activeCount = 0;
                for (long id : bundleList) {
                    if ("Active".equals(JMXUtil.getBundleStatus(id))) {
                        activeCount++;
                    }
                }
                if (bundles - activeCount < 5) {
                    allActived = true;
                }
                Thread.sleep(2000);
                bundleList = JMXUtil.getBundlesList();
                if (bundles != bundleList.length) {
                    bundles = bundleList.length;
                    allActived = false;
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } while (!allActived);
    }

    /**
     * Getter for log.
     * 
     * @return the log
     */
    public String getLog() {
        return this.log.toString();
    }

    final class ProcessOutput implements Runnable {

        private static final char LINE = '\n';

        private final InputStream input;

        ProcessOutput(InputStream input, boolean isError) {
            this.input = input;
        }

        @Override
        public void run() {
            try {
                BufferedReader inReader = new BufferedReader(new InputStreamReader(input));
                String input = null;
                while ((input = inReader.readLine()) != null) {
                    if (!input.isEmpty()) {
                        synchronized (log) {
                            log.add(input);
                        }
                    }
                }
            } catch (IOException e) {
            }
        }
    }

}
