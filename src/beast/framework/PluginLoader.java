/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package beast.framework;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to dynamically load jar plugin and to load methods within the
 * dynamically loaded classes.
 *
 * @author Jorge.Molina & Daniel Jones
 */
 final class PluginLoader extends URLClassLoader {

    protected String path;

    /**
     * Constructor
     */
    PluginLoader(String path, ClassLoader parent) {
        super(new URL[0], parent);
        init(path);
    }

    void init(String path) {
        this.path = path;

        //find all JAR files on the path and subdirectories
        File f = new File(path);
        try {
            // Add plugin directory to search path
            addURL(f.toURI().toURL());
        } catch (MalformedURLException e) {
            System.err.println("PluginClassLoader: " + e);
        }
        String[] list = f.list();
        if (list == null) {
            return;
        }
        for (int i = 0; i < list.length; i++) {
            if (list[i].equals(".rsrc")) {
                continue;
            }
            File f2 = new File(path, list[i]);
            if (f2.isDirectory()) {
                addDirectory(f2);
            } else {
                addJar(f2);
            }
        }
        addDirectory(f, "jars"); // add ImageJ/jars; requested by Wilhelm Burger
    }

    private void addDirectory(File f) {
        System.out.println("PluginClassLoader.addDirectory: " + f);
        try {
            // Add first level subdirectories to search path
            addURL(f.toURI().toURL());
        } catch (MalformedURLException e) {
            System.err.println("PluginClassLoader: " + e);
        }
        String[] innerlist = f.list();
        if (innerlist == null) {
            return;
        }
        for (int j = 0; j < innerlist.length; j++) {
            File g = new File(f, innerlist[j]);
            if (g.isFile()) {
                addJar(g);
            }
        }
    }

    private void addJar(File f) {
        if (f.getName().endsWith(".jar") || f.getName().endsWith(".zip")) {
            System.out.println("PluginClassLoader.addJar: " + f);
            try {
                addURL(f.toURI().toURL());
            } catch (MalformedURLException e) {
                System.err.println("PluginClassLoader: " + e);
            }
        }
    }

    private void addDirectory(File f, String name) {
        f = f.getParentFile();
        if (f == null) {
            return;
        }
        f = new File(f, name);
        if (f == null) {
            return;
        }
        if (f.isDirectory()) {
            addDirectory(f);
        }
    }

    /**
     * Given the path of a jar it loads the jar and creates an instance of the
     * class
     *
     * @param pluginPath
     * @return an instance of the plugin's main class or null
     */
    public static Object LoadPlugIn(String pluginPath) {
        Class<?> plugIn = null;
        Object retVal = null;
        File jar = new File(pluginPath);
        JarFile jarFile;
        String mainClass = "";
        try {
            jarFile = new JarFile(jar);
             Manifest manifest = jarFile.getManifest();
             
            // check that this is your manifest and do what you need or get the next one

            Attributes attrs = (Attributes) manifest.getMainAttributes();
            mainClass = attrs.getValue(Attributes.Name.MAIN_CLASS);
//            for (Iterator it = attrs.keySet().iterator(); it.hasNext();) {
//                Attributes.Name attrName = (Attributes.Name) it.next();
//               String attrValue = attrs.getValue(attrName);
//                System.out.println(attrName + " " + attrValue);
//            }
            System.out.println(mainClass);
        } catch (IOException ex) {
            Logger.getLogger(PluginLoader.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            URLClassLoader classURL = new URLClassLoader(new URL[]{jar.toURL()}, PluginLoader.class.getClassLoader());           
            
            //TODO: Read class name that needs to be instantiated form MANIFEST
            plugIn = Class.forName(mainClass, false, classURL);
            retVal = plugIn.newInstance();
//            Method[] classList = retVal.getClass().getMethods();
//             for (Method classer : classList) {
//                 System.out.println(classer.getName());
//    }

        } catch (MalformedURLException ex) {
            Logger.getLogger(PluginLoader.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(PluginLoader.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(PluginLoader.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(PluginLoader.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        return retVal;
    }

    /**
     * Calls a method within dynamically loaded class with parameters
     *
     * @param methodName
     * @param plugInst
     * @param params - pass null is no params are needed
     * @return result object or null is nothing was returned
     */
    public static Object CallMethod(String methodName, Object plugInst, Object[] params) {

        Object retVal = null;
        try {
            Method pluginMethod = plugInst.getClass().getDeclaredMethod(methodName);
            retVal = pluginMethod.invoke(plugInst, params);

        } catch (NoSuchMethodException ex) {
            Logger.getLogger(PluginLoader.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(PluginLoader.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(PluginLoader.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(PluginLoader.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return retVal;
    }

}
