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
package org.talend.camel.designer.ui.editor.dependencies.dialog;

import java.text.MessageFormat;
import java.util.Collection;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.talend.camel.designer.ui.editor.dependencies.Messages;
import org.talend.designer.camel.dependencies.core.model.ManifestItem;

/**
 * Dialog for create/edit bundle/package dependency.
 */
public class NewOrEditDependencyDialog extends TitleAreaDialog {

	/** The input. */
	private final Collection<? extends ManifestItem> input;

	/** The origin. */
	private final ManifestItem origin;

    /** The type. */
    private final String type;

	/** The name text. */
	private Text fNameText;
	
	/** The optional btn. */
	private Button fOptionalBtn;
	
	/** The version part. */
	private DependencyVersionPart fVersionPart;

    private final ManifestItem item;

    /**
	 * Instantiates a new new or edit dependency dialog. 
	 * Use for create a new dependency
	 *
	 * @param input the input
	 * @param parentShell the parent shell
	 * @param type the type
	 */
	public NewOrEditDependencyDialog(Collection<? extends ManifestItem> input, Shell parentShell, String type) {
		this(input, null, parentShell, type);
	}

	/**
	 * Instantiates a new new or edit dependency dialog.
	 * Use for edit an exist dependency
	 *
	 * @param input the input
	 * @param sourceItem the source item
	 * @param parentShell the parent shell
	 * @param type the type
	 */
	public NewOrEditDependencyDialog(Collection<? extends ManifestItem> input,
			ManifestItem sourceItem, Shell parentShell, String type) {
		super(parentShell);

		this.input = input;
		this.origin = sourceItem;
		this.type = type;

        item = ManifestItem.newItem(type);
        fVersionPart = new DependencyVersionPart(type != ManifestItem.EXPORT_PACKAGE);
        if (null != origin) {
            fVersionPart.setVersion(origin.getVersion());
        }
	}

    @Override
    protected Control createDialogArea(Composite parent) {
        final String name = type;
        final boolean isNew = origin == null;
        getShell().setText(name);
        setTitle(MessageFormat.format(isNew ? Messages.NewDependencyItemDialog_addTitle
            : Messages.NewDependencyItemDialog_editTitle, name));
        setMessage(MessageFormat.format(isNew ? Messages.NewDependencyItemDialog_addMsg
            : Messages.NewDependencyItemDialog_editMsg, name));

        parent.setLayout(new GridLayout());

        Composite c = new Composite(parent, SWT.NONE);
        c.setLayout(new GridLayout(2, false));
        c.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        new Label(c, SWT.NONE).setText(Messages.NewDependencyItemDialog_name);
        fNameText = new Text(c, SWT.BORDER);
        fNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        if (type != ManifestItem.EXPORT_PACKAGE) {
            Group propertiesGroup = new Group(parent, SWT.NONE);
            propertiesGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            propertiesGroup.setText(Messages.NewOrEditDependencyDialog_properties);
            propertiesGroup.setLayout(new GridLayout());
            fOptionalBtn = new Button(propertiesGroup, SWT.CHECK);
            fOptionalBtn.setText(Messages.NewDependencyItemDialog_optional);
        }

        fVersionPart.createVersionFields(parent, true, true);

        preloadFields();
        addListeners();
        return c;
    }

	/**
	 * Preload fields.
	 */
	private void preloadFields() {
		if (null != origin) {
			fNameText.setText(origin.getName());
			if (null != fOptionalBtn) {
	            fOptionalBtn.setSelection(origin.isOptional());
			}
		}
		fNameText.selectAll();
		fNameText.setFocus();
	}

	/**
	 * Adds the listeners.
	 */
	private void addListeners() {
		final ModifyListener modifyListener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				getButton(OK).setEnabled(validate());
			}
		};
		fNameText.addModifyListener(modifyListener);

        final SelectionListener selectionListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                getButton(OK).setEnabled(validate());
            }
        };
		if (null != fOptionalBtn) {
	        fOptionalBtn.addSelectionListener(selectionListener);
		}

		fVersionPart.addListeners(modifyListener, selectionListener);
	}

	/**
	 * Validate.
	 * 
	 * @return true, if successful valid
	 */
	private boolean validate() {
	    String errorMessage = validateName();
		if (null == errorMessage) {
		    errorMessage = fVersionPart.validateFullVersionRangeText();
		}
		if (null != errorMessage) {
			setErrorMessage(errorMessage);
			return false;
		}
        item.setName(getDependencyName());
        item.setVersion(getVersion());
        item.setOptional(getOptional());
        setErrorMessage(null);
        if (item.equals(origin) && item.isOptional() == origin.isOptional()
                && (item.getVersion() == null && origin.getVersion() == null
                    || item.getVersion() != null && item.getVersion().equals(origin.getVersion()))) {
			// nothing changes.
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		getButton(OK).setEnabled(false);
	}

	/**
	 * Gets the dependency item.
	 *
	 * @return the dependency item
	 */
	public ManifestItem getManifestItem() {
        return item;
	}

	/**
	 * Gets the dependency name.
	 *
	 * @return the dependency name
	 */
	private String getDependencyName() {
		return fNameText.getText().trim();
	}

	/**
	 * Gets the optional.
	 *
	 * @return the optional
	 */
	private boolean getOptional() {
		return (null != fOptionalBtn) ? fOptionalBtn.getSelection() : false;
	}

	/**
	 * Validate name.
	 *
	 * @return the i status
	 */
	public String validateName() {
		final String name = getDependencyName();
		if (origin != null && name.equals(origin.getName())) {
			return null;
		}
		for (ManifestItem o : input) {
			if (name.equals(o.getName())) {
				return Messages.NewDependencyItemDialog_existCheckMessage;
			}
		}
        // Bundle-SymbolicName could include dash (-) and other characters
        if (!this.type.equals(ManifestItem.REQUIRE_BUNDLE)) {
            final IStatus status = JavaConventions.validatePackageName(name);
            if (!status.isOK()) {
                return status.getMessage();
            }
        }
		return null;
	}

	/**
	 * Gets the version.
	 *
	 * @return the version
	 */
	public String getVersion() {
		return fVersionPart.getVersion();
	}

}
