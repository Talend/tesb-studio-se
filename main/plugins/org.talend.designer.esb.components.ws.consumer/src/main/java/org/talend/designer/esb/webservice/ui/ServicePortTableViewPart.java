package org.talend.designer.esb.webservice.ui;

import java.util.EventListener;

import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;

interface ServicePortSelectionListener extends EventListener {

    void portSelected(String port);

    ExtendedTableModel<String> getPortTableModel();
}

public class ServicePortTableViewPart extends AbstractTableViewPart<ServicePortSelectionListener, String> {

    public ServicePortTableViewPart(ServicePortSelectionListener eventListener) {
        super(eventListener);
    }

    @Override
    String getLabelKey() {
        return "WebServiceUI.Port";
    }

    @Override
    String getItemLabel(String item) {
        return item;
    }

    @Override
    void itemSelected(String item) {
        listener.portSelected(item);
    }

    @Override
    ExtendedTableModel<String> getTableModel() {
        return listener.getPortTableModel();
    }
}
