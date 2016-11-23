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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class ServiceMetadataCustomPropertiesTable {

    private static final String COLUMN_PROPERTY_NAME_DEFAULT_VALUE = "property name";

    private static final String COLUMN_PROPERTY_VALUE_DEFAULT_VALUE = "property value";

    private static final String[] columnNames = new String[] { "name", "value" }; //$NON-NLS-1$ //$NON-NLS-2$

    private Composite composite;

    private Table table;

    private TableViewer tableViewer;

    private CustomPropertiesList properties = new CustomPropertiesList(new HashMap<String, String>());

    public ServiceMetadataCustomPropertiesTable(Composite parent, Map<String, String> customProperties) {

        composite = new Composite(parent, SWT.NONE);
        setEditable(false);

        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        composite.setLayoutData(gridData);

        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Label label = new Label(composite, SWT.NONE);
        label.setText("Service Locator Custom Properties");
        label.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 2, 1));

        createTable(composite);

        createTableViewer();
        tableViewer.setContentProvider(new PropertiesContentProvider());
        tableViewer.setLabelProvider(new PropertyLabelProvider());

        properties = new CustomPropertiesList(customProperties);
        tableViewer.setInput(properties);

        createButtons(composite);
    }

    List<String> getColumnNames() {
        return Arrays.asList(columnNames);
    }

    public Map<String, String> getPropertiesMap() {
        return properties.getPropertiesMap();
    }

    private void createTable(Composite parent) {
        int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION;

        table = new Table(parent, style);

        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
        gridData.heightHint = 70;
        gridData.minimumHeight = 50;
        table.setLayoutData(gridData);

        table.setLinesVisible(true);
        table.setHeaderVisible(true);

        TableColumn column = new TableColumn(table, SWT.LEFT, 0);
        column.setText("Property Name");
        column.setWidth(150);

        column = new TableColumn(table, SWT.LEFT, 1);
        column.setText("Property Value");
        column.setWidth(150);
    }

    private void createTableViewer() {

        tableViewer = new TableViewer(table);
        tableViewer.setUseHashlookup(true);

        tableViewer.setColumnProperties(columnNames);

        CellEditor[] editors = new CellEditor[columnNames.length];

        TextCellEditor textEditor = new TextCellEditor(table) {

            @Override
            protected Object doGetValue() {
                Object value = super.doGetValue();
                return null == value || ((String) value).trim().isEmpty() ? COLUMN_PROPERTY_NAME_DEFAULT_VALUE : value;
            }
        };
        ((Text) textEditor.getControl()).setTextLimit(64);
        ((Text) textEditor.getControl()).addVerifyListener(new VerifyListener() {

            @Override
            public void verifyText(VerifyEvent e) {
                e.doit = !e.text.equals("\""); //$NON-NLS-1$
            }
        });
        editors[0] = textEditor;

        textEditor = new TextCellEditor(table);
        ((Text) textEditor.getControl()).setTextLimit(64);
        ((Text) textEditor.getControl()).addVerifyListener(new VerifyListener() {

            @Override
            public void verifyText(VerifyEvent e) {
                e.doit = !e.text.equals("\""); //$NON-NLS-1$
            }
        });
        editors[1] = textEditor;

        tableViewer.setCellEditors(editors);
        tableViewer.setCellModifier(new PropertyCellModifier(this));
    }

    private void createButtons(Composite parent) {

        Button add = new Button(parent, SWT.PUSH | SWT.CENTER);
        add.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ADD));
        add.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
        add.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                properties.addProperty();
            }
        });

        Button delete = new Button(parent, SWT.PUSH | SWT.CENTER);
        delete.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_DELETE));
        delete.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
        delete.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                CustomProperty property = (CustomProperty) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();
                if (null != property) {
                    properties.removeProperty(property);
                }
            }
        });
    }

    public void setEditable(boolean editable) {
        nestedSetEnabled(composite, editable);
    }

    private static void nestedSetEnabled(Composite control, boolean enabled) {
        control.setEnabled(enabled);
        for (Control childControl : control.getChildren()) {
            if (childControl instanceof Composite) {
                nestedSetEnabled((Composite) childControl, enabled);
            } else {
                childControl.setEnabled(enabled);
            }
        }
    }

    private interface IPropertiesListViewer {

        void addProperty(CustomProperty property);

        void removeProperty(CustomProperty property);

        void updateProperty(CustomProperty property);

    }

    private static class CustomProperty {

        private String name;

        private String value;

        public CustomProperty(String name, String value) {
            setName(name);
            setValue(value);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    private static class CustomPropertiesList {

        private Vector<CustomProperty> properties = new Vector<>();

        private Set<IPropertiesListViewer> changeListeners = new HashSet<>();

        public CustomPropertiesList(Map<String, String> customProperties) {
            for (Map.Entry<String, String> prop : customProperties.entrySet()) {
                properties.add(properties.size(), new CustomProperty(prop.getKey(), prop.getValue()));
            }
        }

        public Vector<CustomProperty> getProperties() {
            return properties;
        }

        public void addProperty() {
            CustomProperty property = new CustomProperty(COLUMN_PROPERTY_NAME_DEFAULT_VALUE, COLUMN_PROPERTY_VALUE_DEFAULT_VALUE);
            properties.add(properties.size(), property);
            Iterator<IPropertiesListViewer> iterator = changeListeners.iterator();
            while (iterator.hasNext()) {
                iterator.next().addProperty(property);
            }
        }

        public void removeProperty(CustomProperty property) {
            properties.remove(property);
            Iterator<IPropertiesListViewer> iterator = changeListeners.iterator();
            while (iterator.hasNext()) {
                iterator.next().removeProperty(property);
            }
        }

        public void updateProperty(CustomProperty property) {
            Iterator<IPropertiesListViewer> iterator = changeListeners.iterator();
            while (iterator.hasNext()) {
                iterator.next().updateProperty(property);
            }
        }

        public void removeChangeListener(IPropertiesListViewer viewer) {
            changeListeners.remove(viewer);
        }

        public void addChangeListener(IPropertiesListViewer viewer) {
            changeListeners.add(viewer);
        }

        public Map<String, String> getPropertiesMap() {
            Map<String, String> propsMap = new HashMap<>();
            for (CustomProperty prop : properties) {
                propsMap.put(prop.getName(), prop.getValue());
            }
            return propsMap;
        }
    }

    private static class PropertyLabelProvider extends LabelProvider implements ITableLabelProvider {

        @Override
        public String getColumnText(Object element, int columnIndex) {
            CustomProperty task = (CustomProperty) element;
            if (0 == columnIndex) {
                return task.getName();
            } else if (1 == columnIndex) {
                return task.getValue();
            }
            return "";
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
    }

    private static class PropertyCellModifier implements ICellModifier {

        private ServiceMetadataCustomPropertiesTable tableViewer;

        public PropertyCellModifier(ServiceMetadataCustomPropertiesTable tableViewer) {
            super();
            this.tableViewer = tableViewer;
        }

        @Override
        public boolean canModify(Object element, String property) {
            return true;
        }

        @Override
        public Object getValue(Object element, String property) {

            int columnIndex = tableViewer.getColumnNames().indexOf(property);

            CustomProperty customProperty = (CustomProperty) element;

            if (0 == columnIndex) {
                return customProperty.getName();
            } else if (1 == columnIndex) {
                return customProperty.getValue();
            }
            return "";
        }

        @Override
        public void modify(Object element, String property, Object value) {

            int columnIndex = tableViewer.getColumnNames().indexOf(property);

            TableItem item = (TableItem) element;
            CustomProperty customProperty = (CustomProperty) item.getData();

            if (0 == columnIndex) {
                customProperty.setName(((String) value).trim());
            } else if (1 == columnIndex) {
                customProperty.setValue(((String) value).trim());
            }
            tableViewer.properties.updateProperty(customProperty);
        }

    }

    private class PropertiesContentProvider implements IStructuredContentProvider, IPropertiesListViewer {

        @Override
        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
            if (newInput != null) {
                ((CustomPropertiesList) newInput).addChangeListener(this);
            }
            if (oldInput != null) {
                ((CustomPropertiesList) oldInput).removeChangeListener(this);
            }
        }

        @Override
        public void dispose() {
            properties.removeChangeListener(this);
        }

        @Override
        public Object[] getElements(Object parent) {
            return properties.getProperties().toArray();
        }

        @Override
        public void addProperty(CustomProperty property) {
            tableViewer.add(property);
        }

        @Override
        public void removeProperty(CustomProperty property) {
            tableViewer.remove(property);
        }

        @Override
        public void updateProperty(CustomProperty property) {
            tableViewer.update(property, null);
        }
    }

}
