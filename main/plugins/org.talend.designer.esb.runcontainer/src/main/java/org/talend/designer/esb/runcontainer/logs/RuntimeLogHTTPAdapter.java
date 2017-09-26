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
package org.talend.designer.esb.runcontainer.logs;

public class RuntimeLogHTTPAdapter implements IRuntimeLogListener {

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.esb.runcontainer.logs.IRuntimeLogListener#addListenerByBundleId(int)
     */
    @Override
    public void addListenerByBundleId(int bundleId) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.designer.esb.runcontainer.logs.IRuntimeLogListener#logReceived(org.talend.designer.esb.runcontainer
     * .logs.FelixLogsModel)
     */
    @Override
    public void logReceived(FelixLogsModel logsModel) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.esb.runcontainer.logs.IRuntimeLogListener#logReceived(java.lang.String)
     */
    @Override
    public void logReceived(String logs, boolean isError) {
    }
}
