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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.talend.designer.esb.runcontainer.util.JMXUtil;

public class CheckingBundlesProgress extends RuntimeProgress {

    @Override
    public void run(IProgressMonitor parentMonitor) throws InvocationTargetException, InterruptedException {
        if (parentMonitor != null) {
            SubMonitor subMonitor = SubMonitor.convert(parentMonitor, 1);
            subMonitor.setTaskName("Checking runtime bundles..."); //$NON-NLS-1$
            if (checkRunning()) {
                waitForActive(subMonitor);
                subMonitor.worked(1);
            }
        } else {
            waitForActive();
        }
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
}
