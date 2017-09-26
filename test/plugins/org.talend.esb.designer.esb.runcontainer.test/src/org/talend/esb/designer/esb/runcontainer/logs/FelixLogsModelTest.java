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
package org.talend.esb.designer.esb.runcontainer.logs;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.talend.designer.esb.runcontainer.logs.FelixLogsModel;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FelixLogsModelTest {

    /**
     * Test method for {@link org.talend.designer.esb.runcontainer.logs.FelixLogsModel#toString()}.
     * 
     * @throws Exception
     */
    @Test
    public void testToString() throws Exception {

        Bundle b = Platform.getBundle("org.talend.esb.designer.esb.runcontainer.test");
        URL url = FileLocator.toFileURL(FileLocator.find(b, new Path("resources"), null)); //$NON-NLS-1$
        File responseFile = new File(url.getPath() + "/FelixLogsModelTest.json"); //$NON-NLS-1$
        Assert.assertTrue(responseFile.exists());
        String json = new String(Files.readAllBytes(responseFile.toPath()));
        String data = "\"data\":";
        int dataPos = json.indexOf(data) + 7;
        String line = json.substring(dataPos, json.length());
        ObjectMapper mapper = new ObjectMapper();
        FelixLogsModel[] logs = mapper.readValue(line, FelixLogsModel[].class);
        Assert.assertEquals(logs.length, 100);

    }

}
