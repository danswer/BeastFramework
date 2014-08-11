/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beast.framework;

import javax.swing.ImageIcon;

/**
 *
 * @author DrBonez
 */
public class Plugin {
    private final long serialVersionUID = 1L;
    public String version = "v1.0";
    public String description = "";
    public String jarName = "";   
    public boolean NEED_APAS = false;
    public boolean RUNS_ON_WINDOWS = true;
    public boolean RUNS_ON_UNIX = false;
    private final PluginListener beastListener;
    public ImageIcon icon;
    public boolean loggedIn;

    Plugin(String jarName, String ver, String desc, boolean apasNeeded,boolean runWindows,boolean runUnix,ImageIcon iconImage,final PluginListener bListener ){
        this.beastListener = bListener;
        this.version = ver;
        this.icon = iconImage;
        this.NEED_APAS = apasNeeded;
        this.RUNS_ON_UNIX = runUnix;
        this.RUNS_ON_WINDOWS = runWindows;
        this.description=desc;
        
    }
     public String getJarName() {
        return jarName;
    }
    public long getSerialVersionUID() {
        return serialVersionUID;
    }

    public  String getVersion() {
        return version;
    }

    public  String getDescription() {
        return description;
    }

    public  boolean isNEED_APAS() {
        return NEED_APAS;
    }

    public  boolean isRUNS_ON_WINDOWS() {
        return RUNS_ON_WINDOWS;
    }

    public  boolean isRUNS_ON_UNIX() {
        return RUNS_ON_UNIX;
    }

    public PluginListener getBeastListener() {
        return beastListener;
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }
}
