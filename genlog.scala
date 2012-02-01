val df = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS")

do {
	println(df.format(new java.util.Date))
	Thread.sleep(1+scala.util.Random.nextInt(100))
} while(true)