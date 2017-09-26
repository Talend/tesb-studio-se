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
package org.talend.designer.esb.runcontainer.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import org.eclipse.core.runtime.IPath;

public class StatPortChecker implements Runnable {

    private static final String UTF_8 = "UTF-8";

    private static final String PORT_JOB_DEFAULT = "portStats =";

    // private static final String PORT_ROUTE_DEFAULT = "portStats = -1";

    private IPath path;

    private String statCode;

    private boolean stop = false;

    /**
     * @param path
     * @param statPort
     * @param tracePort
     */
    public StatPortChecker(IPath path, int statPort, int tracePort) {
        this.path = path;
        this.statCode = "portStats = " + statPort;
    }

    @Override
    public void run() {
        long modified = 0;
        File code = path.toFile();
        while (!stop) {
            if (code.exists() && code.lastModified() != modified) {
                try {
                    byte[] encoded = Files.readAllBytes(code.toPath());
                    String codeStr = new String(encoded, UTF_8);
                    int pos = codeStr.indexOf(PORT_JOB_DEFAULT);
                    if (pos > 0) {
                        int nbStart = codeStr.indexOf(";", pos);
                        FileWriter writer = new FileWriter(code);
                        BufferedWriter bufferedWriter = new BufferedWriter(writer);
                        bufferedWriter.write(codeStr.substring(0, pos) + statCode + codeStr.substring(nbStart, codeStr.length()));
                        bufferedWriter.flush();
                        bufferedWriter.close();
                        writer.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    modified = code.lastModified();
                }
            }
        }
    }

    public void stop() {
        stop = true;
    }
}
