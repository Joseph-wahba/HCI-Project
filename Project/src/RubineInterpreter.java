
import edu.berkeley.guir.lib.satin.*;
import edu.berkeley.guir.lib.awt.geom.Polygon2D;
import edu.berkeley.guir.lib.collection.SortedValueMap;
import edu.berkeley.guir.lib.debugging.Debug;
import edu.berkeley.guir.lib.gesture.TrainingException;
import edu.berkeley.guir.lib.properties.FlexProperties;
import edu.berkeley.guir.lib.satin.SatinConstants;
import edu.berkeley.guir.lib.satin.event.NewStrokeEvent;
import edu.berkeley.guir.lib.satin.event.SingleStrokeEvent;
import edu.berkeley.guir.lib.satin.event.StrokeEvent;
import edu.berkeley.guir.lib.satin.event.UpdateStrokeEvent;
import edu.berkeley.guir.lib.satin.interpreter.InterpreterImpl;
import edu.berkeley.guir.lib.satin.recognizer.Classification;
import edu.berkeley.guir.lib.satin.recognizer.Recognizer;
import edu.berkeley.guir.lib.satin.recognizer.rubine.RubineRecognizer;
import edu.berkeley.guir.lib.satin.stroke.TimedStroke;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.net.URL;
import java.text.ParseException;
import java.util.WeakHashMap;

public abstract class RubineInterpreter
  extends InterpreterImpl
{
  static final long serialVersionUID = -8744819975929174646L;
  private static final Debug debug = new Debug(true);
  public static final String RUBINE_DATA_DIRECTORY_PROPERTY = "RUBINE_DATA_DIRECTORY_PROPERTY";
  public static final String RUBINE_DATA_DIRECTORY_DEFAULT = "E:\\HCI\\Projects\\hci\\";
  public static final String RUBINE_DATA_URL_PROPERTY = "RubineData";
  public static final String RUBINE_DATA_URL_DEFAULT = "";
  
  static
  {
    SatinConstants.glprops.setProperty("RUBINE_DATA_DIRECTORY_PROPERTY", "E:\\HCI\\Projects\\hci\\");
    
    SatinConstants.glprops.setProperty("RubineData", "");
  }
  
  private static final WeakHashMap cache = new WeakHashMap();
  boolean flagSimplify = false;
  String strFileName;
  double confidence;
  transient RubineRecognizer recognizer;
  private FileInputStream localObject1;
  private InputStreamReader localObject2;
  
  public RubineInterpreter(String paramString)
  {
    this(paramString, false);
  }
  
  public RubineInterpreter(String paramString, boolean paramBoolean)
  {
    this.strFileName = paramString;
    this.recognizer = initializeRecognizer(paramString);
    
    setSimplify(paramBoolean);
    
    commonInitializations();
  }
  
  private void commonInitializations()
  {
    setName("Rubine Interpreter");
    this.confidence = 0.8D;
  }
  
  @SuppressWarnings("unused")
private RubineRecognizer initializeRecognizer(String paramString)
  {
    RubineRecognizer localRubineRecognizer = null;
    try
    {
      String str1 = (String)SatinConstants.glprops.getProperty("RUBINE_DATA_DIRECTORY_PROPERTY") + paramString;
      //System.out.println(str1);
      this.localObject1 = new FileInputStream(str1);
      this.localObject2 = new InputStreamReader((InputStream)localObject1);
      localRubineRecognizer = new RubineRecognizer((Reader)localObject2);
      cache.put(paramString, localRubineRecognizer);
      
      return localRubineRecognizer;
    }
    catch (Exception localException)
    {
      Object localObject1;
      Object localObject2;
      debug.println(localException);
      try
      {
        String str2 = (String)SatinConstants.glprops.getProperty("RubineData") + paramString;
        
        localObject1 = new URL(str2);
        
        localObject2 = ((URL)localObject1).openStream();
        InputStreamReader localInputStreamReader = new InputStreamReader((InputStream)localObject2);
        
        localRubineRecognizer = new RubineRecognizer(localInputStreamReader);
        cache.put(paramString, localRubineRecognizer);
        System.out.println((RubineRecognizer)cache.get(paramString));
        return localRubineRecognizer;
      }
      catch (FileNotFoundException localFileNotFoundException)
      {
        debug.println(localFileNotFoundException);
        debug.println("Initialization Error - using empty Rubine Recognizer");
        localRubineRecognizer = new RubineRecognizer();
      }
      catch (ParseException localParseException)
      {
        debug.println(localParseException);
        debug.println("Bad Rubine data file.");
        debug.println("Initialization Error - using empty Rubine Recognizer");
        localRubineRecognizer = new RubineRecognizer();
      }
      catch (TrainingException localTrainingException)
      {
        debug.println(localTrainingException);
        debug.println("Error in Rubine Recognizer processing data file.");
        debug.println("Initialization Error - using empty Rubine Recognizer");
        localRubineRecognizer = new RubineRecognizer();
      }
      catch (IOException localIOException)
      {
        debug.println(localIOException);
        debug.println("Initialization Error - using empty Rubine Recognizer");
        localRubineRecognizer = new RubineRecognizer();
      }
    }
    //localRubineRecognizer = (RubineRecognizer)cache.get(paramString);
    if (localRubineRecognizer != null) {
    	System.out.println("here");
      return localRubineRecognizer;
    }
    if (paramString.equals(""))
    {
    	System.out.println("here");
      localRubineRecognizer = new RubineRecognizer();
      cache.put(paramString, localRubineRecognizer);
      return localRubineRecognizer;
    }
    
    return localRubineRecognizer;
  }
  
  public Recognizer getRecognizer()
  {
    return this.recognizer;
  }
  
  public double getConfidence()
  {
    return this.confidence;
  }
  
  public String getFileName()
  {
    return this.strFileName;
  }
  
  public void setConfidence(double paramDouble)
  {
    this.confidence = paramDouble;
  }
  
  public void setSimplify(boolean paramBoolean)
  {
    this.flagSimplify = paramBoolean;
  }
  
  public void handleSingleStroke(SingleStrokeEvent paramSingleStrokeEvent)
  {
    if (this.recognizer == null) {
      return;
    }
    TimedStroke localTimedStroke = paramSingleStrokeEvent.getStroke();
    
    localTimedStroke = preprocessStroke(localTimedStroke);
    
    Classification localClassification = this.recognizer.classify(localTimedStroke);
    debug.println(localClassification);
    
    Double localDouble = (Double)localClassification.getFirstValue();
    if (localDouble.doubleValue() < this.confidence) {
      return;
    }
    handleSingleStroke(paramSingleStrokeEvent, localClassification);
  }
  
  protected TimedStroke preprocessStroke(TimedStroke paramTimedStroke)
  {
    if (this.flagSimplify == true)
    {
      Object localObject = paramTimedStroke.getPolygon2D(12);
      localObject = ((Polygon2D)localObject).simplify();
      return new TimedStroke((Polygon2D)localObject);
    }
    return paramTimedStroke;
  }
  
  public RubineInterpreter clone(RubineInterpreter paramRubineInterpreter)
  {
    paramRubineInterpreter.strFileName = this.strFileName;
    paramRubineInterpreter.recognizer = paramRubineInterpreter.initializeRecognizer(this.strFileName);
    paramRubineInterpreter.commonInitializations();
    return paramRubineInterpreter;
  }
  
  private void readObject(ObjectInputStream paramObjectInputStream)
    throws IOException
  {
    try
    {
      paramObjectInputStream.defaultReadObject();
    }
    catch (Exception localException)
    {
      debug.println(localException);
    }
    initializeRecognizer(this.strFileName);
  }
  
  private void writeObject(ObjectOutputStream paramObjectOutputStream)
    throws IOException
  {
    paramObjectOutputStream.defaultWriteObject();
  }
  
  private RubineInterpreter() {}
  
  public void handleNewStroke(NewStrokeEvent paramNewStrokeEvent) {}
  
  public void handleUpdateStroke(UpdateStrokeEvent paramUpdateStrokeEvent) {}
  
  protected abstract void handleSingleStroke(SingleStrokeEvent paramSingleStrokeEvent, Classification paramClassification);
}
