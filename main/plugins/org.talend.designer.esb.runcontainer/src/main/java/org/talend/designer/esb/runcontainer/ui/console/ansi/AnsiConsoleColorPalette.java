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

import org.eclipse.swt.graphics.RGB;

public class AnsiConsoleColorPalette {

    public static final String PALETTE_VGA = "paletteVGA";

    public static final int PALETTE_SIZE = 256;

    // From Wikipedia, http://en.wikipedia.org/wiki/ANSI_escape_code
    private final static RGB[] paletteVGA = { new RGB(0, 0, 0), // black
            new RGB(170, 0, 0), // red
            new RGB(0, 170, 0), // green
            new RGB(170, 85, 0), // brown/yellow
            new RGB(0, 0, 170), // blue
            new RGB(170, 0, 170), // magenta
            new RGB(0, 170, 170), // cyan
            new RGB(170, 170, 170), // gray
            new RGB(85, 85, 85), // dark gray
            new RGB(255, 85, 85), // bright red
            new RGB(85, 255, 85), // bright green
            new RGB(255, 255, 85), // yellow
            new RGB(85, 85, 255), // bright blue
            new RGB(255, 85, 255), // bright magenta
            new RGB(85, 255, 255), // bright cyan
            new RGB(255, 255, 255) // white
    };

    private static RGB[] palette = paletteVGA;

    private static String currentPaletteName = PALETTE_VGA;

    public static boolean isValidIndex(int value) {
        return value >= 0 && value < PALETTE_SIZE;
    }

    static int TRUE_RGB_FLAG = 0x10000000; // Representing true RGB colors as 0x10RRGGBB

    public static int hackRgb(int r, int g, int b) {
        if (!isValidIndex(r))
            return -1;
        if (!isValidIndex(g))
            return -1;
        if (!isValidIndex(b))
            return -1;
        return TRUE_RGB_FLAG | r << 16 | g << 8 | b;
    }

    static int safe256(int value, int modulo) {
        int result = value * PALETTE_SIZE / modulo;
        return result < PALETTE_SIZE ? result : PALETTE_SIZE - 1;
    }

    public static RGB getColor(Integer index) {
        if (null == index)
            return null;

        if (index >= TRUE_RGB_FLAG) {
            int red = index >> 16 & 0xff;
            int green = index >> 8 & 0xff;
            int blue = index & 0xff;
            return new RGB(red, green, blue);
        }

        if (index >= 0 && index < palette.length) // basic, 16 color palette
            return palette[index];

        if (index >= 16 && index < 232) { // 6x6x6 color matrix
            int color = index - 16;
            int blue = color % 6;
            color = color / 6;
            int green = color % 6;
            int red = color / 6;

            return new RGB(safe256(red, 6), safe256(green, 6), safe256(blue, 6));
        }

        if (index >= 232 && index < PALETTE_SIZE) { // grayscale
            int gray = safe256(index - 232, 24);
            return new RGB(gray, gray, gray);
        }

        return null;
    }

    public static String getPalette() {
        return currentPaletteName;
    }

}
