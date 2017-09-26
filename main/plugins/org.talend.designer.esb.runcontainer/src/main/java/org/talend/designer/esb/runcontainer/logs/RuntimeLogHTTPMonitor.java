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
package org.talend.designer.esb.runcontainer.logs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.service.log.LogService;
import org.talend.designer.esb.runcontainer.core.ESBRunContainerPlugin;
import org.talend.designer.esb.runcontainer.preferences.RunContainerPreferenceInitializer;
import org.talend.designer.esb.runcontainer.server.RuntimeServerController;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Retrieve runtime by rest request
 */
public class RuntimeLogHTTPMonitor {

    private static RuntimeLogHTTPMonitor instance;

    private Map<RuntimeLogHTTPAdapter, Long> listenerMap;

    private Thread outputThread;

    private Thread outputErrorThread;

    RuntimeLogHTTPMonitor() {
        // init
        listenerMap = new HashMap<RuntimeLogHTTPAdapter, Long>();
    }

    public static RuntimeLogHTTPMonitor createRuntimeLogHTTPMonitor() {
        if (instance == null) {
            instance = new RuntimeLogHTTPMonitor();
        }
        return instance;
    }

    public synchronized void addLogLictener(RuntimeLogHTTPAdapter listener) {
        listenerMap.put(listener, System.currentTimeMillis());
    }

    public synchronized void removeLogLictener(RuntimeLogHTTPAdapter listener) {
        listenerMap.remove(listener);
    }

    public boolean startLogging() {
        // launch http monitor for runtime log
        if (listenerMap.size() == 0) {
            new Thread(new HttpLoggingTask(), "Runtime Http Logging").start();
        }
        if (outputThread == null || !outputThread.isAlive()) {
            outputThread = null;
            outputThread = new Thread(new ProcessOutput(RuntimeServerController.getInstance().getRuntimeProcess()
                    .getInputStream(), false), "Runtime Process Output");
            outputThread.start();
        }

        if (outputErrorThread == null || !outputErrorThread.isAlive()) {
            outputErrorThread = null;
            outputErrorThread = new Thread(new ProcessOutput(RuntimeServerController.getInstance().getRuntimeProcess()
                    .getErrorStream(), true), "Runtime Process Error");
            outputErrorThread.start();
        }
        return true;
    }

    public static void main(String[] args) throws Exception {
        RuntimeLogHTTPMonitor monitor = RuntimeLogHTTPMonitor.createRuntimeLogHTTPMonitor();
        monitor.startLogging();
        monitor.addLogLictener(new RuntimeLogHTTPAdapter() {

            @Override
            public void logReceived(FelixLogsModel logsModel) {
                super.logReceived(logsModel);
            }
        });
    }

    class HttpLoggingTask implements Runnable {

        @Override
        public void run() {
            long latestTime = 0;
            long current = System.currentTimeMillis();
            ObjectMapper mapper = new ObjectMapper();

            IPreferenceStore store = ESBRunContainerPlugin.getDefault().getPreferenceStore();
            boolean sysLog = store.getBoolean(RunContainerPreferenceInitializer.P_ESB_RUNTIME_SYS_LOG);

            store = ESBRunContainerPlugin.getDefault().getPreferenceStore();
            String host = store.getString(RunContainerPreferenceInitializer.P_ESB_RUNTIME_HOST);
            String URL = "http://" + host + ":8040/system/console/logs?traces=true&minLevel=" + LogService.LOG_INFO;
            URL url = null;
            String encoding = null;
            HttpURLConnection connection = null;
            String post = "POST";
            String authorization = "Authorization";
            String json = "\"data\":";
            String sysName = "Apache Karaf";
            try {
                url = new URL(URL);
                String username = store.getString(RunContainerPreferenceInitializer.P_ESB_RUNTIME_USERNAME);
                String password = store.getString(RunContainerPreferenceInitializer.P_ESB_RUNTIME_PASSWORD);
                String BASIC = username + ":" + password;
                encoding = "Basic " + Base64.getEncoder().encodeToString(BASIC.getBytes("UTF-8"));

                do {
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod(post);
                    connection.setRequestProperty(authorization, encoding);
                    InputStream content = connection.getInputStream();
                    BufferedReader in = new BufferedReader(new InputStreamReader(content));
                    String line = in.readLine();

                    if (line != null) {
                        synchronized (listenerMap) {
                            int dataPos = line.indexOf(json) + 7;
                            line = line.substring(dataPos, line.length());
                            FelixLogsModel[] logs = mapper.readValue(line, FelixLogsModel[].class);
                            if (logs[0].getReceived() >= current) {
                                for (int i = logs.length - 1; i >= 0; i--) {
                                    if (latestTime >= logs[i].getReceived()) {
                                        continue;
                                    }
                                    // ignore system logs
                                    if (sysLog && logs[i].getBundleName().startsWith(sysName)) {
                                        continue;
                                    }
                                    // ignore authorization logs
                                    if (logs[i].getMessage().indexOf(authorization) == 0) {
                                        continue;
                                    }

                                    for (IRuntimeLogListener listener : listenerMap.keySet()) {
                                        if (listenerMap.get(listener) < logs[i].getReceived()) {
                                            listener.logReceived(logs[i]);
                                        }
                                    }
                                }
                                latestTime = logs[0].getReceived();
                            }
                        }
                    }

                    synchronized (this) {
                        try {
                            final long waitTime = 500;
                            wait(waitTime);
                        } catch (InterruptedException e) {
                        }
                    }
                } while (listenerMap.size() > 0);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    final class ProcessOutput implements Runnable {

        private final InputStream input;

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
                    if (!input.isEmpty() && listenerMap.size() > 0) {
                        for (IRuntimeLogListener listener : listenerMap.keySet()) {
                            listener.logReceived(input, isError);
                        }
                        System.out.println("------>" + input + "------");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("------>" + "stopped" + isError);
        }
    }

}
