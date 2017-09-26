//============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006-2017 Talend – www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
//============================================================================
package org.talend.designer.esb.runcontainer.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class StringUtil {

    private StringUtil() {
    }
    
    public static String toString(InputStream in) {
        char[] buffer = new char[8196];
        StringBuilder answer = new StringBuilder();
        try (Reader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            while (r.read(buffer) != -1) {
                answer.append(buffer);
            }
        } catch (IOException e) {
            answer.append(e.toString());
        }
        return answer.toString();
    }
}
