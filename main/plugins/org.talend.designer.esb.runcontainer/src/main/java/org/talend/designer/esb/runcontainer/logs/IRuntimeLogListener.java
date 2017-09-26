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

/**
 * Runtime log output listener
 */
public interface IRuntimeLogListener {

    public abstract void addListenerByBundleId(int bundleId);

    public abstract void logReceived(FelixLogsModel logsModel);

    public abstract void logReceived(String logs, boolean isError);

}