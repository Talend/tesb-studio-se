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
package org.talend.repository.services.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SearchPattern;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.properties.XmlFileConnectionItem;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.repository.i18n.Messages;

/**
 * DOC talend2 class global comment. Detailled comment
 */
public class XmlTableForm extends Composite {

    private ICompleteListener completeListener;

    private Text nameFilter;

    private Table table;

    private Button selectAllTablesButton;

    private Button selectNoneTablesButton;

    private static class Item {
        private final XmlFileConnectionItem obj;
        private String label;
        private boolean check;

        public Item(XmlFileConnectionItem obj) {
            this.obj = obj;
        }

        public XmlFileConnectionItem getObj() {
            return obj;
        }

        public String getLabel() {
            if (null == label) {
                Set<MetadataTable> tables = ConnectionHelper.getTables(obj.getConnection());
                if (!tables.isEmpty()) {
                    label = obj.getProperty().getLabel() + '-' + tables.iterator().next().getLabel();
                }
            }
            return label;
        }

        public boolean isCheck() {
            return check;
        }

        public void setCheck(boolean check) {
            this.check = check;
        }

    }

    private final List<Item> items;


    /**
     * DOC Administrator FileTableForm constructor comment.
     * 
     * @param parent
     * @param style
     */
    public XmlTableForm(Composite parent, Collection<XmlFileConnectionItem> fileRepObjList) {
        super(parent, SWT.NONE);
        items = new ArrayList<Item>(fileRepObjList.size());
        for (XmlFileConnectionItem obj : fileRepObjList) {
            items.add(new Item(obj));
        }

        setLayout(new GridLayout());

        // Group Table Settings
        Group groupTableSettings = new Group(this, SWT.NONE);
        groupTableSettings.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        groupTableSettings.setText("Select Schema to rewrite");
        groupTableSettings.setLayout(new GridLayout(2, false));

        new Label(groupTableSettings, SWT.NONE).setText(Messages.getString("SelectorTableForm.nameFilter")); //$NON-NLS-1$
        nameFilter = new Text(groupTableSettings, SWT.BORDER);
        nameFilter.setToolTipText(Messages.getString("SelectorTableForm.enterType")); //$NON-NLS-1$
        nameFilter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // List Table
        table = new Table(groupTableSettings, SWT.BORDER | SWT.CHECK | SWT.MULTI | SWT.VIRTUAL); 
        table.addListener(SWT.SetData, new Listener() {
            public void handleEvent(Event event) { 
                TableItem tableItem = (TableItem)event.item;
                @SuppressWarnings("unchecked")
                List<Item> items = (List<Item>) table.getData();
                Item item = items.get(event.index);
                tableItem.setText(item.getLabel());
                tableItem.setImage(ImageProvider.getImage(ECoreImage.METADATA_FILE_XML_ICON));
                tableItem.setChecked(item.isCheck());
                tableItem.setData(item);
            } 
        });
        TableColumn tableName = new TableColumn(table, SWT.NONE);
        tableName.setWidth(300);
        GridData tableGridData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
        tableGridData.heightHint = table.getItemHeight() * 10;
        table.setLayoutData(tableGridData);

        // Composite retreiveSchema
        Composite compositeRetreiveSchemaButton = new Composite(groupTableSettings, SWT.NONE);
        compositeRetreiveSchemaButton.setLayoutData(new GridData(SWT.CENTER, SWT.NONE, false, false, 2, 1));
        compositeRetreiveSchemaButton.setLayout(new GridLayout(2, true));

        // Button Create Table
        selectAllTablesButton = new Button(compositeRetreiveSchemaButton, SWT.PUSH);
        selectAllTablesButton.setText(Messages.getString("SelectorTableForm.selectAllTables")); //$NON-NLS-1$

        selectNoneTablesButton = new Button(compositeRetreiveSchemaButton, SWT.PUSH);
        selectNoneTablesButton.setText(Messages.getString("SelectorTableForm.selectNoneTables")); //$NON-NLS-1$

        addFieldsListeners();

        reftesTableContent(items);
    }

    private void reftesTableContent(List<Item> items) {
        table.clearAll();
        table.setData(items);
        table.setItemCount(items.size());
    }

    private void addFieldsListeners() {
        nameFilter.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                String pattern = nameFilter.getText();
                SearchPattern matcher = new SearchPattern();
                matcher.setPattern(pattern);

                final List<Item> newList = new ArrayList<Item>();
                for (Item item : items) {
                    if (matcher.matches(item.getLabel())) {
                        newList.add(item);
                    }
                }
                reftesTableContent(newList);
                completeListener.setComplete(!getSelectionItems().isEmpty());
            }

        });

        // Event checkBox action
        table.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                if (e.detail == SWT.CHECK) {
                    ((Item) e.item.getData()).setCheck(((TableItem) e.item).getChecked());
                    if (null != completeListener) {
                        completeListener.setComplete(!getSelectionItems().isEmpty());
                    }
                }
            }
        });
        table.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                table.getColumns()[0].setWidth(table.getClientArea().width - 2*table.getBorderWidth());
            }
        });

        selectAllTablesButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                setChecked(true);
            }
        });

        selectNoneTablesButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                setChecked(false);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void setChecked(boolean checked) {
        for (TableItem tableItem : table.getItems()) {
            // update only rendered items
            if (null != tableItem.getData()) {
                tableItem.setChecked(checked);
            }
        }
        for (Item item : (List<Item>) table.getData()) {
            item.setCheck(checked);
        }
        if (null != completeListener) {
            completeListener.setComplete(checked);
        }
    }

    public Collection<XmlFileConnectionItem> getSelectionItems() {
        final Collection<XmlFileConnectionItem> itemMap = new ArrayList<XmlFileConnectionItem>();
        for (TableItem tableItem : table.getItems()) {
            if (tableItem.getChecked()) {
                itemMap.add(((Item) tableItem.getData()).getObj());
            }
        }
        return itemMap;
    }

    interface ICompleteListener {
        void setComplete(boolean complete);
    }

    public void setListener(ICompleteListener completeListener) {
        this.completeListener = completeListener;
    }

}
