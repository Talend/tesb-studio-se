// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
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
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.talend.camel.designer.CamelDesignerPlugin;
import org.talend.camel.designer.ui.editor.dependencies.controls.SearchCellLabelProvider;
import org.talend.camel.designer.ui.editor.dependencies.dialog.NewOrEditDependencyDialog;
import org.talend.designer.camel.dependencies.core.model.ManifestItem;

/**
 * uneditable if readonly
 */
public class CamelDependenciesPanel extends Composite {

    protected final TableViewer tableViewer;
    private final SearchCellLabelProvider labelProvider;

    private final String type;
    private final IRouterDependenciesChangedListener dependenciesChangedListener;

    private ToolItem addBtn;
    private ToolItem remBtn;
    private ToolItem editBtn;
    private ToolItem upBtn;
    private ToolItem downBtn;

    public CamelDependenciesPanel(Composite parent, String type, boolean isReadOnly, final IMessagePart messagePart,
        final IRouterDependenciesChangedListener dependenciesChangedListener) {
		super(parent, SWT.NONE);
		this.type = type;
		this.dependenciesChangedListener = dependenciesChangedListener;

		setLayout(new GridLayout(2, false));

		tableViewer = createTableViewer();
		tableViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));

        if (isReadOnly) {
            // table.setBackground(table.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
            tableViewer.getTable().setEnabled(false);
        } else {
            tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
                @Override
                public void selectionChanged(SelectionChangedEvent event) {
                    final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                    int size = selection.size();
                    if (size == 0) {
                        messagePart.setMessage(null);
                    } else if (selection.size() == 1) {
                        messagePart.setMessage(((ManifestItem) selection.getFirstElement()).getDescription());
                    } else {
                        messagePart.setMessage(size + Messages.RouterDependenciesEditor_multiItemsSelectedStatusMsg);
                    }
                    if (selection == null || selection.isEmpty()) {
                        if (remBtn != null) {
                            remBtn.setEnabled(false);
                        }
                        if (upBtn != null) {
                            upBtn.setEnabled(false);
                        }
                        if (downBtn != null) {
                            downBtn.setEnabled(false);
                        }
                        if (editBtn != null) {
                            editBtn.setEnabled(false);
                        }
                        return;
                    }

                    boolean hasBuiltIn = false;
                    Iterator<?> iterator = selection.iterator();
                    while (iterator.hasNext()) {
                        if (((ManifestItem) iterator.next()).isBuiltIn()) {
                            hasBuiltIn = true;
                            break;
                        }
                    }

                    if (remBtn != null) {
                        remBtn.setEnabled(!hasBuiltIn);
                    }

                    if (editBtn != null) {
                        editBtn.setEnabled(!hasBuiltIn && selection.size() == 1);
                    }

                    if (upBtn != null && downBtn != null) {
                        int nonBuiltInCount = 0;
                        for (ManifestItem item : getInput()) {
                            if (!item.isBuiltIn()) {
                                nonBuiltInCount++;
                            }
                        }
                        boolean state = !hasBuiltIn && selection.size() == 1 && nonBuiltInCount > 1;
                        upBtn.setEnabled(state);
                        downBtn.setEnabled(state);
                    }
                }
            });
            tableViewer.getTable().addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.stateMask == SWT.NONE) {
                        if (remBtn != null && remBtn.isEnabled() && e.keyCode == SWT.DEL) {
                            removeItems();
                        } else if (e.keyCode == SWT.INSERT) {
                            addNewItem();
                        } else if (editBtn != null && editBtn.isEnabled() && e.keyCode == SWT.CR) {
                            editSelected();
                        }
                    }
                }
            });
        }


        labelProvider = new DependenciesTableLabelProvider(tableViewer);
        tableViewer.setLabelProvider(labelProvider);
        tableViewer.setContentProvider(ArrayContentProvider.getInstance());

        ToolBar tb = new ToolBar(this, SWT.FLAT | SWT.VERTICAL);
        tb.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		createButtons(tb);

		if (null != addBtn) {
	        addBtn.setEnabled(!isReadOnly);
		}
	}

    public void setFilterString(String filterString) {
        labelProvider.setFilterString(filterString);
        tableViewer.refresh();
        tableViewer.getTable().redraw();
    }

    public void setShowBuiltIn(boolean showBuiltIn) {
        labelProvider.setShowBuiltIn(showBuiltIn);
        tableViewer.refresh();
    }

    public void setInput(Collection<? extends ManifestItem> manifestItems) {
        tableViewer.setInput(manifestItems);
        tableViewer.refresh();
    }

    public Collection<? extends ManifestItem> getInput() {
        return (Collection<? extends ManifestItem>) tableViewer.getInput();
    }

	protected void fireDependenciesChangedListener() {
	    dependenciesChangedListener.dependencesChanged(this);
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
	    tableViewer.addSelectionChangedListener(listener);
	}

	private void editSelected() {
		final ManifestItem selected =
		    (ManifestItem) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();
		final NewOrEditDependencyDialog dialog =
		    new NewOrEditDependencyDialog(getInput(), selected, getShell(), type);
		if (dialog.open() == Dialog.OK) {
			ManifestItem item = dialog.getManifestItem();
			selected.setName(item.getName());
			selected.setOptional(item.isOptional());
			selected.setVersion(item.getVersion());
			tableViewer.update(selected, null);
			fireDependenciesChangedListener();
		}
	}

	private void moveDown() {
		ManifestItem selected = (ManifestItem) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();

		List input = (List) tableViewer.getInput();
		int index = input.indexOf(selected);
		int size = input.size();
		int targetIndex = index + 1;
		if (targetIndex >= size) {
			for (targetIndex = 0; targetIndex < size && ((ManifestItem)input.get(targetIndex)).isBuiltIn(); ++targetIndex) {
			}
		}
		input.remove(selected);
		input.add(targetIndex, selected);
		tableViewer.refresh();
		tableViewer.setSelection(new StructuredSelection(selected));
		tableViewer.getTable().showSelection();
		fireDependenciesChangedListener();
	}

	private void moveUp() {
	    ManifestItem selected = (ManifestItem) ((IStructuredSelection)tableViewer.getSelection()).getFirstElement();

		List input = (List) tableViewer.getInput();
		int index = input.indexOf(selected);
		int size = input.size();
		int targetIndex = index - 1;
		if (targetIndex < 0 || ((ManifestItem)input.get(targetIndex)).isBuiltIn()){
			targetIndex = size -1;
		}
		input.remove(selected);
		input.add(targetIndex, selected);
		tableViewer.refresh();
		tableViewer.setSelection(new StructuredSelection(selected));
		tableViewer.getTable().showSelection();
		fireDependenciesChangedListener();
	}

	private void removeItems() {
		boolean yes = MessageDialog.openConfirm(getShell(), Messages.RouterDependenciesPanel_deleteTitle, Messages.RouterDependenciesPanel_deleteMsg);
		if(!yes){
			return;
		}
		IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
		Collection<? extends ManifestItem> input = getInput();
		Iterator<?> iterator = selection.iterator();
		while(iterator.hasNext()){
			input.remove(iterator.next());
		}
		tableViewer.refresh();
		fireDependenciesChangedListener();
	}

	private void addNewItem() {
	    final Collection<ManifestItem> input = (Collection<ManifestItem>) getInput();
		NewOrEditDependencyDialog dialog = new NewOrEditDependencyDialog(input, getShell(), type);
		if (dialog.open() == Dialog.OK) {
	        final ManifestItem addedItem = dialog.getManifestItem();
	        input.add(addedItem);
	        tableViewer.refresh();
	        tableViewer.setSelection(new StructuredSelection(addedItem));
	        tableViewer.getTable().showSelection();
	        tableViewer.getTable().setFocus();
	        fireDependenciesChangedListener();
		}
	}

    protected TableViewer createTableViewer() {
        return new TableViewer(this);
    }

    protected void createButtons(ToolBar tb) {
        final SelectionListener selectionListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (e.getSource() == addBtn) {
                    addNewItem();
                } else if (e.getSource() == remBtn) {
                    removeItems();
                } else if (e.getSource() == upBtn) {
                    moveUp();
                } else if (e.getSource() == downBtn) {
                    moveDown();
                } else if (e.getSource() == editBtn) {
                    editSelected();
                }
            }
        };

        addBtn = new ToolItem(tb, SWT.PUSH);
        addBtn.setText(Messages.RouterDependenciesPanel_addBtn);
        addBtn.addSelectionListener(selectionListener);

        remBtn = new ToolItem(tb, SWT.PUSH);
        remBtn.setText(Messages.RouterDependenciesPanel_removeBtn);
        remBtn.addSelectionListener(selectionListener);
        remBtn.setEnabled(false);

        editBtn = new ToolItem(tb, SWT.PUSH);
        editBtn.setText(Messages.RouterDependenciesPanel_editBtn);
        editBtn.setEnabled(false);
        editBtn.addSelectionListener(selectionListener);

        if (type != ManifestItem.EXPORT_PACKAGE) {
            upBtn = new ToolItem(tb, SWT.PUSH);
            upBtn.setText(Messages.RouterDependenciesPanel_upBtn);
            upBtn.addSelectionListener(selectionListener);
            upBtn.setEnabled(false);

            downBtn = new ToolItem(tb, SWT.PUSH);
            downBtn.setText(Messages.RouterDependenciesPanel_downBtn);
            downBtn.addSelectionListener(selectionListener);
            downBtn.setEnabled(false);
        }
    }

    private class DependenciesTableLabelProvider extends SearchCellLabelProvider {

        public DependenciesTableLabelProvider(final StructuredViewer structuredViewer) {
            super(structuredViewer);
        }

        @Override
        public Image getImage(Object element) {
            switch (((ManifestItem) element).getHeader()) {
            case ManifestItem.IMPORT_PACKAGE:
                return CamelDesignerPlugin.getImage(((ManifestItem) element).isOptional()
                    ? CamelDesignerPlugin.IMPORT_PACKAGE_OVERLAY_ICON
                    : CamelDesignerPlugin.IMPORT_PKG_ICON);
            case ManifestItem.REQUIRE_BUNDLE:
                return CamelDesignerPlugin.getImage(((ManifestItem) element).isOptional()
                    ? CamelDesignerPlugin.REQUIRE_BUNDLE_OVERLAY_ICON
                    : CamelDesignerPlugin.REQUIRE_BD_ICON);
            case ManifestItem.BUNDLE_CLASSPATH:
                return CamelDesignerPlugin.getImage(CamelDesignerPlugin.BUNDLE_CP_ICON);
            case ManifestItem.EXPORT_PACKAGE:
                return CamelDesignerPlugin.getImage(CamelDesignerPlugin.IMPORT_PKG_ICON);
            default:
                return null;
            }
        }

        @Override
        protected boolean isBuiltIn(Object element) {
            return ((ManifestItem) element).isBuiltIn();
        }

    }

}
