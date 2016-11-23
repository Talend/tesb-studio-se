package org.talend.designer.esb.components.rs.provider.generator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.swt.events.SelectionEvent;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.process.INode;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.ui.editor.cmd.ChangeMetadataCommand;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.properties.controllers.ComboController;

public class RestResponseSchemaController extends ComboController {

    public RestResponseSchemaController(IDynamicProperty dp) {
        super(dp);
    }

    @Override
    public Command createComboCommand(SelectionEvent event) {
        // Change the body type according to selected return body type
        Command changePropertyCommand = super.createComboCommand(event);
        Object newReturnType = null;
        if (changePropertyCommand != null) {
            newReturnType = ((PropertyChangeCommand) changePropertyCommand).getNewValue();
        }
        if (newReturnType == null) {
            return null;
        }

        // get old metadata column
        List<IMetadataTable> metadataList = ((INode) elem).getMetadataList();
        IMetadataTable oldMetadataTable = null;
        if (metadataList != null && metadataList.size() > 0) {
            oldMetadataTable = metadataList.get(0);
        } else {
            metadataList = new ArrayList<>();
            ((INode) elem).setMetadataList(metadataList);
        }

        // create new metadata column
        IMetadataTable newMetadataTable = oldMetadataTable == null ? new MetadataTable() : oldMetadataTable.clone();
        List<IMetadataColumn> listColumns = newMetadataTable.getListColumns();
        if (listColumns == null) {
            listColumns = new ArrayList<>();
            newMetadataTable.setListColumns(listColumns);
        }
        IMetadataColumn bodyColumn = listColumns.size() > 0 ? listColumns.get(0) : new MetadataColumn();
        bodyColumn.setId("body");
        bodyColumn.setTalendType(newReturnType.toString());
        listColumns.clear();
        listColumns.add(bodyColumn);
        metadataList.clear();
        metadataList.add(newMetadataTable);

        // construct change metadata command
        ChangeMetadataCommand changeMetadataCommand = new ChangeMetadataCommand((INode) elem, null, oldMetadataTable,
                newMetadataTable);

        // construct compound command by combining above 2 commands
        CompoundCommand compoundCommand = new CompoundCommand();
        compoundCommand.add(changePropertyCommand);
        compoundCommand.add(changeMetadataCommand);

        return compoundCommand;
    }

}
