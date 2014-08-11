/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beast.framework;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author DrBonez
 */
final class PluginClassLoader extends URLClassLoader {
   
    private final Map<String, Class<?>> classes = new HashMap<String, Class<?>>();
    
    private final File dataFolder;
    protected File file;
    
    private IllegalStateException pluginState;

    PluginClassLoader( final ClassLoader parent, final File dataFolder, final File file)  {
        super(new URL[0] , parent);
    
        this.dataFolder = dataFolder;
        this.file = file;

       
    }
}

