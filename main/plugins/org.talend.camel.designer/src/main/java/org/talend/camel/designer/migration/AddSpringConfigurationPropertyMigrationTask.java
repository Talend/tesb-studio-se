package org.talend.camel.designer.migration;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.tree.DefaultElement;
import org.talend.camel.core.model.camelProperties.CamelProcessItem;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;

public class AddSpringConfigurationPropertyMigrationTask extends AbstractRouteItemMigrationTask {

    private static final String IGNORE_EXCHANGE_EVENTS = "ignoreExchangeEvents";

    private static final String SPRING_BEANS_NAMESPACE = "http://www.springframework.org/schema/beans";

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2014, 11, 18, 12, 00, 00);
        return gc.getTime();
    }

    @Override
    protected ExecutionResult execute(CamelProcessItem item) {
        try {
            addIgnoreExchangeEventProperty(item);
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    private void addIgnoreExchangeEventProperty(CamelProcessItem item) throws PersistenceException, DocumentException {
        String springContent = item.getSpringContent();
        if (null != springContent && !springContent.isEmpty()) {
            Document document = DocumentHelper.parseText(springContent);
            QName qname = QName.get("bean", SPRING_BEANS_NAMESPACE);
            List<Element> beans = document.getRootElement().elements(qname);
            for (Element bean : beans) {
                if ("jmxEventNotifier".equals(bean.attributeValue("id"))
                        && "org.apache.camel.management.JmxNotificationEventNotifier".equals(bean.attributeValue("class"))) {
                    List<Element> properties = bean.elements(QName.get("property", SPRING_BEANS_NAMESPACE));
                    boolean hasIgnore = false;
                    for (Element property : properties) {
                        List<Attribute> propertyAttributes = property.attributes();
                        for (Attribute propertyAttribute : propertyAttributes) {
                            if (propertyAttribute.getValue().equals(IGNORE_EXCHANGE_EVENTS)) {
                                hasIgnore = true;
                                break;
                            }
                        }
                    }
                    if (!hasIgnore) {
                        DefaultElement ignoreExchangeElement = new DefaultElement("property", bean.getNamespace());
                        ignoreExchangeElement
                                .add(DocumentHelper.createAttribute(ignoreExchangeElement, "name", IGNORE_EXCHANGE_EVENTS));
                        ignoreExchangeElement.add(DocumentHelper.createAttribute(ignoreExchangeElement, "value", "true"));
                        bean.add(ignoreExchangeElement);
                        item.setSpringContent(document.asXML());
                        saveItem(item);
                    }
                    break;
                }
            }
        }
    }
}
