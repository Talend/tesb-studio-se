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
package org.talend.designer.esb.runcontainer.ui.console.ansi;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.talend.designer.esb.runcontainer.util.AnsiConsoleUtils;

public class AnsiConsoleAttributes implements Cloneable {

    public final static int UNDERLINE_NONE = -1; // nothing in SWT, a bit of an abuse

    public Integer currentBgColor;

    public Integer currentFgColor;

    public int underline;

    public boolean bold;

    public boolean italic;

    public boolean invert;

    public boolean conceal;

    public boolean strike;

    public boolean framed;

    public AnsiConsoleAttributes() {
        reset();
    }

    public void reset() {
        currentBgColor = null;
        currentFgColor = null;
        underline = UNDERLINE_NONE;
        bold = false;
        italic = false;
        invert = false;
        conceal = false;
        strike = false;
        framed = false;
    }

    @Override
    public AnsiConsoleAttributes clone() {
        AnsiConsoleAttributes result = new AnsiConsoleAttributes();
        result.currentBgColor = currentBgColor;
        result.currentFgColor = currentFgColor;
        result.underline = underline;
        result.bold = bold;
        result.italic = italic;
        result.invert = invert;
        result.conceal = conceal;
        result.strike = strike;
        result.framed = framed;
        return result;
    }

    public static Color hiliteRgbColor(Color c) {
        if (c == null)
            return new Color(null, new RGB(0xff, 0xff, 0xff));
        int red = c.getRed() * 2;
        int green = c.getGreen() * 2;
        int blue = c.getBlue() * 2;

        if (red > 0xff)
            red = 0xff;
        if (green > 0xff)
            green = 0xff;
        if (blue > 0xff)
            blue = 0xff;

        return new Color(null, new RGB(red, green, blue)); // here
    }

    // This function maps from the current attributes as "described" by escape sequences to real,
    // Eclipse console specific attributes (resolving color palette, default colors, etc.)
    public static void updateRangeStyle(StyleRange range, AnsiConsoleAttributes attribute) {
        AnsiConsoleAttributes tempAttrib = attribute.clone();

        boolean hilite = false;

        // Prepare the foreground color
        if (hilite) {
            if (tempAttrib.currentFgColor == null) {
                range.foreground = AnsiConsoleUtils.getDebugConsoleFgColor();
                range.foreground = hiliteRgbColor(range.foreground);
            } else {
                if (tempAttrib.currentFgColor < AnsiCommands.COMMAND_COLOR_INTENSITY_DELTA)
                    range.foreground = new Color(null, AnsiConsoleColorPalette.getColor(tempAttrib.currentFgColor
                            + AnsiCommands.COMMAND_COLOR_INTENSITY_DELTA));
                else
                    range.foreground = new Color(null, AnsiConsoleColorPalette.getColor(tempAttrib.currentFgColor));
            }
        } else {
            if (tempAttrib.currentFgColor != null)
                range.foreground = new Color(null, AnsiConsoleColorPalette.getColor(tempAttrib.currentFgColor));
        }

        // Prepare the background color
        if (tempAttrib.currentBgColor != null)
            range.background = new Color(null, AnsiConsoleColorPalette.getColor(tempAttrib.currentBgColor));

        // These two still mess with the foreground/background colors
        // We need to solve them before we use them for strike/underline/frame colors
        if (tempAttrib.invert) {
            if (range.foreground == null)
                range.foreground = AnsiConsoleUtils.getDebugConsoleFgColor();
            if (range.background == null)
                range.background = AnsiConsoleUtils.getDebugConsoleBgColor();
            Color tmp = range.background;
            range.background = range.foreground;
            range.foreground = tmp;
        }

        if (tempAttrib.conceal) {
            if (range.background == null)
                range.background = AnsiConsoleUtils.getDebugConsoleBgColor();
            range.foreground = range.background;
        }

        range.font = null;
        range.fontStyle = SWT.NORMAL;
        // Prepare the rest of the attributes
        if (tempAttrib.bold)
            range.fontStyle |= SWT.BOLD;

        if (tempAttrib.italic)
            range.fontStyle |= SWT.ITALIC;

        if (tempAttrib.underline != UNDERLINE_NONE) {
            range.underline = true;
            range.underlineColor = range.foreground;
            range.underlineStyle = tempAttrib.underline;
        } else
            range.underline = false;

        range.strikeout = tempAttrib.strike;
        range.strikeoutColor = range.foreground;

        if (tempAttrib.framed) {
            range.borderStyle = SWT.BORDER_SOLID;
            range.borderColor = range.foreground;
        } else
            range.borderStyle = SWT.NONE;
    }
}
