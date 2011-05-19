/* Utilities for sorting members.

Copyright (c) 2006-2009 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.
*/
package ptolemy.backtrack.eclipse.plugin.actions.codestyle;

import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.util.CompilationUnitSorter;
import org.eclipse.jdt.internal.corext.codemanipulation.SortMembersOperation;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.util.JdtFlags;
import org.eclipse.jdt.internal.ui.actions.ActionMessages;
import org.eclipse.jdt.internal.ui.actions.ActionUtil;
import org.eclipse.jdt.internal.ui.actions.WorkbenchRunnableAdapter;
import org.eclipse.jdt.internal.ui.dialogs.OptionalMessageDialog;
import org.eclipse.jdt.internal.ui.javaeditor.IJavaAnnotation;
import org.eclipse.jdt.internal.ui.util.BusyIndicatorRunnableContext;
import org.eclipse.jdt.internal.ui.util.ElementValidator;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

import ptolemy.backtrack.eclipse.plugin.console.OutputConsole;

//////////////////////////////////////////////////////////////////////////
//// SortMembersUtility

/**
 * Utilities for sorting members.
 *
 * @author Thomas Huining Feng
 * @version $Id$
 * @since Ptolemy II 6.0
 * @Pt.ProposedRating Red (tfeng)
 * @Pt.AcceptedRating Red (tfeng)
 */
public class SortMembersUtility {

    public static void sortICompilationUnit(ICompilationUnit compilationUnit,
            IEditorPart editor) {
        Shell shell = editor.getEditorSite().getShell();

        if (compilationUnit == null) {
            return;
        }

        if (!ActionUtil.isProcessable(shell, compilationUnit)) {
            return;
        }

        if (!ElementValidator.check(compilationUnit, shell,
                ActionMessages.SortMembersAction_dialog_title, false)) {
            return;
        }

        if (editor != null && containsRelevantMarkers(editor)) {
            int returnCode = OptionalMessageDialog.open(
                    "ptolemy.backtrack.eclipse.plugin.actions."
                            + "SortMembersAction", shell,
                    ActionMessages.SortMembersAction_dialog_title, null,
                    ActionMessages.SortMembersAction_containsmarkers,
                    MessageDialog.WARNING, new String[] {
                            IDialogConstants.OK_LABEL,
                            IDialogConstants.CANCEL_LABEL }, 0);
            if (returnCode != OptionalMessageDialog.NOT_SHOWN
                    && returnCode != Window.OK) {
                return;
            }
        }

        ISchedulingRule schedulingRule = ResourcesPlugin.getWorkspace()
                .getRoot();
        PtolemySortMembersOperation operation = new PtolemySortMembersOperation(
                compilationUnit, null, false);
        try {
            BusyIndicatorRunnableContext context = new BusyIndicatorRunnableContext();
            PlatformUI.getWorkbench().getProgressService().runInUI(context,
                    new WorkbenchRunnableAdapter(operation, schedulingRule),
                    schedulingRule);
        } catch (InvocationTargetException e) {
            OutputConsole.outputError(e.getMessage());
        } catch (InterruptedException e) {
            // Do nothing. Operation has been canceled by user.
        }
    }

    public static class JavaElementComparator implements
            Comparator<BodyDeclaration> {

        public JavaElementComparator(boolean doNotSortFields) {
            _doNotSortFields = doNotSortFields;
        }

        /** Compare two body declarations and return a number reflecting
         *  the order between them.
         *
         *  @param bodyDeclaration1 The first body declaration.
         *  @param bodyDeclaration2 The second body declaration.
         *  @return -1 if the first body declaration should be sorted before
         *   the second; 1 if the second body declaration should be sorted
         *   before the first; 0 if the order does not matter.
         */
        public int compare(BodyDeclaration bodyDeclaration1,
                BodyDeclaration bodyDeclaration2) {
            int type1 = bodyDeclaration1.getNodeType();
            int type2 = bodyDeclaration2.getNodeType();

            // Initializers are a special case.
            if (type1 == ASTNode.INITIALIZER && type2 != ASTNode.INITIALIZER) {
                return 1;
            } else if (type1 != ASTNode.INITIALIZER
                    && type2 == ASTNode.INITIALIZER) {
                return -1;
            }

            boolean fieldType1 = (type1 == ASTNode.FIELD_DECLARATION || type1 == ASTNode.ENUM_CONSTANT_DECLARATION);
            boolean fieldType2 = (type2 == ASTNode.FIELD_DECLARATION || type2 == ASTNode.ENUM_CONSTANT_DECLARATION);
            if (_doNotSortFields && fieldType1 && fieldType2) {
                return preserveRelativeOrder(bodyDeclaration1, bodyDeclaration2);
            } else {
                return compareVisibility(bodyDeclaration1, bodyDeclaration2);
            }
        }

        protected int compareVisibility(BodyDeclaration bodyDeclaration1,
                BodyDeclaration bodyDeclaration2) {
            int visibilityCode1 = getVisibilityCode(bodyDeclaration1);
            int visibilityCode2 = getVisibilityCode(bodyDeclaration2);
            if (visibilityCode1 == visibilityCode2) {
                return compareNodeType(bodyDeclaration1, bodyDeclaration2);
            } else {
                return visibilityCode1 - visibilityCode2;
            }
        }

        private String buildSignature(Type type) {
            return ASTNodes.asString(type);
        }

        private int compareNames(BodyDeclaration bodyDeclaration1,
                BodyDeclaration bodyDeclaration2, String name1, String name2) {
            int nameResult = name1.compareTo(name2);
            if (nameResult != 0) {
                return nameResult;
            }
            return preserveRelativeOrder(bodyDeclaration1, bodyDeclaration2);
        }

        private int compareNodeType(BodyDeclaration bodyDeclaration1,
                BodyDeclaration bodyDeclaration2) {
            int typeCode1 = getNodeTypeCode(bodyDeclaration1);
            int typeCode2 = getNodeTypeCode(bodyDeclaration2);
            if (typeCode1 != typeCode2) {
                return typeCode1 - typeCode2;
            } else {
                switch (bodyDeclaration1.getNodeType()) {
                case ASTNode.METHOD_DECLARATION:
                    MethodDeclaration method1 = (MethodDeclaration) bodyDeclaration1;
                    MethodDeclaration method2 = (MethodDeclaration) bodyDeclaration2;

                    if (method1.isConstructor() && !method2.isConstructor()) {
                        return -1;
                    } else if (!method1.isConstructor()
                            && method2.isConstructor()) {
                        return 1;
                    }

                    String methodName1 = method1.getName().getIdentifier();
                    String methodName2 = method2.getName().getIdentifier();

                    // method declarations (constructors) are sorted by name
                    int nameResult = methodName1.compareTo(methodName2);
                    if (nameResult != 0) {
                        return nameResult;
                    }

                    // if names equal, sort by parameter types
                    List<?> parameters1 = method1.parameters();
                    List<?> parameters2 = method2.parameters();
                    int length1 = parameters1.size();
                    int length2 = parameters2.size();
                    int minLength = Math.min(length1, length2);
                    for (int i = 0; i < minLength; i++) {
                        SingleVariableDeclaration param1i = (SingleVariableDeclaration) parameters1
                                .get(i);
                        SingleVariableDeclaration param2i = (SingleVariableDeclaration) parameters2
                                .get(i);
                        int paramResult = buildSignature(param1i.getType())
                                .compareTo(buildSignature(param2i.getType()));
                        if (paramResult != 0) {
                            return paramResult;
                        }
                    }
                    if (length1 != length2) {
                        return length1 - length2;
                    }
                    return preserveRelativeOrder(bodyDeclaration1,
                            bodyDeclaration2);

                case ASTNode.FIELD_DECLARATION:
                    FieldDeclaration field1 = (FieldDeclaration) bodyDeclaration1;
                    FieldDeclaration field2 = (FieldDeclaration) bodyDeclaration2;

                    String fieldName1 = ((VariableDeclarationFragment) field1
                            .fragments().get(0)).getName().getIdentifier();
                    String fieldName2 = ((VariableDeclarationFragment) field2
                            .fragments().get(0)).getName().getIdentifier();

                    return compareNames(bodyDeclaration1, bodyDeclaration2,
                            fieldName1, fieldName2);

                case ASTNode.INITIALIZER:
                    return preserveRelativeOrder(bodyDeclaration1,
                            bodyDeclaration2);

                case ASTNode.TYPE_DECLARATION:
                case ASTNode.ENUM_DECLARATION:
                case ASTNode.ANNOTATION_TYPE_DECLARATION:
                    AbstractTypeDeclaration type1 = (AbstractTypeDeclaration) bodyDeclaration1;
                    AbstractTypeDeclaration type2 = (AbstractTypeDeclaration) bodyDeclaration2;

                    String typeName1 = type1.getName().getIdentifier();
                    String typeName2 = type2.getName().getIdentifier();

                    return compareNames(bodyDeclaration1, bodyDeclaration2,
                            typeName1, typeName2);

                case ASTNode.ENUM_CONSTANT_DECLARATION:
                    EnumConstantDeclaration enum1 = (EnumConstantDeclaration) bodyDeclaration1;
                    EnumConstantDeclaration enum2 = (EnumConstantDeclaration) bodyDeclaration2;

                    String enumName1 = enum1.getName().getIdentifier();
                    String enumName2 = enum2.getName().getIdentifier();

                    return compareNames(bodyDeclaration1, bodyDeclaration2,
                            enumName1, enumName2);

                case ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION:
                    AnnotationTypeMemberDeclaration annotation1 = (AnnotationTypeMemberDeclaration) bodyDeclaration1;
                    AnnotationTypeMemberDeclaration annotation2 = (AnnotationTypeMemberDeclaration) bodyDeclaration2;

                    String annotationName1 = annotation1.getName()
                            .getIdentifier();
                    String annotationName2 = annotation2.getName()
                            .getIdentifier();

                    return compareNames(bodyDeclaration1, bodyDeclaration2,
                            annotationName1, annotationName2);

                default:
                    return preserveRelativeOrder(bodyDeclaration1,
                            bodyDeclaration2);
                }
            }
        }

        private int getNodeTypeCode(BodyDeclaration bodyDeclaration) {
            switch (bodyDeclaration.getNodeType()) {
            case ASTNode.METHOD_DECLARATION:
                return 0;
            case ASTNode.FIELD_DECLARATION:
                return 1;
            case ASTNode.INITIALIZER:
                return 2;
            case ASTNode.TYPE_DECLARATION:
                return 3;
            case ASTNode.ENUM_DECLARATION:
                return 4;
            case ASTNode.ANNOTATION_TYPE_DECLARATION:
                return 5;
            case ASTNode.ENUM_CONSTANT_DECLARATION:
                return 6;
            case ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION:
                return 7;
            default:
                return -1;
            }
        }

        private int getVisibilityCode(BodyDeclaration bodyDeclaration) {
            switch (JdtFlags.getVisibilityCode(bodyDeclaration)) {
            case Modifier.PUBLIC:
                return 0;
            case Modifier.PROTECTED:
                return 1;
            case Modifier.NONE:
                return 2;
            case Modifier.PRIVATE:
                return 3;
            default:
                return -1;
            }
        }

        private int preserveRelativeOrder(BodyDeclaration bodyDeclaration1,
                BodyDeclaration bodyDeclaration2) {
            int order1 = ((Integer) bodyDeclaration1
                    .getProperty(CompilationUnitSorter.RELATIVE_ORDER))
                    .intValue();
            int order2 = ((Integer) bodyDeclaration2
                    .getProperty(CompilationUnitSorter.RELATIVE_ORDER))
                    .intValue();
            return order1 - order2;
        }

        private boolean _doNotSortFields;
    }

    public static class PtolemySortMembersOperation extends
            SortMembersOperation {
        public PtolemySortMembersOperation(ICompilationUnit compilationUnit,
                int[] positions, boolean doNotSortFields) {
            super(compilationUnit, positions, doNotSortFields);

            _compilationUnit = compilationUnit;
            _positions = positions;
            _comparator = new JavaElementComparator(doNotSortFields);
        }

        public void run(IProgressMonitor monitor) throws CoreException {
            CompilationUnitSorter.sort(AST.JLS3, _compilationUnit, _positions,
                    _comparator, 0, monitor);
        }

        private JavaElementComparator _comparator;

        private ICompilationUnit _compilationUnit;

        private int[] _positions;
    }

    private static boolean containsRelevantMarkers(IEditorPart editor) {
        IEditorInput input = editor.getEditorInput();
        IAnnotationModel model = JavaUI.getDocumentProvider()
                .getAnnotationModel(input);
        Iterator<?> iterator = model.getAnnotationIterator();
        while (iterator.hasNext()) {
            Object element = iterator.next();
            if (element instanceof IJavaAnnotation) {
                IJavaAnnotation annot = (IJavaAnnotation) element;
                if (!annot.isMarkedDeleted() && annot.isPersistent()
                        && !annot.isProblem()) {
                    return true;
                }
            }
        }
        return false;
    }
}
