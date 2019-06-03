import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Verwaltet die globalen Anwendungseinstellungen
 * 
 * @author Mike Ungers
 *
 */
public class ConfigManager {
	public static final String CONFIG_KEY_SMTP_HOST = "smtpHost";
	public static final String CONFIG_KEY_SMTP_PORT = "smtpPort";
	public static final String CONFIG_KEY_SMTP_USERNAME = "smtpUsername";
	public static final String CONFIG_KEY_SMTP_PASSWORD = "smtpPassword";
	public static final String CONFIG_KEY_SMTP_USE_TLS = "smtpUseTls";
	public static final String CONFIG_KEY_SMTP_SENDER_ADDRESS = "smtpSenderAddress";
	public static final String CONFIG_KEY_MAIL_RECIPIENTS = "mailRecipients";
	public static final String CONFIG_KEY_CRONTRIGGER_EXPORT = "cronTriggerExport";
	public static final String CONFIG_KEY_CRONTRIGGER_IMPORT = "cronTriggerImport";
	public static final String CONFIG_KEY_EXPORT_PATH = "exportPath";
	public static final String CONFIG_KEY_DB_URL = "dataBaseURL";
	public static final String CONFIG_KEY_DB_USER = "dataBaseUser";
	public static final String CONFIG_KEY_DB_PASSWORD = "dataBasePassword";
	public static final String CONFIG_KEY_INFLUX_URL = "influxURL";
	public static final String CONFIG_KEY_INFLUX_USER = "influxUser";
	public static final String CONFIG_KEY_INFLUX_PASSWORD = "influxPassword";
	
	// Es gibt nur eine Instanz dieser Klasse
	private static final ConfigManager INSTANCE = new ConfigManager();
	public static ConfigManager getInstance() { return INSTANCE; }
	/** Die globalen Anwendungseinstellungen */
	private Properties properties = new Properties();
	
	/**
	 * Dieser Konstruktor lädt die globalen Anwedungseinstellungen
	 */
	public ConfigManager() {
		File configFile = new File("./config.properties");
		if (configFile.exists()) {
			try {
				properties.load(new FileInputStream(configFile));
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}
	}
	
	public Properties getProperties() {
		return properties;
	}
}
