package org.talend.repository.services.utils;

import org.talend.core.GlobalServiceRegister;
import org.talend.core.IESBService;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.processor.RepositoryTypeProcessor;

public class ServiceOperationRepositoryTypeProcessor extends RepositoryTypeProcessor {

    public ServiceOperationRepositoryTypeProcessor(String repositoryType) {
        super(repositoryType);
    }

    @Override
    protected ERepositoryObjectType getType() {
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IESBService.class)) {
            IESBService service = (IESBService) GlobalServiceRegister.getDefault().getService(IESBService.class);
            return service.getServicesType();
        }
        return super.getType();
    }

    @Override
    public boolean isSelectionValid(RepositoryNode node) {
        if (node == null) {
            return false;
        }
        IRepositoryViewObject object = node.getObject();
        if (object == null) {
            return super.isSelectionValid(node);
        }
        if (object instanceof OperationRepositoryObject) {
            return true;
        }
        return false;
    }
}
