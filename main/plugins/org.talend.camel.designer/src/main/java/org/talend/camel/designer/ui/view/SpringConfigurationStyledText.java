package org.talend.camel.designer.ui.view;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class SpringConfigurationStyledText extends StyledText implements ExtendedModifyListener, KeyListener, MenuDetectListener {

    // comment case
    public static final int COMMENT = 0;

    // key case
    public static final int KEY = 1;

    // string case
    public static final int STRING = 2;

    // xml element case
    public static final int ELEMENT = 4;

    // xml attribute
    public static final int ATTRIBUTE = 8;

    // other
    public static final int OTHER = 16;

    // end of file
    public static final int EOF = -1;

    // comment color
    public static final RGB commentRGB = new RGB(63, 95, 191);

    // key color
    public static final RGB keyRGB = new RGB(128, 0, 0);

    // string color
    public static final RGB stringRGB = new RGB(42, 0, 255);

    // element name color
    public static final RGB elementRGB = new RGB(63, 127, 127);

    // attribute name color
    public static final RGB attributeRGB = new RGB(127, 0, 127);

    // other color
    public static final RGB otherRGB = new RGB(0, 0, 0);

    // make stack side of redo/undo
    private static final int MAX_STACK_SIZE = 10;

    private static Color[] colors = null;

    // key words of xml
    private String[] fgKeywords = { "<?", "xml", "?>" };

    private static final String COMMENT_START_TAG = "<!--";

    private static final String COMMENT_END_TAG = "-->";

    // xml content scanner
    private XmlScanner scanner = new XmlScanner();

    // undo list
    private List<UndoRedoModel> undoStack;

    // redo list
    private List<UndoRedoModel> redoStack;

    // used to indicate the change is caused by redo/undo action or not
    private boolean fromUndoRedo = false;

    public SpringConfigurationStyledText(Composite parent, int style) {
        super(parent, style);
        undoStack = new LinkedList<>();
        redoStack = new LinkedList<>();

        initialize();
        addListeners();
    }

    private void addListeners() {
        // this listener used to handle the change delta
        addExtendedModifyListener(this);

        // used for short-keys
        addKeyListener(this);

        // used for context menus
        addMenuDetectListener(this);
    }

    /**
     * init the colors please do not dispose them when close the text because they are shared
     */
    private void initialize() {
        if (colors == null) {
            colors = new Color[6];
            colors[0] = new Color(getDisplay(), commentRGB);
            colors[1] = new Color(getDisplay(), keyRGB);
            colors[2] = new Color(getDisplay(), stringRGB);
            colors[3] = new Color(getDisplay(), elementRGB);
            colors[4] = new Color(getDisplay(), attributeRGB);
            colors[5] = new Color(getDisplay(), otherRGB);
        }
    }

    private Color getColor(int token) {
        switch (token) {
        case COMMENT:
            return colors[0];
        case KEY:
            return colors[1];
        case STRING:
            return colors[2];
        case ELEMENT:
            return colors[3];
        case ATTRIBUTE:
            return colors[4];
        default:
            break;
        }
        return colors[5];
    }

    private void undo() {
        if (undoStack.size() > 0) {
            fromUndoRedo = true;
            UndoRedoModel lastEdit = undoStack.remove(0);
            int length = lastEdit.getLength();
            String oldContent = lastEdit.getOldContent();
            int startReplaceIndex = lastEdit.getOffset();
            replaceTextRange(startReplaceIndex, length, oldContent);
            redoStack.add(0, lastEdit);
            fromUndoRedo = false;
            setSelectionRange(startReplaceIndex, oldContent.length());
        }
    }

    private void redo() {
        if (redoStack.size() > 0) {
            fromUndoRedo = true;
            UndoRedoModel lastEdit = redoStack.remove(0);
            String oldContent = lastEdit.getOldContent();
            int startReplaceIndex = lastEdit.getOffset();
            replaceTextRange(startReplaceIndex, oldContent.length(), lastEdit.getContent());
            undoStack.add(0, lastEdit);

            fromUndoRedo = false;
            setSelectionRange(startReplaceIndex, lastEdit.getContent().length());
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.stateMask == SWT.CTRL) {
            switch (e.keyCode) {
            case 'a':
            case 'A':
                selectAll();
                break;
            case 'z':
            case 'Z':
                undo();
                break;
            case 'y':
            case 'Y':
                redo();
                break;
            default:
                break;
            }
        } else if ((e.stateMask & SWT.CTRL) > 0 && (e.stateMask & SWT.SHIFT) > 0) {
            switch (e.keyCode) {
            case '?':
            case '/':
                commentEventHandler();
                break;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void menuDetected(MenuDetectEvent e) {
        /*
         * create context menus and set a data for each item which will be used on listener to indicate the item
         */
        Menu m = new Menu(this);

        MenuItem copyItem = new MenuItem(m, SWT.NONE);
        copyItem.setText("&Copy\tCtrl+C");
        copyItem.setEnabled(!"".equals(getSelectionText()));
        copyItem.setData('c');

        MenuItem pasteItem = new MenuItem(m, SWT.NONE);
        pasteItem.setText("&Paste\tCopy+V");
        pasteItem.setData('p');

        MenuItem cutItem = new MenuItem(m, SWT.NONE);
        cutItem.setText("Cu&t\tCtrl+X");
        cutItem.setEnabled(!"".equals(getSelectionText()));
        cutItem.setData('t');

        new MenuItem(m, SWT.SEPARATOR);
        MenuItem selectAllItem = new MenuItem(m, SWT.NONE);
        selectAllItem.setText("&Select All\tCtrl+A");
        selectAllItem.setData('s');
        new MenuItem(m, SWT.SEPARATOR);

        MenuItem undoItem = new MenuItem(m, SWT.NONE);
        undoItem.setText("&Undo\tCtrl+Z");
        undoItem.setEnabled(undoStack.size() > 0);
        undoItem.setData('u');

        MenuItem redoItem = new MenuItem(m, SWT.NONE);
        redoItem.setText("&Redo\tCtrl+Y");
        redoItem.setEnabled(redoStack.size() > 0);
        redoItem.setData('r');

        new MenuItem(m, SWT.SEPARATOR);
        MenuItem commentItem = null;
        if (isCommentAvailable()) {
            commentItem = new MenuItem(m, SWT.NONE);
            commentItem.setText("&Comment\tCtrl+Shift+/");
            commentItem.setData('/');
        } else if (selectionIsCommented()) {
            commentItem = new MenuItem(m, SWT.NONE);
            commentItem.setText("&Uncomment\tCtrl+Shift+/");
            commentItem.setData('/');
        }

        setMenu(m);

        SelectionAdapter listener = new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Object data = e.widget.getData();
                if (data == null || !(data instanceof Character)) {
                    return;
                }
                switch ((Character) data) {
                case 'c':
                    copy();
                    break;
                case 'p':
                    paste();
                    break;
                case 't':
                    cut();
                    break;
                case 's':
                    selectAll();
                    break;
                case 'u':
                    undo();
                    break;
                case 'r':
                    redo();
                    break;
                case '/':
                    commentEventHandler();
                    break;
                default:
                    break;
                }
            }
        };
        copyItem.addSelectionListener(listener);
        pasteItem.addSelectionListener(listener);
        cutItem.addSelectionListener(listener);
        selectAllItem.addSelectionListener(listener);
        undoItem.addSelectionListener(listener);
        redoItem.addSelectionListener(listener);
        if (commentItem != null) {
            commentItem.addSelectionListener(listener);
        }
    }

    @Override
    public void modifyText(ExtendedModifyEvent event) {
        /*
         * every changes should be record into undo list (except the change is caused by redo/undo action)
         */
        addUndoList(event);

        /*
         * update the styles
         */
        updateStyledRanges(event);

        /*
         * auto completion
         */
        int start = event.start;
        int length = event.length;
        String textRange = getTextRange(start, length);
        String previousChar = null;
        if (start - 1 > 0) {
            previousChar = getTextRange(start - 1, 1);
        }

        if ("/".equals(textRange) && "<".equals(previousChar) && !isCommentBlock(start - 1)) {
            String openedTag = searchNearestOpenedTag(start - 2);
            if (openedTag != null && !openedTag.isEmpty()) {
                replaceTextRange(start + length, 0, openedTag);
                setSelection(start + event.length + openedTag.length());
            }
        }
    }

    private String searchNearestOpenedTag(int lastOffset) {
        Stack<String> closeTagStack = new Stack<>();
        boolean hasSingleEndTag = false;
        for (int i = lastOffset; i >= 0; i--) {
            String currentChar = getText(i, i);
            if (isCommentBlock(i)) {
                continue;
            }
            if (">".equals(currentChar)) {
                if (hasSingleEndTag) {
                    return null;
                }
                if (i > 0 && "/".equals(getText(i - 1, i - 1))) {
                    hasSingleEndTag = true;
                    i--;
                }
                continue;
            }
            if ("<".equals(currentChar)) {
                if (hasSingleEndTag) {
                    hasSingleEndTag = false;
                    continue;
                }
                String nextChar = getText(i + 1, i + 1);
                if ("/".equals(nextChar)) {
                    String searchTagName = searchTagName(i + 2, lastOffset);
                    if (!searchTagName.isEmpty()) {
                        closeTagStack.push(searchTagName);
                    }
                    continue;
                }
                String currentTagName = searchTagName(i + 1, lastOffset);
                if (closeTagStack.isEmpty()) {
                    return currentTagName.isEmpty() ? null : currentTagName + ">";
                } else {
                    String pop = closeTagStack.pop();
                    if (!currentTagName.equals(pop)) {
                        return null;
                    }
                }
            }

        }
        return null;
    }

    private String searchTagName(int startOffset, int lastOffset) {
        StringBuffer tagNameSB = new StringBuffer();
        for (int j = startOffset; j < lastOffset; j++) {
            String closeTagName = getText(j, j);
            if ("<".equals(closeTagName)) {
                return null;
            }
            if (Character.isWhitespace(closeTagName.charAt(0)) || ">".equals(closeTagName)) {
                break;
            }
            tagNameSB.append(closeTagName);
        }
        return tagNameSB.toString().trim();
    }

    private boolean isCommentBlock(int offset) {
        StyleRange[] styleRanges = getStyleRanges(offset, 1);
        if (styleRanges != null && styleRanges.length > 0 && commentRGB.equals(styleRanges[0].foreground.getRGB())) {
            return true;
        }
        return false;
    }

    protected void commentEventHandler() {
        Point selectionRange = getSelectionRange();
        String selectionText = getSelectionText();
        if (selectionIsCommented()) {
            int commentStartIndex = selectionText.indexOf(COMMENT_START_TAG);
            int commentEndIndex = selectionText.lastIndexOf(COMMENT_END_TAG);
            String afterReplacedText = selectionText.substring(0, commentStartIndex);
            afterReplacedText += selectionText.substring(commentStartIndex + COMMENT_START_TAG.length(), commentEndIndex);
            replaceTextRange(selectionRange.x, selectionRange.y, afterReplacedText);
        } else if (selectionText.trim().length() > 0) {
            replaceTextRange(selectionRange.x, selectionRange.y, COMMENT_START_TAG + selectionText + COMMENT_END_TAG);
        }
    }

    protected boolean selectionIsCommented() {
        String selectionText = getSelectionText();
        String trimedSelection = selectionText.trim();
        if (trimedSelection.length() >= COMMENT_END_TAG.length() + COMMENT_START_TAG.length()
                && trimedSelection.startsWith(COMMENT_START_TAG) && trimedSelection.endsWith(COMMENT_END_TAG)) {
            return true;
        }
        return false;
    }

    private boolean isCommentAvailable() {
        if (selectionIsCommented()) {
            return false;
        }
        return getSelectionText().trim().length() > 0;
    }

    private void updateStyledRanges(ExtendedModifyEvent event) {
        StyledText st = (StyledText) event.widget;
        int start = event.start;
        int length = event.length;
        String replacedText = event.replacedText;
        String textRange = st.getTextRange(start, length);
        /*
         * if no real change then return
         */
        if ("".equals(textRange.trim()) && "".equals(replacedText.trim())) {
            return;
        }

        List<StyleRange> styles = new ArrayList<>();
        scanner.setRange(st.getText());
        int token = scanner.nextToken();
        while (token != EOF) {
            int startOffset = scanner.getStartOffset();
            int tokenLength = scanner.getLength();
            String tokenText = st.getTextRange(startOffset, tokenLength).trim();
            for (String s : fgKeywords) {
                if (s.equals(tokenText)) {
                    token = KEY;
                    break;
                }
            }

            Color color = getColor(token);
            StyleRange style = new StyleRange(startOffset, tokenLength, color, null);
            if (token == KEY) {
                style.fontStyle = SWT.BOLD;
            }
            styles.add(style);
            token = scanner.nextToken();
        }
        st.setStyleRanges(styles.toArray(new StyleRange[0]));
    }

    private void addUndoList(ExtendedModifyEvent event) {
        if (fromUndoRedo) {
            return;
        }
        StyledText st = (StyledText) event.widget;
        String currText = st.getText();
        String newText = currText.substring(event.start, event.start + event.length);
        if (undoStack.size() == MAX_STACK_SIZE) {
            undoStack.remove(undoStack.size() - 1);
        }
        undoStack.add(0, new UndoRedoModel(newText, event.start, event.length, event.replacedText));

    }

    private static class UndoRedoModel {

        private String content = "";

        private int offset;

        private int length;

        private String oldContent = "";

        public UndoRedoModel(String content, int offset, int length, String oldContent) {
            this.content = content;
            this.oldContent = oldContent;
            this.offset = offset;
            this.length = length;
        }

        public String getContent() {
            return content == null ? "" : content;
        }

        public int getOffset() {
            return offset;
        }

        public String getOldContent() {
            return oldContent == null ? "" : oldContent;
        }

        public int getLength() {
            return length;
        }
    }

    private static class XmlScanner {

        protected String content;

        protected int currentPosition;

        protected int endPosition;

        protected int startToken;

        /**
         * Returns the ending location of the current token in the document.
         */
        public final int getLength() {
            return currentPosition - startToken;
        }

        /**
         * Returns the starting location of the current token in the document.
         */
        public final int getStartOffset() {
            return startToken;
        }

        /**
         * Returns the next lexical token in the document.
         */
        public int nextToken() {
            int c;
            startToken = currentPosition;
            c = read();
            while (Character.isWhitespace(c)) {
                c = read();
            }
            unread(c);
            switch (c = read()) {
            case EOF:
                return EOF; // end of file
            case '<': // element start
                c = read();
                if (c == EOF) {
                    return EOF;
                }
                if ('?' == c) { // xml start
                    return KEY;
                }
                if ('!' == c) { // potential a comment
                    c = read();
                    if ('-' == c) {
                        c = read();
                        if ('-' == c) { // comment

                            while (true) {
                                c = read();
                                if (c == EOF) {
                                    return COMMENT;
                                }
                                if (c != '-') {
                                    continue;
                                }
                                c = read(); // potential end of comment
                                if (c == EOF) {
                                    return COMMENT;
                                }
                                if (c != '-') {
                                    continue;
                                }
                                c = read();
                                if (c == EOF) {
                                    return COMMENT;
                                }
                                if (c != '>') { // end of comment
                                    continue;
                                } else {
                                    return COMMENT;
                                }
                            }
                        } else {
                            unread(c);
                            unread(c);
                        }
                    } else {
                        unread(c);
                    }
                }

                while (Character.isWhitespace((char) c)) {
                    c = read();
                    if (c == EOF) {
                        return EOF;
                    }
                }
                if (c == '>') {
                    return ELEMENT;
                }
                if ('/' == c) { // potential end of element
                    c = read();
                    if (c == EOF) {
                        return EOF;
                    }
                    if (c == '>') {
                        return ELEMENT;
                    } else {
                        unread(c);
                        unread(c);
                    }
                } else {
                    unread(c);
                }
                while (c != EOF && Character.isWhitespace((char) c)) {
                    c = read();
                }
                while (c != EOF && !Character.isWhitespace((char) c)) {
                    if (c == '>') {
                        return ELEMENT;
                    }
                    c = read();
                }
                return ELEMENT;
            case '/': // potential end of element
                c = read();
                while (c != EOF && '>' != c) {
                    c = read();
                }
                return ELEMENT;
            case '"': // attribute value start
                for (;;) {
                    c = read();
                    switch (c) {
                    case '"':
                        return STRING; // attribute value end
                    case EOF:
                        unread(c);
                        return STRING;
                    }
                }
            case '>': // end of element
                return ELEMENT;
            case '=': // attribute equal
                return OTHER;
            default:
                while (c != EOF && '=' != (char) c && '<' != (char) c) {
                    c = read();
                }
                if ('=' == (char) c || '<' == (char) c) {
                    unread(c);
                }
                return ATTRIBUTE; // all others seems an attribute
            }
        }

        /**
         * Returns next character.
         */
        protected int read() {
            if (currentPosition <= endPosition) {
                return content.charAt(currentPosition++);
            }
            return EOF;
        }

        public void setRange(String text) {
            content = text;
            currentPosition = 0;
            endPosition = content.length() - 1;
        }

        protected void unread(int c) {
            if (c != EOF)
                currentPosition--;
        }
    }
}
