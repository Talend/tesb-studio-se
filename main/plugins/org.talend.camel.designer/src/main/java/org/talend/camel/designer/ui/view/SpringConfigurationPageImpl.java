package org.talend.camel.designer.ui.view;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.Page;
import org.talend.camel.core.model.camelProperties.CamelProcessItem;
import org.talend.camel.designer.i18n.CamelDesignerMessages;
import org.talend.camel.designer.ui.editor.RouteProcess;
import org.talend.camel.designer.util.CamelSpringUtil;
import org.talend.designer.core.ui.editor.AbstractTalendEditor;

public class SpringConfigurationPageImpl extends Page {

    private final CommandStack commandStack;

    private final RouteProcess process;

    private Composite composite;

    private StyledText springText;

    public SpringConfigurationPageImpl(AbstractTalendEditor abstractTalendEditor) {
        this.commandStack = abstractTalendEditor.getCommandStack();
        this.process = (RouteProcess) abstractTalendEditor.getProcess();
    }

    @Override
    public void createControl(Composite parent) {
        composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        composite.setLayout(new GridLayout(2, false));

        springText = new SpringConfigurationStyledText(composite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        springText.setEditable(!process.isReadOnly());
        GridData layoutData = new GridData(GridData.FILL_BOTH);
        layoutData.horizontalSpan = 2;
        springText.setLayoutData(layoutData);
        springText.setText(process.getSpringContent() == null ? "" : process //$NON-NLS-1$
                .getSpringContent());

        Label warning = new Label(composite, SWT.NONE);
        warning.setText(CamelDesignerMessages.getString("SpringConfigurationPageImpl_warningMessage")); //$NON-NLS-1$
        warning.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_RED));

        Button restoreDefaultBtn = new Button(composite, SWT.PUSH);
        restoreDefaultBtn.setText(CamelDesignerMessages.getString("SpringConfigurationPageImpl_restoreDefaultBtn")); //$NON-NLS-1$
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.END;
        restoreDefaultBtn.setLayoutData(gridData);
        restoreDefaultBtn.setToolTipText(CamelDesignerMessages.getString("SpringConfigurationPageImpl_restoreDefaultBtnTooltip")); //$NON-NLS-1$
        restoreDefaultBtn.setEnabled(!process.isReadOnly());

        restoreDefaultBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                springText.setText(CamelSpringUtil.getDefaultContent((CamelProcessItem) process.getProperty().getItem()));
            }
        });

        springText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                commandStack.execute(new ChangeSpringConfigurationCommand(((StyledText) e.widget).getText(), process));
            }
        });
    }

    @Override
    public Control getControl() {
        return composite;
    }

    @Override
    public void setFocus() {
        springText.setFocus();
    }

}
