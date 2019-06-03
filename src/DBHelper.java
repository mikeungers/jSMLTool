import java.util.Properties;
import java.util.Stack;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Pong;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

/**
 * Hilfsklasse zur Ansteuerung der Datenbank.
 * Liefert DAO Objekte zum Zugriff auf die einzelnen Tabellen.
 * 
 * @author Mike Ungers
 *
 */
public class DBHelper {
	/** Globale Anwendungseinstellungen */
	private final static Properties properties = ConfigManager.getInstance().getProperties();
	
	private InfluxDB influxDb;
	
	// Es gibt nur eine Instanz dieser Klasse
	private static final DBHelper INSTANCE = new DBHelper();
	public static DBHelper getInstance() {return INSTANCE;}
	private static final String DB_NAME = "logpv4j";
	
	private Stack<Point> influxTempStack = new Stack<Point>();
	
	/**
	 * Konstruktor welcher die Datenbankverbindung herstellt
	 */
	public DBHelper() {
		try {
			this.influxDb = InfluxDBFactory.connect(properties.getProperty(ConfigManager.CONFIG_KEY_INFLUX_URL, "http://localhost:8086"), properties.getProperty(ConfigManager.CONFIG_KEY_INFLUX_USER, "admin"), properties.getProperty(ConfigManager.CONFIG_KEY_INFLUX_PASSWORD, "12345678"));
			this.influxDb.setDatabase(DB_NAME);
		} catch (Throwable e) {
			System.err.println(e.getMessage());
		}
	}
	
	/**
	 * Schlieﬂt die Datenbankverbindung
	 */
	public void close() {
		if (this.influxDb != null) {
			this.influxDb.close();
		}
	}
	
	public void writeInflux(Point point) {
		Pong pong = null;
		try {
			pong = this.influxDb.ping();
		} catch (Throwable t) {
			this.influxTempStack.push(point);
		}
		if (pong != null) {
			while (!this.influxTempStack.empty()) {
				this.influxDb.write(this.influxTempStack.pop());
			}
			this.influxDb.write(point);
		}
	}
	
	public InfluxDB getInflux() {
		return this.influxDb;
	}
	
	public QueryResult influxQuery(String query) {
		return this.influxDb.query(new Query(query, DB_NAME));
	}
	
}
