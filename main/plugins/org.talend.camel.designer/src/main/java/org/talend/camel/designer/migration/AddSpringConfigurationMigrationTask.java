package org.talend.camel.designer.migration;

import java.util.Date;
import java.util.GregorianCalendar;

import org.talend.camel.core.model.camelProperties.CamelProcessItem;
import org.talend.camel.designer.util.CamelSpringUtil;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;

/**
 * used to set the default spring configuration content when importing an old version route item
 */
public class AddSpringConfigurationMigrationTask extends AbstractRouteItemMigrationTask {

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2012, 11, 17, 14, 00, 00);
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(CamelProcessItem item) {

        try {
            addDefaultSpringContentFor(item);
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }

    }

    private void addDefaultSpringContentFor(CamelProcessItem item) throws PersistenceException {
        String springContent = item.getSpringContent();
        if (springContent == null || "".equals(springContent.trim())) {
            String defaultContent = CamelSpringUtil.getDefaultContent(item);
            item.setSpringContent(defaultContent);
            saveItem(item);
        }
    }
}