// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.camel.designer.ui.editor.dependencies.dialog;

import org.eclipse.osgi.service.resolver.VersionRange;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.Version;
import org.talend.camel.designer.ui.editor.dependencies.Messages;

/**
 * Use to create UI part to set/get dependency version.
 *
 * @author GaoZone (modify from {@link org.eclipse.pde.internal.ui.parts.PluginVersionPart}
 */
public class DependencyVersionPart {

    /** The min version text. */
    private Text fMinVersionText;

    /** The min version bound. */
    private Combo fMinVersionBound;

    /** The max version text. */
    private Text fMaxVersionText;

    /** The max version bound. */
    private Combo fMaxVersionBound;

    /** The version range. */
    private VersionRange fVersionRange;

    /** The is ranged. */
    private boolean fIsRanged;

    /** The range allowed. */
    private final boolean fRangeAllowed;

    /**
     * Instantiates a new dependency version part. Support single version input and version range input.
     *
     * @param rangeAllowed the range allowed, true means need to fill both min version and max version.
     */
    public DependencyVersionPart(boolean rangeAllowed) {
        this.fRangeAllowed = rangeAllowed;
    }

    /**
     * Creates the version fields.
     *
     * @param comp the comp
     * @param createGroup the create group
     * @param editable the editable
     */
    public void createVersionFields(Composite comp, boolean createGroup, boolean editable) {
        if (fRangeAllowed)
            createRangeField(comp, createGroup, editable);
        else
            createSingleField(comp, createGroup, editable);
        preloadFields();
    }

    /**
     * Creates the range field.
     *
     * @param parent the parent
     * @param createGroup the create group
     * @param editable the editable
     */
    private void createRangeField(Composite parent, boolean createGroup, boolean editable) {
        if (createGroup) {
            parent = new Group(parent, SWT.NONE);
            ((Group) parent).setText(getGroupText());
            parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            parent.setLayout(new GridLayout(3, false));
        }

        String[] comboItems = new String[] { Messages.DependencyVersionPart_comboInclusive,
                Messages.DependencyVersionPart_comboExclusive };
        Label minlabel = new Label(parent, SWT.NONE);
        minlabel.setText(Messages.DependencyVersionPart_minimumVersion);
        fMinVersionText = new Text(parent, SWT.SINGLE | SWT.BORDER);
        fMinVersionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fMinVersionText.setEnabled(editable);

        fMinVersionBound = new Combo(parent, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
        fMinVersionBound.setEnabled(editable);
        fMinVersionBound.setItems(comboItems);

        Label maxlabel = new Label(parent, SWT.NONE);
        maxlabel.setText(Messages.DependencyVersionPart_maximumVersion);
        fMaxVersionText = new Text(parent, SWT.SINGLE | SWT.BORDER);
        fMaxVersionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fMaxVersionText.setEnabled(editable);

        fMaxVersionBound = new Combo(parent, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
        fMaxVersionBound.setEnabled(editable);
        fMaxVersionBound.setItems(comboItems);
    }

    /**
     * Creates the single field.
     *
     * @param parent the parent
     * @param createGroup the create group
     * @param editable the editable
     */
    private void createSingleField(Composite parent, boolean createGroup, boolean editable) {
        if (createGroup) {
            parent = new Group(parent, SWT.NONE);
            ((Group) parent).setText(getGroupText());
            parent.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL));
            parent.setLayout(new GridLayout(2, false));
        }
        Label label = new Label(parent, SWT.NONE);
        label.setText(Messages.DependencyVersionPart_version);

        fMinVersionText = new Text(parent, SWT.SINGLE | SWT.BORDER);
        fMinVersionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fMinVersionText.setEnabled(editable);
    }

    /**
     * Preload fields. Call after UI init.
     */
    private void preloadFields() {
        if (fRangeAllowed) {
            fMinVersionText.setText((fVersionRange != null) ? fVersionRange.getMinimum().toString() : ""); //$NON-NLS-1$
            fMaxVersionText.setText(fVersionRange != null && fVersionRange.getMaximum().getMajor() != Integer.MAX_VALUE
                    ? fVersionRange.getMaximum().toString() : ""); //$NON-NLS-1$

            if (fVersionRange != null)
                fMinVersionBound.select(fVersionRange.getIncludeMinimum() ? 0 : 1);
            else
                fMinVersionBound.select(0);

            if (fVersionRange != null && getMaxVersion().length() > 0)
                fMaxVersionBound.select(fVersionRange.getIncludeMaximum() ? 0 : 1);
            else
                fMaxVersionBound.select(1);
        }
        fMinVersionText.setText((fVersionRange != null) ? fVersionRange.getMinimum().toString() : ""); //$NON-NLS-1$
    }

    /**
     * Validate version.
     *
     * @param versionString the versionString
     * @return the i status
     */
    private static String validateVersion(String versionString) {
        if (versionString.isEmpty()) {
            return null;
        }
        try {
            Version.parseVersion(versionString);
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
        return null;
    }

    /**
     * Validate version range.
     *
     * @return the i status
     */
    private String validateVersionRange() {
        if (!fRangeAllowed || getMinVersion().isEmpty() || getMaxVersion().isEmpty()) {
            fIsRanged = false;
            return null;
        }
        final int c = new Version(getMinVersion()).compareTo(new Version(getMaxVersion()));
        if (c == 0 || c < 0) {
            fIsRanged = c != 0;
            return null;
        }
        return Messages.DependencyVersionDialog_versionRangeError;
    }

    /**
     * Validate full version range text.
     *
     * @return an OK status if all versions are valid, otherwise the status's message will contain an error message.
     */
    public String validateFullVersionRangeText() {
        String status = validateVersion(getMinVersion());
        if (status == null)
            status = validateVersion(getMaxVersion());
        if (status == null)
            status = validateVersionRange();
        return status;
    }

    private String getMinVersion() {
        return fMinVersionText.getText().trim();
    }

    private String getMaxVersion() {
        if (fMaxVersionText != null)
            return fMaxVersionText.getText().trim();
        return ""; //$NON-NLS-1$
    }

    private boolean getMinInclusive() {
        if (fMinVersionBound != null)
            return fMinVersionBound.getSelectionIndex() == 0;
        return false;
    }

    private boolean getMaxInclusive() {
        if (fMaxVersionBound != null)
            return fMaxVersionBound.getSelectionIndex() == 0;
        return true;
    }

    /**
     * Extract single version from text.
     *
     * @return the string
     */
    private String extractSingleVersionFromText() {
        if (!fRangeAllowed)
            return getMinVersion();
        if (getMinVersion().isEmpty())
            return getMaxVersion();
        return getMinVersion();
    }

    public String getVersion() {
        if (fIsRanged) {
            // if versions are equal they must be inclusive for a range to be
            // valid
            // blindly set for the user
            String minV = getMinVersion();
            String maxV = getMaxVersion();
            boolean minI = getMinInclusive();
            boolean maxI = getMaxInclusive();
            if (minV.equals(maxV))
                minI = maxI = true;
            VersionRange versionRange = new VersionRange(new Version(minV), minI, new Version(maxV), maxI);
            if (!versionRange.equals(VersionRange.emptyRange)) {
                return versionRange.toString();
            }
        } else {
            String singleversion = extractSingleVersionFromText();
            if (!singleversion.isEmpty()) {
                Version versionBean = new Version(singleversion);
                if (!versionBean.equals(Version.emptyVersion)) {
                    return versionBean.toString();
                }
            }
        }
        return null;
    }

    /**
     * Adds the listeners.
     *
     * @param minListener the min listener
     * @param selectionListener the selection listener
     */
    public void addListeners(ModifyListener minListener, SelectionListener selectionListener) {
        if (fMinVersionText != null && minListener != null) {
            fMinVersionText.addModifyListener(minListener);
        }
        if (fRangeAllowed && fMaxVersionText != null && minListener != null) {
            fMaxVersionText.addModifyListener(minListener);
        }
        if (fRangeAllowed && fMinVersionBound != null) {
            fMinVersionBound.addSelectionListener(selectionListener);
            fMaxVersionBound.addSelectionListener(selectionListener);
        }
    }

    protected String getGroupText() {
        return Messages.DependenceVersionPart_groupText;
    }

    public void setVersion(String version) {
        try {
            if (version != null && !version.equals("")) { //$NON-NLS-1$
                fVersionRange = new VersionRange(version);
                Version max = fVersionRange.getMaximum();
                if (max.getMajor() != Integer.MAX_VALUE && fVersionRange.getMinimum().compareTo(fVersionRange.getMaximum()) < 0)
                    fIsRanged = true;
            }
        } catch (IllegalArgumentException e) {
            // illegal version string passed
            fVersionRange = new VersionRange("[1.0.0,1.0.0]"); //$NON-NLS-1$
        }
    }

}
