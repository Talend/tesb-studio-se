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

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.designer.esb.runcontainer.core.ESBRunContainerPlugin;
import org.talend.designer.esb.runcontainer.preferences.RunContainerPreferenceInitializer;
import org.talend.designer.esb.runcontainer.ui.console.RuntimeClient;

public class RuntimeConsoleUtil {

    public static final String KARAF_CONSOLE = "ESB Runtime";

    public static IOConsole findConsole() {
        ConsolePlugin plugin = ConsolePlugin.getDefault();
        IConsoleManager conMan = plugin.getConsoleManager();
        IConsole[] existing = conMan.getConsoles();
        for (int i = 0; i < existing.length; i++) {
            if (KARAF_CONSOLE.equals(existing[i].getName()))
                return (IOConsole) existing[i];
        }
        // no console found, so create a new one
        IOConsole myConsole = new IOConsole(KARAF_CONSOLE, null);
        conMan.addConsoles(new IConsole[] { myConsole });
        return myConsole;
    }

    public static IOConsoleOutputStream getOutputStream() {
        IOConsoleOutputStream outputStream = findConsole().newOutputStream();
        outputStream.setEncoding(System.getProperty("sun.jnu.encoding", "UTF-8"));
        return outputStream;
    }

    public static void clearConsole() {
        ConsolePlugin plugin = ConsolePlugin.getDefault();
        IConsoleManager conMan = plugin.getConsoleManager();
        IConsole[] existing = conMan.getConsoles();
        for (int i = 0; i < existing.length; i++) {
            if (KARAF_CONSOLE.equals(existing[i].getName())) {
                ((IOConsole) existing[i]).destroy();
                conMan.removeConsoles(new IConsole[] { existing[i] });
            }
        }
    }

    public static void loadConsole() {
        clearConsole();
        RuntimeClient client = new RuntimeClient();

        PipedInputStream pis = new PipedInputStream();
        PipedOutputStream pos = new PipedOutputStream();

        try {
            pos.connect(pis);
        } catch (IOException e) {
            ExceptionHandler.process(e);
        }
        client.setInputStream(pis);

        Thread consoleThread = new Thread("Runtime Console Input") {

            @Override
            public void run() {
                InputStream is = findConsole().getInputStream();

                int count = 0;
                byte[] bs = new byte[1024];
                try {
                    while ((count = is.read(bs)) > 0) {
                        // remore duplicate \r\n for Windows
                        if (count > 1 && bs[count - 1] == 10 && bs[count - 2] == 13) {
                            count--;
                        }
                        pos.write(Arrays.copyOf(bs, count));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        pos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        consoleThread.start();

        Thread connectThread = new Thread("Runtime Console Connector") {

            @Override
            public void run() {
                IPreferenceStore store = ESBRunContainerPlugin.getDefault().getPreferenceStore();
                String etcLocation = store.getString(RunContainerPreferenceInitializer.P_ESB_RUNTIME_LOCATION);
                String host = store.getString(RunContainerPreferenceInitializer.P_ESB_RUNTIME_HOST);
                System.setProperty("karaf.etc", etcLocation + "/etc");
                String[] karafArgs = new String[] { "-h", host };

                try {
                    client.connect(karafArgs);
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }
        };
        connectThread.start();
    }
}
