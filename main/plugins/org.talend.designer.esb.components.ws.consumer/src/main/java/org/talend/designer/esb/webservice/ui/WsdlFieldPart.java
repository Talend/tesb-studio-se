package org.talend.designer.esb.webservice.ui;

import java.util.EventListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.talend.commons.ui.swt.formtools.LabelledFileField;
import org.talend.commons.ui.utils.PathUtils;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.ui.proposal.TalendProposalUtils;

interface WsdlFieldListener extends EventListener {

    void refreshPageByWsdl(String filePath);
}

public class WsdlFieldPart extends AbstractWebServiceUIPart<WsdlFieldListener> implements RefreshEventListener {

    public WsdlFieldPart(WsdlFieldListener eventListener) {
        super(eventListener);
    }

    private LabelledFileField wsdlField;

    @Override
    public Control createControl(Composite parent) {
        wsdlField = new LabelledFileField(parent, "WSDL:", new String[] { "*.wsdl", "*.*" }, 1, SWT.BORDER) {

            @Override
            protected void setFileFieldValue(String result) {
                if (result != null) {
                    setRawFieldValue(result);
                }
            }
        };

        wsdlField.getTextControl().addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent event) {
                switch (event.keyCode) {
                case SWT.CR:
                case SWT.KEYPAD_CR:
                    refreshTriggered();
                }
            }
        });
        return wsdlField.getTextControl();
    }

    public void setInitData(org.talend.core.model.process.AbstractNode node, String initialWsdlLocation) {
        // add a listener for ctrl+space.
        TalendProposalUtils.installOn(wsdlField.getTextControl(), node.getProcess(), node);

        if (initialWsdlLocation != null) {
            wsdlField.getTextControl().setText(initialWsdlLocation);
        }
    }

    public void setRawFieldValue(String rawValueWithoutQuotes) {
        String locationWithQuotes = TalendTextUtils.addQuotes(PathUtils.getPortablePath(rawValueWithoutQuotes));
        setFieldValue(locationWithQuotes);
    }

    public void setFieldValue(String wsdlLocationWithQuotes) {
        wsdlField.setText(wsdlLocationWithQuotes);
        listener.refreshPageByWsdl(wsdlField.getText());
    }

    @Override
    public void refreshTriggered() {
        listener.refreshPageByWsdl(wsdlField.getText());
    }
}
