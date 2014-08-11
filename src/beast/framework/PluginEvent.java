/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beast.framework;

import java.util.EventObject;

/**
 *
 * @author DrBonez
 */
public class PluginEvent extends EventObject {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String message = "";
    private String pluginSourceName = "";

    public PluginEvent(Object source, String msg, String pluginJarFileName) {
        super(source);
        message = msg;
        pluginSourceName = pluginJarFileName;
        // TODO Auto-generated constructor stub
    }

    public String GetMessage() {
        return message;
    }

    public String getPluginSourceName() {
        return pluginSourceName;
    }
}
