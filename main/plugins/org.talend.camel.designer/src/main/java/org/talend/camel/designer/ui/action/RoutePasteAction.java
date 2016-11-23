// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006-2013 Talend – www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
// ============================================================================
package org.talend.camel.designer.ui.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.ui.actions.Clipboard;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPart;
import org.talend.camel.designer.ui.editor.CamelTalendEditor;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.EComponentType;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.designer.core.model.process.AbstractProcessProvider;
import org.talend.designer.core.ui.action.GEFPasteAction;
import org.talend.designer.core.ui.editor.cmd.MultiplePasteCommand;
import org.talend.designer.core.ui.editor.cmd.NodesPasteCommand;
import org.talend.designer.core.ui.editor.cmd.NotesPasteCommand;
import org.talend.designer.core.ui.editor.connections.ConnLabelEditPart;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodeLabelEditPart;
import org.talend.designer.core.ui.editor.nodes.NodePart;
import org.talend.designer.core.ui.editor.notes.NoteEditPart;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainerPart;

/**
 * @see org.talend.camel.designer.ui.editor.CamelEditorDropTargetListener
 */
public class RoutePasteAction extends GEFPasteAction {

    public RoutePasteAction(IWorkbenchPart part) {
        super(part);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        Object clipBoardContent;
        try {
            clipBoardContent = Clipboard.getDefault().getContents();
        } catch (RuntimeException e) {
            return;
        }

        org.eclipse.swt.dnd.Clipboard systemClipboard = new org.eclipse.swt.dnd.Clipboard(Display.getCurrent());
        Object systemObject = systemClipboard.getContents(TextTransfer.getInstance());

        if (clipBoardContent instanceof List) {
            List<EditPart> partsList = (List<EditPart>) clipBoardContent;
            if (partsList == null || partsList.isEmpty()) {
                return;
            }

            List<NodePart> nodeParts = new ArrayList<>();
            List<NoteEditPart> noteParts = new ArrayList<>();

            for (Object o : partsList) {
                if (o instanceof NodePart) {
                    if (!nodeParts.contains(o)) {
                        nodeParts.add((NodePart) o);
                    }
                } else if (o instanceof NoteEditPart) {
                    noteParts.add((NoteEditPart) o);
                }
            }
            CamelTalendEditor editor = (CamelTalendEditor) this.getWorkbenchPart();
            org.eclipse.draw2d.geometry.Point gefPoint = getCursorLocation();

            AbstractProcessProvider findProcessProviderFromPID = AbstractProcessProvider
                    .findProcessProviderFromPID(IComponent.JOBLET_PID);
            if (findProcessProviderFromPID != null) {
                boolean isDuplicateRoutelet = false;
                String duplicateRouteletName = "";
                for (NodePart copiedNodePart : nodeParts) {
                    Node copiedNode = (Node) copiedNodePart.getModel();
                    // add for bug TDI-20207.if copy joblet/job to itself,then return.
                    EComponentType componentType = null;
                    String copideNodeId = null;
                    String editorProcessId = null;
                    if (copiedNode.getComponent() != null) {
                        componentType = copiedNode.getComponent().getComponentType();
                        if (copiedNode.getComponent().getProcess() != null) {
                            copideNodeId = copiedNode.getComponent().getProcess().getId();
                        }
                    }
                    if (editor.getProcess() != null) {
                        editorProcessId = editor.getProcess().getId();
                    }
                    for (IElementParameter element : copiedNode.getElementParametersFromField(EParameterFieldType.PROCESS_TYPE)) {
                        for (Map.Entry<String, IElementParameter> entry : element.getChildParameters().entrySet()) {
                            if (("PROCESS_TYPE_PROCESS").equals(entry.getKey())) {
                                if (editorProcessId != null && editorProcessId.equals(entry.getValue().getValue())) {
                                    return;
                                }
                            }
                        }
                    }

                    if ((EComponentType.JOBLET).equals(componentType)) {
                        // Check if is copying routelet in itself
                        if (editorProcessId != null && editorProcessId.equals(copideNodeId)) {
                            return;
                        }
                        // Check if is duplicate routelet
                        if (editor.getProcess().getNodesOfType(copiedNode.getComponent().getName()).size() > 0) {
                            isDuplicateRoutelet = true;
                            duplicateRouteletName += " ," + copiedNode.getComponent().getName(); //$NON-NLS-1$
                        }
                    }
                    // Check if it's Camel component
                    if (!ComponentCategory.CATEGORY_4_CAMEL.getName().equals(copiedNode.getComponent().getType())) {
                        return;
                    }
                }

                if (isDuplicateRoutelet) {
                    MessageDialog.openInformation(editor.getEditorSite().getShell(), "Copying Routelet", //$NON-NLS-1$
                            "Do not allow duplicate Routelets\nRoutelet \"" + duplicateRouteletName.substring(2) //$NON-NLS-1$
                                    + "\" already exist.");
                    return;
                }
            }

            if (nodeParts.size() != 0 && noteParts.size() != 0) {
                MultiplePasteCommand mpc = new MultiplePasteCommand(nodeParts, noteParts,
                        (org.talend.designer.core.ui.editor.process.Process) editor.getProcess(), gefPoint);
                mpc.setSelectedSubjobs(new ArrayList<SubjobContainerPart>());
                execute(mpc);
            } else if (nodeParts.size() != 0) {
                NodesPasteCommand cmd = new NodesPasteCommand(nodeParts, editor.getProcess(), gefPoint);
                cmd.setSelectedSubjobs(new ArrayList<SubjobContainerPart>());
                execute(cmd);
            } else if (noteParts.size() != 0) {
                NotesPasteCommand cmd = new NotesPasteCommand(noteParts,
                        (org.talend.designer.core.ui.editor.process.Process) editor.getProcess(), gefPoint, false, null);
                execute(cmd);
            }
            setCursorLocation(null);
        } else if (clipBoardContent instanceof String) {
            List objects = getSelectedObjects();

            if (objects.size() == 1) {
                String content = (String) clipBoardContent;
                if (objects.get(0) instanceof NoteEditPart && ((NoteEditPart) objects.get(0)).getDirectEditManager() != null) {
                    Text text = ((NoteEditPart) objects.get(0)).getDirectEditManager().getTextControl();
                    if (text != null) {
                        text.insert(content);
                    }
                } else if (objects.get(0) instanceof ConnLabelEditPart
                        && ((ConnLabelEditPart) objects.get(0)).getDirectEditManager() != null) {
                    Text text = ((ConnLabelEditPart) objects.get(0)).getDirectEditManager().getTextControl();
                    if (text != null) {
                        text.insert(content);
                    }
                } else if (objects.get(0) instanceof NodeLabelEditPart
                        && ((NodeLabelEditPart) objects.get(0)).getDirectEditManager() != null) {
                    {
                        Text text = (Text) ((NodeLabelEditPart) objects.get(0)).getDirectEditManager().getCellEditor()
                                .getControl();
                        if (text != null) {
                            text.insert(content);
                        }
                    }

                }
            }
        } else if (systemObject != null && systemObject instanceof String) {
            List objects = getSelectedObjects();

            if (objects.size() == 1) {
                String content = (String) systemObject;
                if (objects.get(0) instanceof NoteEditPart && ((NoteEditPart) objects.get(0)).getDirectEditManager() != null) {
                    Text text = ((NoteEditPart) objects.get(0)).getDirectEditManager().getTextControl();
                    if (text != null) {
                        text.insert(content);
                    }
                } else if (objects.get(0) instanceof ConnLabelEditPart
                        && ((ConnLabelEditPart) objects.get(0)).getDirectEditManager() != null) {
                    Text text = ((ConnLabelEditPart) objects.get(0)).getDirectEditManager().getTextControl();
                    if (text != null) {
                        text.insert(content);
                    }
                } else if (objects.get(0) instanceof NodeLabelEditPart
                        && ((NodeLabelEditPart) objects.get(0)).getDirectEditManager() != null) {
                    {
                        Text text = (Text) ((NodeLabelEditPart) objects.get(0)).getDirectEditManager().getCellEditor()
                                .getControl();
                        if (text != null) {
                            text.insert(content);
                        }
                    }

                }
            }
        }
    }

}
