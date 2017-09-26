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
package org.talend.designer.esb.runcontainer.util;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

public class AnsiConsoleUtils {

    private final static String DEBUG_CONSOLE_PLUGIN_ID = "org.eclipse.debug.ui";

    private final static String DEBUG_CONSOLE_FALLBACK_BKCOLOR = "0,0,0";

    private final static String DEBUG_CONSOLE_FALLBACK_FGCOLOR = "192,192,192";

    static Color colorFromStringRgb(String strRgb) {
        Color result = null;
        String[] splitted = strRgb.split(",");
        if (splitted != null && splitted.length == 3) {
            int red = tryParseInteger(splitted[0]);
            int green = tryParseInteger(splitted[1]);
            int blue = tryParseInteger(splitted[2]);
            result = new Color(null, new RGB(red, green, blue));
        }
        return result;
    }

    public static Color getDebugConsoleBgColor() {
        IPreferencesService ps = Platform.getPreferencesService();
        String value = ps.getString(DEBUG_CONSOLE_PLUGIN_ID, "org.eclipse.debug.ui.consoleBackground",
                DEBUG_CONSOLE_FALLBACK_BKCOLOR, null);
        return colorFromStringRgb(value);
    }

    public static Color getDebugConsoleFgColor() {
        IPreferencesService ps = Platform.getPreferencesService();
        String value = ps.getString(DEBUG_CONSOLE_PLUGIN_ID, "org.eclipse.debug.ui.outColor", DEBUG_CONSOLE_FALLBACK_FGCOLOR,
                null);
        return colorFromStringRgb(value);
    }

    public static int tryParseInteger(String text) {
        if ("".equals(text))
            return -1;

        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

}
