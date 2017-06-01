// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class CheckingBundlesProgressTest {

    /**
     * Test method for
     * {@link org.talend.designer.esb.runcontainer.ui.progress.CheckingBundlesProgress#run(org.eclipse.core.runtime.IProgressMonitor)}
     * .
     * 
     * @throws Exception
     */
    @Test
    public void testRun() throws Exception {
        long[] bundles = new long[] { 1, 2, 3 };
        IProgressMonitor monitor = new NullProgressMonitor();
        monitor.beginTask("test", 1);
        try {
            CheckingBundlesProgress mock = Mockito.mock(CheckingBundlesProgress.class);

            Mockito.when(mock.checkRunning()).thenReturn(true);
            mock.run(monitor);

        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertFalse(monitor.isCanceled());
    }
}
