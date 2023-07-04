package com.lib.citylib.camTra.utils;

import java.io.File;
import java.util.Arrays;

public class DirectoryStructure {
    private StringBuilder buf =new StringBuilder();
    StringBuilder sb = new StringBuilder();
    int kai = 0;
    int id = 0;
    public StringBuilder getBuf() {
        return this.buf;
    }

    public void println() {
        System.out.println(buf);
    }
    public void scan(String path) {
        File f = new File(path);
        if (!f.getName().startsWith(".")) {
            if (f.isDirectory()) {
                scan(new File(path));
                buf.delete(buf.length() - 2, buf.length());
            } else {
                System.out.format("{\"label\" : \"%s\"}", f.getName() + " is not a directory!");
            }
        }
    }
    private void scan(File f) {
        if (!f.getName().startsWith(".")) {
            if (f.isDirectory() && f.listFiles().length != 0) {
                buf.append(space(kai)).append("{\n")
                        .append(space(++kai)).append("\"label\" : \"").append(f.getName()).append("\",\n")
                        .append(space(kai)).append("\"id\" : \"").append(id++).append("\",\n")
                        .append(space(kai)).append("\"children\" : [\n");
                kai++;
                Arrays.asList(f.listFiles()).forEach(this::scan);
                buf.delete(buf.length() - 2, buf.length());
                buf.append("\n").append(space(--kai)).append("]\n").append(space(--kai)).append("},\n");
            } else {
                buf.append(space(kai)).append("{\n")
                        .append(space(++kai)).append("\"label\" : \"").append(f.getName()).append("\",\n")
                        .append(space(kai)).append("\"id\" : \"").append(id++).append("\"\n")
                        .append(space(--kai)).append("},\n");
            }
        }
    }
    public String space(int kai) {
        if (kai <= 0) {
            return "";
        }
        char[] cs = new char[kai << 1];
        Arrays.fill(cs, ' ');
        return new String(cs, 0, cs.length);
    }
}
