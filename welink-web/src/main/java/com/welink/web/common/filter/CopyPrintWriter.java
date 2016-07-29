/**
 * Project Name:welink-web
 * File Name:PrintWriter.java
 * Package Name:com.welink.web.common.filter
 * Date:2015年9月30日下午12:02:24
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
*/

package com.welink.web.common.filter;

import java.io.PrintWriter;
import java.io.Writer;

/**
 * ClassName:PrintWriter <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2015年9月30日 下午12:02:24 <br/>
 * @author   LuoGuangChun
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
public class CopyPrintWriter extends PrintWriter {

    private StringBuilder copy = new StringBuilder();

    public CopyPrintWriter(Writer writer) {
        super(writer);
    }

    @Override
    public void write(int c) {
        copy.append((char) c); // It is actually a char, not an int.
        super.write(c);
    }

    @Override
    public void write(char[] chars, int offset, int length) {
        copy.append(chars, offset, length);
        super.write(chars, offset, length);
    }

    @Override
    public void write(String string, int offset, int length) {
        copy.append(string, offset, length);
        super.write(string, offset, length);
    }

    public String getCopy() {
        return copy.toString();
    }

}

