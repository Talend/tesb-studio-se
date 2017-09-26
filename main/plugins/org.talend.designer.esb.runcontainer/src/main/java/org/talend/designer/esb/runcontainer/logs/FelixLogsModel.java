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

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * DOC yyan class global comment. Detailled comment <br/>
 * Runtime Log Model
 */
public class FelixLogsModel {

    private static final String LINE_SPLIT = " | ";

    private int id;

    private long received;

    private String level;

    private int raw_level;

    private String message;

    private String service;

    private String exception;

    private int bundleId;

    private String bundleName;

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getReceived() {
        return this.received;
    }

    public void setReceived(long received) {
        this.received = received;
    }

    public String getLevel() {
        return this.level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public int getRaw_level() {
        return this.raw_level;
    }

    public void setRaw_level(int raw_level) {
        this.raw_level = raw_level;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getService() {
        return this.service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getException() {
        return this.exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public int getBundleId() {
        return this.bundleId;
    }

    public void setBundleId(int bundleId) {
        this.bundleId = bundleId;
    }

    public String getBundleName() {
        return this.bundleName;
    }

    public void setBundleName(String bundleName) {
        this.bundleName = bundleName;
    }

    @Override
    public String toString() {
        Date date = new Date(getReceived());
        String eventlog = new SimpleDateFormat().format(date) + LINE_SPLIT + getLevel() + LINE_SPLIT + getBundleId() + " - "
                + getBundleName() + LINE_SPLIT + getMessage() + LINE_SPLIT + getException();

        return eventlog;
    }
}
