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
package org.talend.camel.designer.ui.editor.dependencies;

import java.util.Collection;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.talend.designer.camel.dependencies.core.model.ManifestItem;

/**
 * uneditable if readonly
 */
public class CheckedCamelDependenciesPanel extends CamelDependenciesPanel {

    private static final ICheckStateProvider checkStateProvider = new ICheckStateProvider() {

        @Override
        public boolean isChecked(Object element) {
            return !((ManifestItem) element).isOptional();
        }

        @Override
        public boolean isGrayed(Object element) {
            return ((ManifestItem) element).isBuiltIn();
        }
    };

    private ToolItem selectAll;

    private ToolItem deselectAll;

    public CheckedCamelDependenciesPanel(Composite parent, String type, boolean isReadOnly, final IMessagePart messagePart,
            final IRouterDependenciesChangedListener dependenciesChangedListener) {
        super(parent, type, isReadOnly, messagePart, dependenciesChangedListener);

        selectAll.setEnabled(!isReadOnly);
        deselectAll.setEnabled(!isReadOnly);
    }

    private void selectAll(boolean state) {
        boolean hasChanged = false;
        Collection<? extends ManifestItem> input = getInput();
        for (ManifestItem bcp : input) {
            if (bcp.isBuiltIn() || !bcp.isOptional() == state) {
                continue;
            }
            hasChanged = true;
            bcp.setOptional(!state);
            ((CheckboxTableViewer) tableViewer).setChecked(bcp, state);
        }
        if (hasChanged) {
            fireDependenciesChangedListener();
        }
    }

    @Override
    protected TableViewer createTableViewer() {
        final CheckboxTableViewer viewer = CheckboxTableViewer.newCheckList(this,
                SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        viewer.setCheckStateProvider(checkStateProvider);
        viewer.addCheckStateListener(new ICheckStateListener() {

            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                if (((ManifestItem) event.getElement()).isBuiltIn()) {
                    viewer.setChecked(event.getElement(), !event.getChecked());
                } else {
                    ((ManifestItem) event.getElement()).setOptional(!event.getChecked());
                    fireDependenciesChangedListener();
                }
            }
        });
        return viewer;
    }

    @Override
    protected void createButtons(ToolBar tb) {
        final SelectionListener selectionListener = new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (e.getSource() == selectAll) {
                    selectAll(true);
                } else if (e.getSource() == deselectAll) {
                    selectAll(false);
                }
            }
        };

        selectAll = new ToolItem(tb, SWT.PUSH);
        selectAll.setText(Messages.RouterDependenciesPanel_selectAll);
        selectAll.addSelectionListener(selectionListener);

        deselectAll = new ToolItem(tb, SWT.PUSH);
        deselectAll.setText(Messages.RouterDependenciesPanel_deselectAll);
        deselectAll.addSelectionListener(selectionListener);
    }

}
