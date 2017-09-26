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
package org.talend.camel.designer.generator;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.fieldassist.DecoratedField;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IControlCreator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.camel.designer.dialog.RouteResourceSelectionDialog;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.properties.Item;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.camel.resource.core.model.ResourceDependencyModel;
import org.talend.designer.camel.resource.core.util.RouteResourceUtil;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController;
import org.talend.designer.core.ui.editor.properties.controllers.creator.SelectAllTextControlCreator;
import org.talend.designer.runprocess.ProcessorUtilities;

/**
 * @author Xiaopeng Li
 * 
 */
public class RouteResourceController extends AbstractElementPropertySectionController {

    private Text labelText;

    IControlCreator cbCtrl = new IControlCreator() {

        public Control createControl(final Composite parent, final int style) {
            CCombo cb = new CCombo(parent, style);
            return cb;
        }
    };

    SelectionListener listenerSelection = new SelectionListener() {

        public void widgetDefaultSelected(SelectionEvent e) {
            // do nothing.
        }

        public void widgetSelected(SelectionEvent selectionEvent) {
            final PropertyChangeCommand cmd;
            if (selectionEvent.getSource() instanceof Button) {
                cmd = createButtonCommand((Button) selectionEvent.getSource());
            } else if (selectionEvent.getSource() instanceof CCombo) {
                cmd = createComboCommand((CCombo) selectionEvent.getSource());
            } else {
                cmd = null;
            }
            if (cmd != null) {
                cmd.setUpdate(true);
                executeCommand(cmd);
            }
        }
    };

    public RouteResourceController(IDynamicProperty dp) {
        super(dp);
    }

    /**
     * 
     * @param source
     * @return
     */
    private PropertyChangeCommand createButtonCommand(Button button) {
        RouteResourceSelectionDialog dialog = new RouteResourceSelectionDialog(button.getShell());

        selectNodeIfExists(button, dialog);

        if (dialog.open() == Window.OK) {

            IRepositoryViewObject repositoryObject = dialog.getResult().getObject();

            // refreshItemeProperty(repositoryObject);

            final Item item = repositoryObject.getProperty().getItem();
            String id = item.getProperty().getId();
            String paramName = (String) button.getData(PARAMETER_NAME);

            return new PropertyChangeCommand(elem, paramName, id);
        }
        return null;
    }

    /**
     * DOC nrousseau Comment method "createComboCommand".
     * 
     * @param source
     * @return
     */
    private PropertyChangeCommand createComboCommand(CCombo combo) {
        String paramName = (String) combo.getData(PARAMETER_NAME);

        IElementParameter param = elem.getElementParameter(paramName);

        String value = combo.getText();

//        for (int j = 0; j < param.getListItemsValue().length; j++) {
//            if (combo.getText().equals(param.getListItemsDisplayName()[j])) {
//                value = (String) param.getListItemsValue()[j];
//            }
//        }
        if (value.equals(param.getValue())) {
            return null;
        }

        return new PropertyChangeCommand(elem, paramName, value);
    }

    @Override
    public Control createControl(final Composite subComposite, final IElementParameter param, final int numInRow,
            final int nbInRow, final int top, final Control lastControl) {
        this.curParameter = param;
        this.paramFieldType = param.getFieldType();
        FormData data;

        final DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER | SWT.READ_ONLY,
                new SelectAllTextControlCreator());
        if (param.isRequired()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                    FieldDecorationRegistry.DEC_REQUIRED);
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.TOP, false);
        }
        Control cLayout = dField.getLayoutControl();

        labelText = (Text) dField.getControl();

        labelText.setData(PARAMETER_NAME, param.getName());

        cLayout.setBackground(subComposite.getBackground());
        labelText.setEditable(false);
        if (elem instanceof Node) {
            labelText.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }

        addDragAndDropTarget(labelText);

        CLabel labelLabel = getWidgetFactory().createCLabel(subComposite, param.getDisplayName());
        data = new FormData();
        if (lastControl != null) {
            data.left = new FormAttachment(lastControl, 0);
        } else {
            data.left = new FormAttachment(((numInRow - 1) * MAX_PERCENT) / (nbInRow + 1), 0);
        }
        data.top = new FormAttachment(0, top);
        labelLabel.setLayoutData(data);
        if (numInRow != 1) {
            labelLabel.setAlignment(SWT.RIGHT);
        }

        data = new FormData();
        int currentLabelWidth = STANDARD_LABEL_WIDTH;
        GC gc = new GC(labelLabel);
        Point labelSize = gc.stringExtent(param.getDisplayName());
        gc.dispose();
        if ((labelSize.x + ITabbedPropertyConstants.HSPACE) > currentLabelWidth) {
            currentLabelWidth = labelSize.x + ITabbedPropertyConstants.HSPACE;
        }

        if (numInRow == 1) {
            if (lastControl != null) {
                data.left = new FormAttachment(lastControl, currentLabelWidth);
            } else {
                data.left = new FormAttachment(0, currentLabelWidth);
            }

        } else {
            data.left = new FormAttachment(labelLabel, 0, SWT.RIGHT);
        }
        data.right = new FormAttachment((numInRow * MAX_PERCENT) / (nbInRow + 1), 0);
        data.top = new FormAttachment(0, top);
        cLayout.setLayoutData(data);

        Button btn;
        Point btnSize;

        btn = getWidgetFactory().createButton(subComposite, "", SWT.PUSH); //$NON-NLS-1$
        btnSize = btn.computeSize(SWT.DEFAULT, SWT.DEFAULT);

        btn.setImage(ImageProvider.getImage(CoreUIPlugin.getImageDescriptor(DOTS_BUTTON)));

        btn.addSelectionListener(listenerSelection);
        btn.setData(PARAMETER_NAME, param.getName());
        btn.setEnabled(!param.isReadOnly());
        data = new FormData();
        data.left = new FormAttachment(cLayout, 0);
        data.right = new FormAttachment(cLayout, STANDARD_BUTTON_WIDTH, SWT.RIGHT);
        data.top = new FormAttachment(0, top);
        data.height = STANDARD_HEIGHT - 2;
        btn.setLayoutData(data);

        hashCurControls.put(param.getName(), labelText);
        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        Control lastControlUsed = btn;
        lastControlUsed = addVersionCombo(subComposite,
                param.getChildParameters().get(EParameterName.ROUTE_RESOURCE_TYPE_VERSION.getName()), lastControlUsed,
                numInRow + 1, nbInRow, top);
        dynamicProperty.setCurRowSize(Math.max(initialSize.y, btnSize.y) + ITabbedPropertyConstants.VSPACE);
        return btn;
    }

    /**
     * 
     * @param subComposite
     * @param param
     * @param lastControl
     * @param numInRow
     * @param nbInRow
     * @param top
     * @return
     */
    private Control addVersionCombo(Composite subComposite, IElementParameter param, Control lastControl, int numInRow,
            int nbInRow, int top) {
        DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, cbCtrl);
        if (param.isRequired()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                    FieldDecorationRegistry.DEC_REQUIRED);
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.TOP, false);
        }
        if (param.isRepositoryValueUsed()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                    FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
            decoration.setDescription(""); //$NON-NLS-1$
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.BOTTOM, false);
        }

        Control cLayout = dField.getLayoutControl();
        CCombo combo = (CCombo) dField.getControl();
        FormData data;
        combo.setItems(getListToDisplay(param));
        combo.setEditable(false);
        cLayout.setBackground(subComposite.getBackground());
        combo.setEnabled(!param.isReadOnly());
        combo.addSelectionListener(listenerSelection);
        combo.setData(PARAMETER_NAME, param.getName());
        if (elem instanceof Node) {
            combo.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }

        CLabel labelLabel = getWidgetFactory().createCLabel(subComposite, param.getDisplayName());
        data = new FormData();
        if (lastControl != null) {
            data.left = new FormAttachment(lastControl, 0);
        } else {
            data.left = new FormAttachment(((numInRow - 1) * MAX_PERCENT) / nbInRow, 0);
        }

        data.top = new FormAttachment(0, top);
        labelLabel.setLayoutData(data);
        if (numInRow != 1) {
            labelLabel.setAlignment(SWT.RIGHT);
        }
        // *********************
        data = new FormData();
        int currentLabelWidth = STANDARD_LABEL_WIDTH;
        GC gc = new GC(labelLabel);
        Point labelSize = gc.stringExtent(param.getDisplayName());
        gc.dispose();

        if ((labelSize.x + ITabbedPropertyConstants.HSPACE) > currentLabelWidth) {
            currentLabelWidth = labelSize.x + ITabbedPropertyConstants.HSPACE;
        }

        if (numInRow == 1) {
            if (lastControl != null) {
                data.left = new FormAttachment(lastControl, currentLabelWidth);
            } else {
                data.left = new FormAttachment(0, currentLabelWidth);
            }

        } else {
            data.left = new FormAttachment(labelLabel, 0, SWT.RIGHT);
        }
        data.top = new FormAttachment(0, top);
        cLayout.setLayoutData(data);
        // **********************
        hashCurControls.put(param.getName(), combo);

        return cLayout;
    }

    private String[] getListToDisplay(IElementParameter param) {
        String[] originalList = param.getListItemsDisplayName();
        List<String> stringToDisplay = new ArrayList<String>();
        String[] itemsShowIf = param.getListItemsShowIf();
        if (itemsShowIf != null) {
            String[] itemsNotShowIf = param.getListItemsNotShowIf();
            for (int i = 0; i < originalList.length; i++) {
                if (param.isShow(itemsShowIf[i], itemsNotShowIf[i], elem.getElementParameters())) {
                    stringToDisplay.add(originalList[i]);
                }
            }
        } else {
            for (String element : originalList) {
                stringToDisplay.add(element);
            }
        }
        return stringToDisplay.toArray(new String[0]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.ui.editor.properties.controllers.
     * AbstractElementPropertySectionController#estimateRowSize (org.eclipse.swt.widgets.Composite,
     * org.talend.core.model.process.IElementParameter)
     */
    @Override
    public int estimateRowSize(Composite subComposite, IElementParameter param) {
        final DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, new IControlCreator() {

            public Control createControl(Composite parent, int style) {
                return getWidgetFactory().createButton(parent, EParameterName.ROUTE_RESOURCE_TYPE.getDisplayName(), SWT.None);
            }

        });
        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dField.getLayoutControl().dispose();

        return initialSize.y + ITabbedPropertyConstants.VSPACE;
    }

    public void propertyChange(PropertyChangeEvent arg0) {
    }

    @Override
    public void refresh(final IElementParameter param, boolean check) {
        updateContextList(param);
        if (hashCurControls == null) {
            return;
        }
        final String resourceId = (String) param.getValue();
        if (resourceId == null) {
            labelText.setText("");
        } else {
            final IElementParameter versionParameter =
                param.getChildParameters().get(EParameterName.ROUTE_RESOURCE_TYPE_VERSION.getName());
            final String version = (String) versionParameter.getValue();
            final ResourceDependencyModel rdModel = RouteResourceUtil.createDependency(resourceId, version);
            if (rdModel == null) {
                labelText.setText("");
            } else {
                labelText.setText(rdModel.getClassPathUrl());
                // version
                refreshCombo(versionParameter);
            }
        }

        if (elem != null && elem instanceof Node) {
            ((Node) elem).checkAndRefreshNode();
        }
    }

    /**
	 * 
	 * 
	 */
    private void refreshCombo(IElementParameter childParameter) {
        if (childParameter == null) {
            return;
        }
        CCombo combo = (CCombo) hashCurControls.get(childParameter.getName());

        if (combo == null || combo.isDisposed()) {
            return;
        }
        Object value = childParameter.getValue();
        if (value instanceof String) {
            String version = (String) value;
//            String strValue = ""; //$NON-NLS-1$
//            int nbInList = 0, nbMax = childParameter.getListItemsValue().length;
//            while (strValue.equals(new String("")) && nbInList < nbMax) { //$NON-NLS-1$
//                if (name.equals(childParameter.getListItemsValue()[nbInList])) {
//                    strValue = childParameter.getListItemsDisplayName()[nbInList];
//                }
//                nbInList++;
//            }
            String[] paramItems = getListToDisplay(childParameter);
            String[] comboItems = combo.getItems();

            if (!Arrays.equals(paramItems, comboItems)) {
                combo.setItems(paramItems);
            }
            combo.setText(version);
//            combo.setVisible(true);
        }

    }

    /**
     * see feature 0003664: tRunJob: When opening the tree dialog to select the job target, it could be useful to open
     * it on previous selected job if exists.
     * 
     * @param button
     * @param dialog
     */
    private void selectNodeIfExists(Button button, RouteResourceSelectionDialog dialog) {
        try {
            if (elem != null && elem instanceof Node) {
                Node runJobNode = (Node) elem;
                String paramName = (String) button.getData(PARAMETER_NAME);
                String jobId = (String) runJobNode.getPropertyValue(paramName); // .getElementParameter(name).getValue();
                dialog.setSelectedNodeId(jobId);
            }
        } catch (Throwable e) {
            ExceptionHandler.process(e);
        }
    }

    /**
     * 
     * 
     * @param processParam
     */
    private void updateContextList(IElementParameter processParam) {
        if (processParam == null || processParam.getFieldType() != EParameterFieldType.ROUTE_RESOURCE_TYPE) {
            return;
        }
        final String resourceId = (String) processParam.getValue();
        if(resourceId == null){
        	return;
        }

        // for version type
        final List<String> versions = new ArrayList<String>();
        versions.add(RelationshipItemBuilder.LATEST_VERSION);

//        StringBuffer labels = new StringBuffer("");
        List<IRepositoryViewObject> allVersion = new ArrayList<IRepositoryViewObject>();
        if (StringUtils.isNotEmpty(resourceId)) {
            allVersion = ProcessorUtilities.getAllVersionObjectById(resourceId);

            // IRepositoryObject lastVersionObject = null;
//            Item item = null;
//            String label = null;
            if (allVersion != null) {
                String oldVersion = null;
                for (IRepositoryViewObject obj : allVersion) {
                    String version = obj.getVersion();
                    if (oldVersion == null) {
                        oldVersion = version;
                    }
//                    if (VersionUtils.compareTo(version, oldVersion) >= 0) {
//                        item = obj.getProperty().getItem();
//                        // lastVersionObject = obj;
//                    }
                    oldVersion = version;
                    versions.add(version);
                }
//                label = item.getProperty().getLabel();
//                labels.append(label);
                // IPath path =
                // RepositoryNodeUtilities.getPath(lastVersionObject);
                // if (path != null) {
                // label = path.toString() + IPath.SEPARATOR + label;
                // }
            } else {
                elem.setPropertyValue(processParam.getName(), ""); //$NON-NLS-1$
            }
        }
//        jobNameParam.setLabelFromRepository(labels.toString());

        setProcessTypeRelatedValues(processParam.getChildParameters().get(EParameterName.ROUTE_RESOURCE_TYPE_VERSION.getName()),
            versions.toArray(new String[versions.size()])/*,
            RelationshipItemBuilder.LATEST_VERSION*/);

    }

    private void setProcessTypeRelatedValues(IElementParameter childParam, String[] valueList/*, final String defaultValue*/) {
        if (childParam == null) {
            return;
        }
        childParam.setListItemsDisplayName(valueList);
        childParam.setListItemsValue(valueList);

        if (elem != null) {
//            if (valueList != null && Arrays.!valueList.contains(childParam.getValue())) {
//                if (nameList != null && nameList.size() > 0) {
//                    // set default value
//                    if (defaultValue != null) {
//                        childParam.setValue(defaultValue);
//                    } else {
//                        elem.setPropertyValue(fullChildName, valueList.get(valueList.size() - 1));
//                    }
//                }
//            } else {
                elem.setPropertyValue(childParam.getName(), childParam.getValue());
//            }
        }
    }
}
