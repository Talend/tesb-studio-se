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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Console;
import java.io.FileInputStream;
import java.io.IOError;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.Charset;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.karaf.client.ClientConfig;
import org.apache.sshd.client.ClientBuilder;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.auth.keyboard.UserInteraction;
import org.apache.sshd.client.channel.ChannelShell;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.channel.PtyCapableChannelSession;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.RuntimeSshException;
import org.apache.sshd.common.channel.PtyMode;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.jline.terminal.Attributes;
import org.jline.terminal.Attributes.ControlChar;
import org.jline.terminal.Attributes.InputFlag;
import org.jline.terminal.Attributes.LocalFlag;
import org.jline.terminal.Attributes.OutputFlag;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.impl.SimpleLogger;
import org.talend.designer.esb.runcontainer.util.RuntimeConsoleUtil;

import com.sun.xml.internal.ws.util.NoCloseInputStream;

public class RuntimeClient {

    boolean connected = false;

    public void connect(String[] args) throws Exception {
        ClientConfig config = new ClientConfig(args);
        SimpleLogger.setLevel(config.getLevel());

        if (config.getFile() != null) {
            StringBuilder sb = new StringBuilder();
            sb.setLength(0);
            try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(config.getFile())))) {
                for (int c = reader.read(); c >= 0; c = reader.read()) {
                    sb.append((char) c);
                }
            }
            config.setCommand(sb.toString());
        } else if (config.isBatch()) {
            StringBuilder sb = new StringBuilder();
            sb.setLength(0);
            Reader reader = new BufferedReader(new InputStreamReader(System.in));
            for (int c = reader.read(); c >= 0; c = reader.read()) {
                sb.append((char) c);
            }
            config.setCommand(sb.toString());
        }

        SshClient client = ClientBuilder.builder().build();
        // setupAgent(config.getUser(), config.getKeyFile(), client);
        // client.getProperties().put(FactoryManager.IDLE_TIMEOUT, String.valueOf(config.getIdleTimeout()));
        final Console console = System.console();
        if (console != null) {
            client.setUserInteraction(new UserInteraction() {

                @Override
                public void welcome(ClientSession s, String banner, String lang) {
                    System.err.println(banner);
                }

                @Override
                public String[] interactive(ClientSession s, String name, String instruction, String lang, String[] prompt,
                        boolean[] echo) {
                    String[] answers = new String[prompt.length];
                    try {
                        for (int i = 0; i < prompt.length; i++) {
                            if (echo[i]) {
                                answers[i] = console.readLine(prompt[i] + " ");
                            } else {
                                answers[i] = new String(console.readPassword(prompt[i] + " "));
                            }
                            if (answers[i] == null) {
                                return null;
                            }
                        }
                        return answers;
                    } catch (IOError e) {
                        return null;
                    }
                }

                @Override
                public boolean isInteractionAllowed(ClientSession session) {
                    return true;
                }

                @Override
                public void serverVersionInfo(ClientSession session, List<String> lines) {
                }

                @Override
                public String getUpdatedPassword(ClientSession session, String prompt, String lang) {
                    return null;
                }
            });
        }
        client.start();

        ClientSession session = connectWithRetries(client, config);

        if (config.getPassword() != null) {
            session.addPasswordIdentity(config.getPassword());
        }
        session.auth().verify();

        int exitStatus = 0;
        Terminal terminal = TerminalBuilder.terminal();
        Attributes attributes = terminal.enterRawMode();
        IOConsoleOutputStream outputStream = RuntimeConsoleUtil.getOutputStream();

        try {
            ClientChannel channel;
            if (config.getCommand().length() > 0) {
                channel = session.createChannel("exec", config.getCommand() + "\n");
                channel.setIn(new ByteArrayInputStream(new byte[0]));
            } else {
                ChannelShell shell = session.createShellChannel();
                channel = shell;

                channel.setIn(new NoCloseInputStream(inputStream));

                Map<PtyMode, Integer> modes = new HashMap<>();
                // Control chars
                modes.put(PtyMode.VINTR, attributes.getControlChar(ControlChar.VINTR));
                modes.put(PtyMode.VQUIT, attributes.getControlChar(ControlChar.VQUIT));
                modes.put(PtyMode.VERASE, attributes.getControlChar(ControlChar.VERASE));
                modes.put(PtyMode.VKILL, attributes.getControlChar(ControlChar.VKILL));
                modes.put(PtyMode.VEOF, attributes.getControlChar(ControlChar.VEOF));
                modes.put(PtyMode.VEOL, attributes.getControlChar(ControlChar.VEOL));
                modes.put(PtyMode.VEOL2, attributes.getControlChar(ControlChar.VEOL2));
                modes.put(PtyMode.VSTART, attributes.getControlChar(ControlChar.VSTART));
                modes.put(PtyMode.VSTOP, attributes.getControlChar(ControlChar.VSTOP));
                modes.put(PtyMode.VSUSP, attributes.getControlChar(ControlChar.VSUSP));
                modes.put(PtyMode.VDSUSP, attributes.getControlChar(ControlChar.VDSUSP));
                modes.put(PtyMode.VREPRINT, attributes.getControlChar(ControlChar.VREPRINT));
                modes.put(PtyMode.VWERASE, attributes.getControlChar(ControlChar.VWERASE));
                modes.put(PtyMode.VLNEXT, attributes.getControlChar(ControlChar.VLNEXT));
                modes.put(PtyMode.VSTATUS, attributes.getControlChar(ControlChar.VSTATUS));
                modes.put(PtyMode.VDISCARD, attributes.getControlChar(ControlChar.VDISCARD));
                // Input flags
                modes.put(PtyMode.IGNPAR, getFlag(attributes, InputFlag.IGNPAR));
                modes.put(PtyMode.PARMRK, getFlag(attributes, InputFlag.PARMRK));
                modes.put(PtyMode.INPCK, getFlag(attributes, InputFlag.INPCK));
                modes.put(PtyMode.ISTRIP, getFlag(attributes, InputFlag.ISTRIP));
                modes.put(PtyMode.INLCR, getFlag(attributes, InputFlag.INLCR));
                modes.put(PtyMode.IGNCR, getFlag(attributes, InputFlag.IGNCR));
                modes.put(PtyMode.ICRNL, getFlag(attributes, InputFlag.ICRNL));
                modes.put(PtyMode.IXON, getFlag(attributes, InputFlag.IXON));
                modes.put(PtyMode.IXANY, getFlag(attributes, InputFlag.IXANY));
                modes.put(PtyMode.IXOFF, getFlag(attributes, InputFlag.IXOFF));
                // Local flags
                modes.put(PtyMode.ISIG, getFlag(attributes, LocalFlag.ISIG));
                modes.put(PtyMode.ICANON, getFlag(attributes, LocalFlag.ICANON));
                modes.put(PtyMode.ECHO, getFlag(attributes, LocalFlag.ECHO));
                modes.put(PtyMode.ECHOE, getFlag(attributes, LocalFlag.ECHOE));
                modes.put(PtyMode.ECHOK, getFlag(attributes, LocalFlag.ECHOK));
                modes.put(PtyMode.ECHONL, getFlag(attributes, LocalFlag.ECHONL));
                modes.put(PtyMode.NOFLSH, getFlag(attributes, LocalFlag.NOFLSH));
                modes.put(PtyMode.TOSTOP, getFlag(attributes, LocalFlag.TOSTOP));
                modes.put(PtyMode.IEXTEN, getFlag(attributes, LocalFlag.IEXTEN));
                // Output flags
                modes.put(PtyMode.OPOST, getFlag(attributes, OutputFlag.OPOST));
                modes.put(PtyMode.ONLCR, getFlag(attributes, OutputFlag.ONLCR));
                modes.put(PtyMode.OCRNL, getFlag(attributes, OutputFlag.OCRNL));
                modes.put(PtyMode.ONOCR, getFlag(attributes, OutputFlag.ONOCR));
                modes.put(PtyMode.ONLRET, getFlag(attributes, OutputFlag.ONLRET));
                shell.setPtyModes(modes);
                shell.setPtyColumns(terminal.getWidth());
                shell.setPtyLines(terminal.getHeight());
                shell.setAgentForwarding(true);
                String ctype = System.getenv("LC_CTYPE");
                if (ctype == null) {
                    ctype = Locale.getDefault().toString() + "."
                            + System.getProperty("input.encoding", Charset.defaultCharset().name());
                }
                shell.setEnv("LC_CTYPE", ctype);
            }

            channel.setOut(outputStream);
            channel.setErr(outputStream);
            channel.open().verify();
            if (channel instanceof PtyCapableChannelSession) {
                registerSignalHandler(terminal, (PtyCapableChannelSession) channel);
            }
            connected = true;
            channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), 0);
            if (channel.getExitStatus() != null) {
                exitStatus = channel.getExitStatus();
            }
            channel.close();
        } finally {
            terminal.setAttributes(attributes);
            client.stop();
            client.close();
            connected = false;
            if (!outputStream.isClosed()) {
                outputStream.close();
            }
        }
    }

    public boolean isConnected() {
        return connected;
    }

    private InputStream inputStream;

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    private static int getFlag(Attributes attributes, InputFlag flag) {
        return attributes.getInputFlag(flag) ? 1 : 0;
    }

    private static int getFlag(Attributes attributes, OutputFlag flag) {
        return attributes.getOutputFlag(flag) ? 1 : 0;
    }

    private static int getFlag(Attributes attributes, LocalFlag flag) {
        return attributes.getLocalFlag(flag) ? 1 : 0;
    }

    private static ClientSession connectWithRetries(SshClient client, ClientConfig config) throws Exception, InterruptedException {
        ClientSession session = null;
        int retries = 0;
        do {
            try {
                ConnectFuture future = client.connect(config.getUser(), config.getHost(), config.getPort());
                future.await();
                session = future.getSession();
            } catch (RuntimeSshException ex) {
                if (++retries < 10) {
                    TimeUnit.SECONDS.sleep(2);
                } else {
                    throw ex;
                }
            }
        } while (session == null);
        return session;
    }

    private static void registerSignalHandler(final Terminal terminal, final PtyCapableChannelSession channel) {
        try {
            Class<?> signalClass = Class.forName("sun.misc.Signal");
            Class<?> signalHandlerClass = Class.forName("sun.misc.SignalHandler");
            // Implement signal handler
            Object signalHandler = Proxy.newProxyInstance(RuntimeClient.class.getClassLoader(),
                    new Class<?>[] { signalHandlerClass }, new InvocationHandler() {

                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            Size size = terminal.getSize();
                            channel.sendWindowChange(size.getColumns(), size.getRows());
                            return null;
                        }
                    });
            signalClass.getMethod("handle", signalClass, signalHandlerClass).invoke(null,
                    signalClass.getConstructor(String.class).newInstance("WINCH"), signalHandler);
        } catch (Exception e) {
            // Ignore this exception, if the above failed, the signal API is incompatible with what we're expecting
        }
    }
}