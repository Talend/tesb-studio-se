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
package org.talend.designer.esb.runcontainer.ui.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.talend.designer.esb.runcontainer.i18n.RunContainerMessages;
import org.talend.designer.esb.runcontainer.ui.dialog.RuntimeInfoDialog;
import org.talend.designer.esb.runcontainer.util.JMXUtil;

public class OpenRuntimeInfoAction extends Action {

    public OpenRuntimeInfoAction() {
        setToolTipText(RunContainerMessages.getString("OpenRuntimeInfoAction.Tip")); //$NON-NLS-1$
        setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {

        MBeanServerConnection mbsc;
        try {
            mbsc = JMXUtil.createJMXconnection();
            List<Map<String, String>> list = new ArrayList<Map<String, String>>();
            String JOB_MBEAN = "TalendAgent:type=O.S. Informations"; //$NON-NLS-1$
            ObjectName objectJob = new ObjectName(JOB_MBEAN);
            MBeanInfo info = mbsc.getMBeanInfo(objectJob);
            MBeanAttributeInfo[] attrInfo = info.getAttributes();

            for (int i = 0; i < attrInfo.length; i++) {
                try {
                    Map<String, String> attributeMap = new HashMap<String, String>();
                    String attributeName = attrInfo[i].getName();
                    attributeMap.put("name", attributeName); //$NON-NLS-1$
                    String attributeDesc = attrInfo[i].getType();
                    attributeMap.put("desc", attributeDesc); //$NON-NLS-1$
                    String attributeValue = mbsc.getAttribute(objectJob, attributeName).toString();
                    attributeMap.put(attributeName, attributeValue);
                    attributeMap.put("value", attributeValue); //$NON-NLS-1$
                    list.add(attributeMap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            RuntimeInfoDialog dlg = new RuntimeInfoDialog(list);
            dlg.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
