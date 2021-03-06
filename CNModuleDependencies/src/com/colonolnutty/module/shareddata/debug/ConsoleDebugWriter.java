package com.colonolnutty.module.shareddata.debug;

/**
 * User: Jack's Computer
 * Date: 10/04/2017
 * Time: 11:07 AM
 */
public class ConsoleDebugWriter extends DebugWriter {
    @Override
    public void writeln(String text) {
        System.out.println(text);
    }

    @Override
    public void write(Exception e) {
        e.printStackTrace(System.out);
    }
}
