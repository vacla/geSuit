package net.cubespace.geSuit.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import net.cubespace.geSuit.core.util.Utilities;

public class MOTDFile {
    private File file;
    private String MOTD;

    public MOTDFile(File file) {
        this.file = file;
    }
    
    public File getFile() {
        return file;
    }
    
    public void load() throws IOException {
        if (!file.exists()) {
            MOTD = "";
            return;
        }
        
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while((line = reader.readLine()) != null) {
        	if (builder.length() != 0) {
        	    builder.append('\n');
        	}
        	builder.append(line);
        }
        
        reader.close();
        MOTD = Utilities.colorize(builder.toString());
    }
    
    public String getMOTD() {
        return MOTD;
    }
}
