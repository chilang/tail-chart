import com.sun.tools.visualvm.charts.ChartFactory
import com.sun.tools.visualvm.charts.ChartFactory
import com.sun.tools.visualvm.charts.SimpleXYChartDescriptor
import com.sun.tools.visualvm.charts.SimpleXYChartDescriptor
import com.sun.tools.visualvm.charts.SimpleXYChartSupport
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.UIManager
import java.io.{InputStreamReader, BufferedReader}
import java.lang.String
import java.util.Date


UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)
SwingUtilities.invokeLater(new Demo)

class Generator(val support: SimpleXYChartSupport, val scaleFactor:Double) extends Thread {
  val ITEMS_COUNT: Int = 8
  val WINDOW_SIZE = 10

  override def run: Unit = {
    val TIMESTAMP_PATTERN: String = "yyyy-MM-dd HH:mm:ss,SSS"
    val df = new java.text.SimpleDateFormat(TIMESTAMP_PATTERN)
    val input = new BufferedReader(new InputStreamReader(System.in))
    var line: String = input.readLine()
    val window = new scala.collection.mutable.Queue[Date]()
    
    while (line != null) {
      val timeLog = line.take(TIMESTAMP_PATTERN.length)
      val timestamp: Date = df.parse(timeLog)
      window.enqueue(timestamp)
      if (window.size > WINDOW_SIZE) window.dequeue()
      val windowTimeSpanInMs: Long = timestamp.getTime - window.head.getTime
      val txRatePerSec: Double = if (window.size == 1) 0 else window.size / (windowTimeSpanInMs / 1000.0)
      support.addValues(timestamp.getTime, Array[Long]((txRatePerSec * scaleFactor).toLong))

      line = input.readLine()
    }
  }
}

class Demo extends Runnable {
  val VALUES_LIMIT: Int = 300
  val ITEMS_COUNT: Int = 1
  val SCALING_FACTOR:Double = 100.0
  var support: SimpleXYChartSupport = null

  private def createModels: Unit = {
    val descriptor: SimpleXYChartDescriptor = SimpleXYChartDescriptor.decimal(
      /*minValue=*/0L,
      /*maxValue=*/20L,
      /*initialYMargin=*/10L,
      /*chartFactor=*/1.0 / SCALING_FACTOR,
      /*hideableItems=*/true,
      /*valuesBuffer=*/VALUES_LIMIT)
    
    1 to ITEMS_COUNT foreach( i => descriptor.addLineFillItems("Item " + i))
    
    descriptor.setChartTitle("<html><font size='+1'><b>Demo Chart</b></font></html>")
    descriptor.setXAxisDescription("<html>X Axis <i>[time]</i></html>")
    descriptor.setYAxisDescription("<html>Y Axis <i>[units]</i></html>")
    support = ChartFactory.createSimpleXYChart(descriptor)
    val generator = new Generator(support, SCALING_FACTOR)
    generator.start
  }

  private def createUI: Unit = {
    val frame: JFrame = new JFrame("Charts Test")
    frame.getContentPane.add(support.getChart)
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    frame.setBounds(100, 200, 800, 600)
    frame.setVisible(true)
  }

  def run: Unit = {
    createModels
    createUI
  }
}