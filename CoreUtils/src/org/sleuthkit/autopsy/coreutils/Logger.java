package org.sleuthkit.autopsy.coreutils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ResourceBundle;
import java.util.logging.*;

public class Logger extends java.util.logging.Logger{
    
  //File Handlers which point to the output logs
  static final FileHandler traces = initTraces();
  static final FileHandler normal = initNormal();
  
  
  
  /** Static blocks to get around compile errors
   * such as "variable might not have been initialized
   **/
  //<editor-fold defaultstate="visible" desc="static block initializers">
  
  private static FileHandler initTraces(){
      
      try{
          
          FileHandler f = new FileHandler("autopsy_traces.log");
          f.setEncoding(Charset.forName("UTF-8").name());
          f.setFormatter(new SimpleFormatter());
          return f;
      }
      catch(IOException e){
          return null;
      }
  }
  
  private static FileHandler initNormal(){
      try{
          FileHandler f = new FileHandler("autopsy.log");
          f.setEncoding(Charset.forName("UTF-8").name());
          f.setFormatter(new SimpleFormatter());
          return f;
      }
      catch(IOException e){
          return null;
      }
  }
  
  //</editor-fold>
  
   Logger(java.util.logging.Logger log){
      super(log.getName(), log.getResourceBundleName());
      log.setUseParentHandlers(false);
  }
   
     
   public static Logger getLogger(String name) {
       Logger l = new Logger(java.util.logging.Logger.getLogger(name));
       l.addHandler(normal);
       l.addHandler(traces);
       return l;
   }
        

   public static Logger getLogger(String name, String resourceBundleName) {
       return new Logger(Logger.getLogger(name, resourceBundleName));
   }
   
    @Override
 public synchronized void  log(Level level, String message, Throwable thrown){
     super.log(level, message + " : Error snippet:  " + thrown.toString());
     removeHandler(normal);
     super.log(level, message, thrown);
     addHandler(normal);
 }
 
    @Override
 public synchronized void throwing(String sourceClass, String sourceMethod, Throwable thrown){
     removeHandler(normal);
     super.throwing(sourceClass, sourceMethod, thrown);
     addHandler(normal);
 }
 
 
}
