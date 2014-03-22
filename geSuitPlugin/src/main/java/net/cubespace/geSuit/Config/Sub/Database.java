package net.cubespace.geSuit.Config.Sub;

import net.cubespace.Yamler.Config.Config;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class Database extends Config {
    public String URL = "jdbc:h2:{DIR}geSuit";
    public String Username = "username";
    public String Password = "password";
}
