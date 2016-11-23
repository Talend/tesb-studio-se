package org.talend.designer.esb.webservice.ui;

import java.util.EventListener;

import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.talend.commons.ui.swt.advanced.dataeditor.AbstractDataTableEditorView;
import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreatorColumn;
import org.talend.commons.utils.data.bean.IBeanPropertyAccessors;

public abstract class AbstractTableViewPart<T extends EventListener, U> extends AbstractWebServiceUIPart<T> {

    protected DataTableEditorView tableView;

    public AbstractTableViewPart(T eventListener) {
        super(eventListener);
    }

    abstract String getLabelKey();

    abstract ExtendedTableModel<U> getTableModel();

    abstract String getItemLabel(U item);

    abstract void itemSelected(U item);

    @Override
    @SuppressWarnings("unchecked")
    final Control createControl(Composite parent) {
        tableView = createTableView(parent, getLabelKey(), getTableModel());
        final Table table = tableView.getTable();
        table.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                TableItem[] item = table.getSelection();
                U currentPortName = (U) item[0].getData();
                itemSelected(currentPortName);
            }
        });
        return table;
    }

    protected class DataTableEditorView extends AbstractDataTableEditorView<U> {

        private IBeanPropertyAccessors<U, String> accessors;

        public DataTableEditorView(Composite parent, ExtendedTableModel<U> model, IBeanPropertyAccessors<U, String> accessors) {
            super(parent, SWT.NONE, model, false, true, false, false);
            this.accessors = accessors;
            initGraphicComponents();
            GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
            gridData.minimumHeight = 150;
            this.getMainComposite().setLayoutData(gridData);
            GridLayout layout = (GridLayout) this.getMainComposite().getLayout();
            layout.marginWidth = 0;
            layout.marginHeight = 0;
        }

        @Override
        protected void setTableViewerCreatorOptions(TableViewerCreator<U> newTableViewerCreator) {
            super.setTableViewerCreatorOptions(newTableViewerCreator);
            newTableViewerCreator.setHeaderVisible(false);
            newTableViewerCreator.setVerticalScroll(true);
            newTableViewerCreator.setReadOnly(true);
        }

        @Override
        protected void createColumns(TableViewerCreator<U> tableViewerCreator, Table table) {
            TableViewerCreatorColumn<U, String> rowColumn = new TableViewerCreatorColumn<>(tableViewerCreator);
            rowColumn.setBeanPropertyAccessors(accessors);
            rowColumn.setWeight(60);
            rowColumn.setModifiable(true);
            rowColumn.setMinimumWidth(60);
            rowColumn.setCellEditor(new TextCellEditor(tableViewerCreator.getTable()));
        }
    };

    /**
     * Creates the table view with giving i18n titleKey, paramList, and accessor to String. The parent Composite need to
     * be grid layout.
     */
    private DataTableEditorView createTableView(Composite parent, String titleKey, ExtendedTableModel<U> model) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(Messages.getString(titleKey));
        label.setLayoutData(new GridData(SWT.NONE, SWT.TOP, false, false));

        DataTableEditorView view = new DataTableEditorView(parent, model, createAccessor());

        int horizontalSpan = ((GridLayout) parent.getLayout()).numColumns - 1;
        view.getMainComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, horizontalSpan, 1));
        return view;
    }

    private IBeanPropertyAccessors<U, String> createAccessor() {
        return new IBeanPropertyAccessors<U, String>() {

            @Override
            public String get(U bean) {
                return getItemLabel(bean);
            }

            @Override
            public void set(U bean, String value) {
            }

        };
    }

    protected boolean selectFirstElement() {
        int itemCount = tableView.getTable().getItemCount();
        if (itemCount < 1) {
            return false;
        }
        tableView.getTable().select(0);
        return true;
    }
}
