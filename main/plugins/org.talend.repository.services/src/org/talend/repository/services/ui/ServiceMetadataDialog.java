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
package org.talend.repository.services.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EMap;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.PluginChecker;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.ui.dialog.HelpAvailableDialog;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.services.Messages;
import org.talend.repository.services.model.services.ServiceConnection;
import org.talend.repository.services.model.services.ServiceItem;

public class ServiceMetadataDialog extends HelpAvailableDialog {

    private static final String HELP_CONTEXT_ID = "org.talend.help.esb_runtime";

    public static final String SECURITY_BASIC = "Security.Basic"; //$NON-NLS-1$

    public static final String SECURITY_SAML = "Security.SAML"; //$NON-NLS-1$

    public static final String AUTHORIZATION = "Authorization"; //$NON-NLS-1$

    public static final String ENCRYPTION = "Encryption"; //$NON-NLS-1$

    public static final String USE_SERVICE_REGISTRY = "UseServiceRegisrty"; //$NON-NLS-1$

    public static final String USE_SAM = "UseSAM"; //$NON-NLS-1$

    public static final String USE_SL = "UseSL"; //$NON-NLS-1$

    public static final String SL_CUSTOM_PROP_PREFIX = "slCustomProperty_"; //$NON-NLS-1$

    public static final String LOG_MESSAGES = "LogMessages"; //$NON-NLS-1$

    public static final String WSDL_SCHEMA_VALIDATION = "WsdlSchemaValidation"; //$NON-NLS-1$

    public static final String USE_BUSINESS_CORRELATION = "useBusinessCorrelation"; //$NON-NLS-1$

    private final ServiceItem serviceItem;

    private final ServiceConnection serviceConnection;

    private ServiceMetadataCustomPropertiesTable customPropertiesTable;

    private boolean useSAM;

    private boolean useSL;

    private boolean useServiceRegistry;

    private Map<String, String> slCustomProperties = new HashMap<>();

    private boolean securityBasic;

    private boolean securitySAML;

    private boolean authorization;

    private boolean encryption;

    private boolean logMessages;

    private boolean wsdlSchemaValidation;

    private boolean useBusinessCorrelation;

    public ServiceMetadataDialog(IShellProvider parentShell, ServiceItem serviceItem, ServiceConnection serviceConnection) {
        super(parentShell);
        this.serviceItem = serviceItem;
        this.serviceConnection = serviceConnection;
        EMap<String, String> props = serviceConnection.getAdditionalInfo();
        if (props != null) {
            useSAM = Boolean.valueOf(props.get(USE_SAM));
            useSL = Boolean.valueOf(props.get(USE_SL));
            securitySAML = Boolean.valueOf(props.get(SECURITY_SAML));
            securityBasic = Boolean.valueOf(props.get(SECURITY_BASIC));
            authorization = Boolean.valueOf(props.get(AUTHORIZATION));
            encryption = Boolean.valueOf(props.get(ENCRYPTION));
            useServiceRegistry = Boolean.valueOf(props.get(USE_SERVICE_REGISTRY));
            logMessages = Boolean.valueOf(props.get(LOG_MESSAGES));
            wsdlSchemaValidation = Boolean.valueOf(props.get(WSDL_SCHEMA_VALIDATION));
            useBusinessCorrelation = Boolean.valueOf(props.get(USE_BUSINESS_CORRELATION));
            for (Map.Entry<String, String> prop : props.entrySet()) {
                if (prop.getKey().startsWith(SL_CUSTOM_PROP_PREFIX)) {
                    slCustomProperties.put(prop.getKey().substring(SL_CUSTOM_PROP_PREFIX.length()), prop.getValue());
                }
            }
        }
    }

    protected ServiceMetadataDialog(Shell parentShell) {
        super(parentShell);
        serviceItem = null;
        serviceConnection = null;
    }

    @Override
    protected String getHelpContextId() {
        return HELP_CONTEXT_ID;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));
        container.setLayout(new GridLayout());
        final Button useSRCheck;
        if (isStudioEEVersion()) {
            useSRCheck = new Button(container, SWT.CHECK);
        } else {
            useSRCheck = null;
            useServiceRegistry = false;
        }

        Group samSlGroup = new Group(container, SWT.NONE);
        Button schemaValidationCheck = null;
        if (isStudioEEVersion()) {
            schemaValidationCheck = new Button(samSlGroup, SWT.CHECK);
            schemaValidationCheck.setText("Use WSDL Schema Validation");
            schemaValidationCheck.setSelection(wsdlSchemaValidation);
            schemaValidationCheck.setEnabled(!useServiceRegistry);
            schemaValidationCheck.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    wsdlSchemaValidation = ((Button) e.widget).getSelection();
                }
            });
        } else {
            wsdlSchemaValidation = false;
        }
        final Button tmpSchemaValidationCheck = schemaValidationCheck;

        final Button correlationCheck = new Button(samSlGroup, SWT.CHECK);
        final Button samCheck = new Button(samSlGroup, SWT.CHECK);
        final Button slCheck = new Button(samSlGroup, SWT.CHECK);
        final Group securityGroup = new Group(container, SWT.NONE);
        final Button basicCheck = new Button(securityGroup, SWT.CHECK);
        final Button samlCheck = new Button(securityGroup, SWT.CHECK);
        final Button logMessagesCheck = new Button(container, SWT.CHECK);

        final Button authorizationCheck;
        final Button encryptCheck;
        if (isStudioEEVersion()) {
            authorizationCheck = new Button(securityGroup, SWT.CHECK);
            encryptCheck = new Button(securityGroup, SWT.CHECK);
        } else {
            authorizationCheck = null;
            authorization = false;
            encryptCheck = null;
            encryption = false;
        }

        if (isStudioEEVersion()) {
            useSRCheck.setText(Messages.ServiceMetadataDialog_useSRBtnText);
            useSRCheck.setSelection(useServiceRegistry);
            useSRCheck.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    useServiceRegistry = useSRCheck.getSelection();
                    authorizationCheck.setEnabled(!useServiceRegistry && securitySAML /* || securityBasic ) */);
                    encryptCheck.setEnabled(!useServiceRegistry && securitySAML /* || securityBasic ) */);
                    samlCheck.setEnabled(!useServiceRegistry);
                    basicCheck.setEnabled(!useServiceRegistry);
                    samCheck.setEnabled(!useServiceRegistry);
                    correlationCheck.setEnabled(!useServiceRegistry);
                    if (tmpSchemaValidationCheck != null) {
                        tmpSchemaValidationCheck.setEnabled(!useServiceRegistry);
                    }
                }
            });
        }
        samSlGroup.setText(Messages.ServiceMetadataDialog_samSlGroupTitle);
        samSlGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        samSlGroup.setLayout(new GridLayout());

        samCheck.setText(Messages.ServiceMetadataDialog_useSAMBtnText);
        samCheck.setSelection(useSAM);
        samCheck.setEnabled(!useServiceRegistry);
        samCheck.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                useSAM = samCheck.getSelection();
            }
        });

        slCheck.setText(Messages.ServiceMetadataDialog_useSLBtnTExt);
        slCheck.setSelection(useSL);
        slCheck.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                useSL = slCheck.getSelection();
                customPropertiesTable.setEditable(useSL);
            }
        });

        customPropertiesTable = new ServiceMetadataCustomPropertiesTable(samSlGroup, slCustomProperties);
        customPropertiesTable.setEditable(useSL);

        correlationCheck.setText(Messages.ServiceMetadataDialog_useBusinessCorrelation);
        correlationCheck.setEnabled(!useServiceRegistry);
        correlationCheck.setSelection(useBusinessCorrelation);
        correlationCheck.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                useBusinessCorrelation = correlationCheck.getSelection();
            }
        });

        securityGroup.setText(Messages.ServiceMetadataDialog_securityGroupTitle);
        securityGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        securityGroup.setLayout(new GridLayout());

        basicCheck.setText(Messages.ServiceMetadataDialog_usernamePsBtnText);
        basicCheck.setSelection(securityBasic);
        basicCheck.setEnabled(!useServiceRegistry);
        basicCheck.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                securityBasic = basicCheck.getSelection();
            }
        });

        samlCheck.setText(Messages.ServiceMetadataDialog_samlBtnText);
        samlCheck.setSelection(securitySAML);
        samlCheck.setEnabled(!useServiceRegistry);
        samlCheck.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                securitySAML = samlCheck.getSelection();
                if (isStudioEEVersion()) {
                    authorizationCheck.setEnabled(securitySAML);
                    encryptCheck.setEnabled(securitySAML);
                }
            }
        });

        if (isStudioEEVersion()) {
            authorizationCheck.setText(Messages.ServiceMetadataDialog_authorizationBtnText);
            authorizationCheck.setSelection(authorization);
            if (!isStudioEEVersion()) {
                authorizationCheck.setSelection(false);
                authorizationCheck.setVisible(false);
                authorization = false;
            }
            authorizationCheck.setEnabled(!useServiceRegistry && securitySAML);
            authorizationCheck.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    authorization = authorizationCheck.getSelection();
                }
            });

            encryptCheck.setText(Messages.ServiceMetadataDialog_encryptionBtnText);
            encryptCheck.setSelection(encryption);
            if (!isStudioEEVersion()) {
                encryptCheck.setSelection(false);
                encryptCheck.setVisible(false);
                encryption = false;
            }
            encryptCheck.setEnabled(!useServiceRegistry && securitySAML);
            encryptCheck.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    encryption = encryptCheck.getSelection();
                }
            });
        }

        logMessagesCheck.setText(Messages.ServiceMetadataDialog_logMessages);
        logMessagesCheck.setSelection(logMessages);
        logMessagesCheck.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                logMessages = logMessagesCheck.getSelection();
            }
        });

        if (!DesignerPlugin.getDefault().getProxyRepositoryFactory().isEditableAndLockIfPossible(serviceItem)) {
            parent.setEnabled(false);
            getShell().setText(Messages.ServiceMetadataDialog_dialogReadonlyTitle);
        } else {
            parent.setEnabled(true);
            getShell().setText(Messages.ServiceMetadataDialog_dialogTitle);
        }
        return super.createDialogArea(parent);
    }

    private boolean isUseSam() {
        return useSAM;
    }

    private boolean isUseSL() {
        return useSL;
    }

    private boolean getSecurityBasic() {
        return securityBasic;
    }

    private boolean getSecuritySAML() {
        return securitySAML;
    }

    private boolean getAuthorization() {
        return authorization;
    }

    private boolean getEncryption() {
        return encryption;
    }

    private boolean getUseServiceRegistry() {
        return useServiceRegistry;
    }

    private boolean isLogMessages() {
        return logMessages;
    }

    private boolean isWsdlSchemaValidation() {
        return wsdlSchemaValidation;
    }

    private boolean isStudioEEVersion() {
        return PluginChecker.isTIS();
    }

    @Override
    protected void okPressed() {
        EMap<String, String> props = serviceConnection.getAdditionalInfo();
        if (props != null) {
            props.put(USE_SAM, Boolean.toString(isUseSam()));
            props.put(USE_SL, Boolean.toString(isUseSL()));
            props.put(SECURITY_BASIC, Boolean.toString(getSecurityBasic()));
            props.put(SECURITY_SAML, Boolean.toString(getSecuritySAML()));
            props.put(AUTHORIZATION, Boolean.toString(getAuthorization()));
            props.put(ENCRYPTION, Boolean.toString(getEncryption()));
            props.put(USE_SERVICE_REGISTRY, Boolean.toString(getUseServiceRegistry()));
            props.put(LOG_MESSAGES, Boolean.toString(isLogMessages()));
            props.put(WSDL_SCHEMA_VALIDATION, Boolean.toString(isWsdlSchemaValidation()));
            props.put(USE_BUSINESS_CORRELATION, Boolean.toString(useBusinessCorrelation));

            if (isUseSL()) {
                slCustomProperties = new HashMap<>(customPropertiesTable.getPropertiesMap());
                // remove old custom properties
                List<String> props2delete = new ArrayList<>();
                for (String propKey : props.keySet()) {
                    if (propKey.startsWith(SL_CUSTOM_PROP_PREFIX)) {
                        props2delete.add(propKey);
                    }
                }
                for (String propKey : props2delete) {
                    props.removeKey(propKey);
                }
                // set new custom properties
                for (Map.Entry<String, String> prop : slCustomProperties.entrySet()) {
                    props.put(SL_CUSTOM_PROP_PREFIX + prop.getKey(), prop.getValue());
                }
            }
        }

        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        try {
            factory.save(serviceItem);
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        super.okPressed();
    }

    @Override
    protected Point getInitialSize() {
        Point pt = super.getInitialSize();
        pt.x = Math.max(pt.x, 300);
        pt.y = Math.max(pt.y, 300);
        return pt;
    }

    @Override
    protected void setShellStyle(int newShellStyle) {
        super.setShellStyle(newShellStyle | SWT.RESIZE);
    }
}
