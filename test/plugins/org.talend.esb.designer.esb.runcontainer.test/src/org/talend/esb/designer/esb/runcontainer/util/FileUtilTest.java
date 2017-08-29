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
package org.talend.esb.designer.esb.runcontainer.util;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.talend.designer.esb.runcontainer.util.FileUtil;

public class FileUtilTest {

    /**
     * Test method for {@link org.talend.designer.esb.runcontainer.util.FileUtil#isContainerArchive(java.lang.String)}.
     * 
     * @throws Exception
     */
    @Test
    public void testIsContainerArchive() throws Exception {
        Bundle b = Platform.getBundle("org.talend.esb.designer.esb.runcontainer.test");
        URL url = FileLocator.toFileURL(FileLocator.find(b, new Path("resources"), null)); //$NON-NLS-1$
        Assert.assertTrue(FileUtil.isContainerArchive(url.getPath() + "/Talend-ESB-V6.4.0-structure.zip"));
        Assert.assertTrue(FileUtil.isContainerArchive(url.getPath() + "/Talend-Runtime-V6.4.0-structure.zip"));
        Assert.assertFalse(FileUtil.isContainerArchive(url.getPath() + "/Talend-ESB-V6.4.0-missing-trun.zip"));
        Assert.assertFalse(FileUtil.isContainerArchive(url.getPath() + "/Talend-Runtime-V6.4.0-structure-missing-etc.zip"));
    }

}
