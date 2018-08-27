package org.talend.camel.designer.migration;

import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.core.runtime.Path;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.talend.camel.core.model.camelProperties.RouteResourceItem;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.resources.ResourceItem;
import org.talend.core.model.resources.ResourcesFactory;
import org.talend.core.repository.model.ProxyRepositoryFactory;

public class ChangeRouteResourceItemToResourceItemTask extends AbstractItemMigrationTask {

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2018, 8, 23, 0, 0, 0);
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(Item item) {
        if (item instanceof RouteResourceItem) {
            RouteResourceItem routeResitem = (RouteResourceItem) item;

            Property property = EcoreUtil.copy(routeResitem.getProperty());
            ResourceItem resItem = ResourcesFactory.eINSTANCE.createResourceItem();

            property.setItem(resItem);
            resItem.setProperty(property);
            resItem.setName(routeResitem.getName());
            resItem.setBindingExtension(routeResitem.getBindingExtension());
            resItem.setContent(routeResitem.getContent());
            for (Object refResource : routeResitem.getReferenceResources()) {
                resItem.getReferenceResources().add(refResource);
            }
            ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
            try {
                IRepositoryViewObject object = factory.getSpecificVersion(item.getProperty().getId(),
                        item.getProperty().getVersion(), true);
                factory.deleteObjectPhysical(object);
                factory.create(resItem, new Path(item.getState().getPath()), false);
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }

            // TODO DELETE
            return ExecutionResult.SUCCESS_NO_ALERT;

        } else {
            return ExecutionResult.NOTHING_TO_DO;
        }
    }

}
